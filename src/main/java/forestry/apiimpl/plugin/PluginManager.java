package forestry.apiimpl.plugin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.mojang.datafixers.util.Pair;
import forestry.Forestry;
import forestry.api.IForestryApi;
import forestry.api.circuits.CircuitHolder;
import forestry.api.circuits.ICircuit;
import forestry.api.circuits.ICircuitLayout;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.arboriculture.ILeafSprite;
import forestry.api.client.arboriculture.ILeafTint;
import forestry.api.client.genetics.IAnalyzerPlugin;
import forestry.api.core.IError;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.ITaxon;
import forestry.api.genetics.pollen.IPollenType;
import forestry.api.plugin.IForestryPlugin;
import forestry.api.plugin.IPollenRegistration;
import forestry.apiimpl.ForestryApiImpl;
import forestry.apiimpl.GeneticManager;
import forestry.apiimpl.client.BeeClientManager;
import forestry.apiimpl.client.ButterflyClientManager;
import forestry.apiimpl.client.ForestryClientApiImpl;
import forestry.apiimpl.client.TreeClientManager;
import forestry.apiimpl.client.genetics.GeneticClientManager;
import forestry.apiimpl.client.plugin.ClientRegistration;
import forestry.core.circuits.CircuitLayout;
import forestry.core.circuits.CircuitManager;
import forestry.core.errors.ErrorManager;
import forestry.core.genetics.PollenManager;
import forestry.core.utils.SpeciesUtil;
import forestry.farming.FarmingManager;
import forestry.plugin.DefaultForestryPlugin;
import forestry.sorting.FilterManager;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

import javax.annotation.Nullable;
import java.util.*;

public class PluginManager {
	private static final ArrayList<IForestryPlugin> LOADED_PLUGINS = new ArrayList<>();

