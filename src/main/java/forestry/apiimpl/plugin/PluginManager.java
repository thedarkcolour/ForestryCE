package forestry.apiimpl.plugin;

import forestry.api.modules.ForestryModuleIds;
import forestry.api.ForestryConstants;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.mojang.datafixers.util.Pair;
import forestry.Forestry;
import forestry.api.IForestryApi;
import forestry.api.core.circuits.CircuitHolder;
import forestry.api.core.circuits.ICircuit;
import forestry.api.core.circuits.ICircuitLayout;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.genetics.IAnalyzerPlugin;
import forestry.api.core.IError;
import forestry.api.core.genetics.ISpeciesType;
import forestry.api.core.genetics.ITaxon;
import forestry.api.core.genetics.pollen.IPollenType;
import forestry.api.plugin.IForestryPlugin;
import forestry.api.plugin.IPollenRegistration;
import forestry.apiimpl.ForestryApiImpl;
import forestry.apiimpl.GeneticManager;
import forestry.apiimpl.client.ButterflyClientManager;
import forestry.apiimpl.client.ForestryClientApiImpl;
import forestry.apiimpl.client.genetics.GeneticClientManager;
import forestry.apiimpl.client.plugin.ClientRegistration;
import forestry.core.engine.circuits.CircuitLayout;
import forestry.core.engine.circuits.CircuitManager;
import forestry.core.platform.errors.ErrorManager;
import forestry.core.engine.genetics.PollenManager;
import forestry.core.content.sorting.FilterManager;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import forestry.api.modules.IForestryModule;

public class PluginManager {
	private static final ArrayList<IForestryPlugin> LOADED_PLUGINS = new ArrayList<>();

	/**
	 * @return The loaded plugins, in the order their hooks are called
	 */
	public static List<IForestryPlugin> getLoadedPlugins() {
		return Collections.unmodifiableList(LOADED_PLUGINS);
	}

	// The plugins that ship base Forestry's own content. Listed rather than derived from the namespace
	// because forestry:kubejs shares that namespace and must keep running after them, as it did when
	// this was a single DefaultForestryPlugin moved to the front.
	private static final Set<ResourceLocation> BASE_PLUGIN_IDS = Set.of(
		ForestryConstants.forestry("default"),
		ForestryModuleIds.APICULTURE,
		ForestryModuleIds.ARBORICULTURE,
		ForestryModuleIds.LEPIDOPTEROLOGY,
		ForestryModuleIds.FARMING);

	// Loads all plugins from the service loader.
	public static void loadPlugins() {
		ServiceLoader<IForestryPlugin> serviceLoader = ServiceLoader.load(IForestryPlugin.class);

		// Base Forestry's plugins register before every other plugin, including forestry:kubejs, so a
		// script or addon can build on the base content. Partitioned rather than moved to the front one
		// at a time: there are five of them now, and a repeated add(0, ...) would reverse their order.
		// The id sort is preserved within each group, and species type registration order depends on it.
		List<IForestryPlugin> basePlugins = new ArrayList<>();
		List<IForestryPlugin> otherPlugins = new ArrayList<>();

		serviceLoader.stream().map(ServiceLoader.Provider::get).sorted(Comparator.comparing(IForestryPlugin::id)).forEachOrdered(plugin -> {
			if (plugin.shouldLoad()) {
				(BASE_PLUGIN_IDS.contains(plugin.id()) ? basePlugins : otherPlugins).add(plugin);
				Forestry.LOGGER.debug("Registered IForestryPlugin {} with class {}", plugin.id(), plugin.getClass().getName());
			} else {
				Forestry.LOGGER.warn("Detected IForestryPlugin {} with class {} but did not load it because IForestryPlugin.shouldLoad returned false.", plugin.id(), plugin.getClass().getName());
			}
		});

		LOADED_PLUGINS.addAll(basePlugins);
		LOADED_PLUGINS.addAll(otherPlugins);
		LOADED_PLUGINS.trimToSize();
	}

	public static void registerErrors() {
		ErrorRegistration registration = new ErrorRegistration();

		for (IForestryPlugin plugin : LOADED_PLUGINS) {
			plugin.registerErrors(registration);
		}

		ArrayList<IError> errors = registration.getErrors();
		int errorCount = errors.size();
		Short2ObjectOpenHashMap<IError> byNumericId = new Short2ObjectOpenHashMap<>(errorCount);
		Object2ShortOpenHashMap<IError> numericIdLookup = new Object2ShortOpenHashMap<>(errorCount);
		ImmutableMap.Builder<ResourceLocation, IError> byId = ImmutableMap.builderWithExpectedSize(errorCount);

		for (int i = 0; i < errors.size(); i++) {
			IError error = errors.get(i);
			byNumericId.put((short) i, error);
			numericIdLookup.put(error, (short) i);
			byId.put(error.getId(), error);
		}

		((ForestryApiImpl) IForestryApi.INSTANCE).setErrorManager(new ErrorManager(byNumericId, numericIdLookup, byId.build()));
	}

	// Runs after all items are registered so that electron tubes and circuit boards are available.
	public static void registerCircuits() {
		CircuitRegistration registration = new CircuitRegistration();

		for (IForestryPlugin plugin : LOADED_PLUGINS) {
			plugin.registerCircuits(registration);
		}

		ArrayList<CircuitLayout> layouts = registration.getLayouts();
		ImmutableMap.Builder<String, ICircuitLayout> layoutsByIdBuilder = ImmutableMap.builderWithExpectedSize(layouts.size());

		for (CircuitLayout layout : layouts) {
			// Layouts by ID
			layoutsByIdBuilder.put(layout.getId(), layout);
		}

		ImmutableMap<String, ICircuitLayout> layoutsById = layoutsByIdBuilder.build();

		ArrayList<CircuitHolder> circuits = registration.getCircuits();
		ImmutableMultimap.Builder<ICircuitLayout, CircuitHolder> circuitHoldersBuilder = new ImmutableMultimap.Builder<>();
		ImmutableMap.Builder<String, ICircuit> circuitsBuilder = ImmutableMap.builderWithExpectedSize(circuits.size());

		for (CircuitHolder holder : circuits) {
			ICircuitLayout layout = layoutsById.get(holder.layoutId());

			if (layout == null) {
				throw new IllegalStateException("Attempted to register a CircuitHolder but no layout was registered with its layout ID: " + holder);
			}

			// Circuit holders by layout
			circuitHoldersBuilder.put(layout, holder);
			// Circuits by ID
			ICircuit circuit = holder.circuit();
			circuitsBuilder.put(circuit.getId(), circuit);
		}

		try {
			((ForestryApiImpl) IForestryApi.INSTANCE).setCircuitManager(new CircuitManager(circuitHoldersBuilder.build(), layoutsById, circuitsBuilder.buildOrThrow()));
		} catch (IllegalArgumentException exception) {
			Forestry.LOGGER.fatal("Failed to register circuits: two circuits were registered with the same ID");
			throw exception;
		}
	}