	// Loads all plugins from the service loader.
	public static void loadPlugins() {
		ServiceLoader<IForestryPlugin> serviceLoader = ServiceLoader.load(IForestryPlugin.class);

		serviceLoader.stream().map(ServiceLoader.Provider::get).sorted(Comparator.comparing(IForestryPlugin::id)).forEachOrdered(plugin -> {
			if (plugin.shouldLoad()) {
				if (plugin.getClass() == DefaultForestryPlugin.class) {
					LOADED_PLUGINS.add(0, plugin);
				} else {
					LOADED_PLUGINS.add(plugin);
				}
				Forestry.LOGGER.debug("Registered IForestryPlugin {} with class {}", plugin.id(), plugin.getClass().getName());
			} else {
				Forestry.LOGGER.warn("Detected IForestryPlugin {} with class {} but did not load it because IForestryPlugin.shouldLoad returned false.", plugin.id(), plugin.getClass().getName());
			}
		});

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
			// TODO remove in 1.20 when FMLCommonSetupEvent throws
			// rethrow swallowed exception
			try {
				plugin.registerCircuits(registration);
			} catch (Throwable e) {
				asyncThrown = new RuntimeException("An error was thrown by plugin " + plugin.id() + " during IForestryPlugin.registerCircuits", e);
				Forestry.LOGGER.fatal(asyncThrown);
			}
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

		ImmutableMap<ResourceLocation, ISpeciesType<?, ?>> speciesTypes = registration.buildSpeciesTypes();
		ImmutableMap<String, ITaxon> taxa = registration.buildTaxa();

		Forestry.LOGGER.debug("Registered {} species types: {}", speciesTypes.size(), Arrays.toString(speciesTypes.keySet().toArray(new ResourceLocation[0])));

		// Register the built-in mutation condition types so their `type` ids are known before any
		// datapack/recipe parse populates the mutation managers in a later reload handler.
		forestry.core.genetics.mutations.MutationConditionTypes.registerBuiltins();

		// Register the built-in product types so the optional `type` key on species products (e.g. the
		// Patriotic bee's randomized firework) resolves before any species JSON parse or network sync.
		forestry.core.genetics.ProductTypes.registerBuiltins();

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

	public static void registerFarming() {
		FarmingRegistration registration = new FarmingRegistration();

		for (IForestryPlugin plugin : LOADED_PLUGINS) {
			try {
				plugin.registerFarming(registration);
			} catch (Throwable t) {
				throw new RuntimeException("An error was thrown by plugin " + plugin.id() + " during IForestryPlugin.registerFarming", t);
			}
		}

		// Defensive copy of fertilizers
		FarmingManager manager = new FarmingManager(new Object2IntOpenHashMap<>(registration.getFertilizers()), registration.buildFarmTypes());

		((ForestryApiImpl) IForestryApi.INSTANCE).setFarmingManager(manager);
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

	// Todo remove in 1.20 when FMLCommonSetupEvent throws exceptions again
	@Nullable
	@Deprecated
	private static RuntimeException asyncThrown = null;

	@Deprecated
	public static void registerAsyncException(IEventBus modBus) {
		modBus.addListener((FMLLoadCompleteEvent event) -> {
			if (asyncThrown != null) {
				throw asyncThrown;
			}
		});
	}

	public static void registerClient() {
		ClientRegistration registration = new ClientRegistration();

		for (IForestryPlugin plugin : LOADED_PLUGINS) {
			plugin.registerClient(consumer -> consumer.accept(registration));
		}

		// Bees
		// id-keyed: resolving a specific species happens at render time, so the (possibly
		// datapack-driven) species list is not needed here.
		IdentityHashMap<ILifeStage, ResourceLocation> defaultBeeModels = new IdentityHashMap<>();
		IdentityHashMap<ILifeStage, Map<ResourceLocation, ResourceLocation>> customBeeModels = new IdentityHashMap<>();

		for (ILifeStage stage : SpeciesUtil.BEE_TYPE.get().getLifeStages()) {
			ResourceLocation defaultModel = Objects.requireNonNull(registration.getDefaultBeeModel(stage), "IClientRegistration.setDefaultBeeModel has not been called for life stage " + stage.getSerializedName() + ", unable to resolve bee default model");
			defaultBeeModels.put(stage, defaultModel);
			customBeeModels.put(stage, registration.getBeeModels().getOrDefault(stage, Map.of()));
		}
		((ForestryClientApiImpl) IForestryClientApi.INSTANCE).setBeeManager(new BeeClientManager(defaultBeeModels, customBeeModels));

		// Trees
		// id-keyed: resolving a species happens at render time by id, so the (datapack-driven) species list is not
		// needed to build the sprite/model maps below.
		HashMap<ResourceLocation, ILeafSprite> spritesById = registration.getLeafSprites();
		HashMap<ResourceLocation, ILeafTint> tintsById = registration.getTints();
		HashMap<ResourceLocation, Pair<ResourceLocation, ResourceLocation>> modelsById = registration.getSaplingModels();

		// The escritoire-color tint fallback (for the ~40 built-in species that register no explicit client tint) is
		// applied lazily at render time in TreeClientManager#getTint from the species object itself, so no species-list
		// iteration is needed here and datapack-added species get the same fallback reloadably.

		// For any species id that has a leaf sprite but no explicit sapling model, synthesize the default-path pair
		// (removing the "tree_" prefix), exactly as the old per-species loop did.
		Map<ResourceLocation, Pair<ResourceLocation, ResourceLocation>> models = new HashMap<>(modelsById);
		for (ResourceLocation id : spritesById.keySet()) {
			models.computeIfAbsent(id, sid -> {
				String path = sid.getPath().replace("tree_", "");
				return Pair.of(
					ResourceLocation.fromNamespaceAndPath(sid.getNamespace(), "block/" + path + "_sapling"),
					ResourceLocation.fromNamespaceAndPath(sid.getNamespace(), "item/" + path + "_sapling")
				);
			});
		}

		((ForestryClientApiImpl) IForestryClientApi.INSTANCE).setTreeManager(new TreeClientManager(
			new HashMap<>(spritesById), new HashMap<>(tintsById), models
		));

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