	public static void registerGenetics() {
		GeneticRegistration registration = new GeneticRegistration();

		// Register SPECIES TYPES, karyotypes, filter rules and set up taxonomy
		for (IForestryPlugin plugin : LOADED_PLUGINS) {
			plugin.registerGenetics(registration);
		}

		((forestry.core.engine.genetics.ForestryFlowerTypeManager) IForestryApi.INSTANCE.getFlowerTypeManager())
				.setCodeFlowerTypes(registration.getFlowerTypes());

		ImmutableMap<ResourceLocation, ISpeciesType<?, ?>> speciesTypes = registration.buildSpeciesTypes();
		ImmutableMap<String, ITaxon> taxa = registration.buildTaxa();

		Forestry.LOGGER.debug("Registered {} species types: {}", speciesTypes.size(), Arrays.toString(speciesTypes.keySet().toArray(new ResourceLocation[0])));

		ForestryApiImpl api = (ForestryApiImpl) IForestryApi.INSTANCE;
		GeneticManager geneticManager = new GeneticManager(taxa, speciesTypes);
		api.setGeneticManager(geneticManager);
		api.setFilterManager(new FilterManager(registration.getFilterRuleTypes()));

		// Register SPECIES for each type
		LinkedHashMap<ISpeciesType<?, ?>, ImmutableMap<ResourceLocation, ?>> allSpecies = new LinkedHashMap<>(speciesTypes.size());

		// go through species builders and build each species
		for (ISpeciesType<?, ?> speciesType : speciesTypes.values()) {
			ImmutableMap<ResourceLocation, ?> species = speciesType.handleSpeciesRegistration(LOADED_PLUGINS);

			allSpecies.put(speciesType, species);

			Forestry.LOGGER.debug("Registered {} species for species type {}", species.size(), speciesType.id());
		}

		for (Map.Entry<ISpeciesType<?, ?>, ImmutableMap<ResourceLocation, ?>> entry : allSpecies.entrySet()) {
			ISpeciesType<?, ?> speciesType = entry.getKey();

			// Data-driven species types (e.g. bees) are legitimately empty here; their species map
			// is populated later by a datapack reload listener, so no empty-species guard is enforced.
			speciesType.onSpeciesRegistered((ImmutableMap) entry.getValue());
		}
	}

	public static void registerPollen() {
		HashMap<ResourceLocation, IPollenType<?>> pollenTypes = new HashMap<>();
		IPollenRegistration registration = pollen -> {
			ResourceLocation id = pollen.id();
			if (pollenTypes.containsKey(id)) {
				throw new IllegalStateException("A pollen type was already registered with ID " + pollen + ": " + pollenTypes.get(id));
			} else {
				pollenTypes.put(id, pollen);
			}
		};

		for (IForestryPlugin plugin : LOADED_PLUGINS) {
			plugin.registerPollen(registration);
		}

		((ForestryApiImpl) IForestryApi.INSTANCE).setPollenManager(new PollenManager(ImmutableMap.copyOf(pollenTypes)));
	}

	public static void registerClient() {
		ClientRegistration registration = new ClientRegistration();

		for (IForestryPlugin plugin : LOADED_PLUGINS) {
			plugin.registerClient(consumer -> consumer.accept(registration));
		}

		// Each module builds its own client manager from the completed registration and installs it
		// here. See IForestryModule.installClientManagers
		for (IForestryModule module : IForestryApi.INSTANCE.getModuleManager().getLoadedModules()) {
			module.applyClientPluginRegistration(registration);
		}

		// Butterflies
		// id-keyed: resolving a species happens at render time by id (ButterflyClientManager#getTextures falls back
		// to the default naming convention, computed from the id alone, for any id with no explicit registration),
		// so the (datapack-driven) species list is not needed to build this map.
		Map<ResourceLocation, Pair<ResourceLocation, ResourceLocation>> butterflyTextures = new HashMap<>(registration.getButterflyTextures());
		((ForestryClientApiImpl) IForestryClientApi.INSTANCE).setButterflyManager(new ButterflyClientManager(butterflyTextures));

		HashMap<ResourceLocation, IAnalyzerPlugin<?, ?>> analyzerPluginsById = registration.getAnalyzerPlugins();
		IdentityHashMap<ISpeciesType<?, ?>, IAnalyzerPlugin<?, ?>> analyzerPlugins = new IdentityHashMap<>(analyzerPluginsById.size());
		for (ISpeciesType<?, ?> type : IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypes()) {
			IAnalyzerPlugin<?, ?> plugin = analyzerPluginsById.get(type.id());
			if (plugin == null) {
				Forestry.LOGGER.warn("No IAnalyzerPlugin registered for species type {}", type.id());
			} else {
				analyzerPlugins.put(type, plugin);
			}
		}
		((ForestryClientApiImpl) IForestryClientApi.INSTANCE).setGeneticsManager(new GeneticClientManager(analyzerPlugins));
	}
}
