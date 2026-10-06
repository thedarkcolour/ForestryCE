package forestry.arboriculture.trees.genetics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.plugin.IArboricultureRegistration;
import forestry.api.plugin.ITreeSpeciesBuilder;
import forestry.arboriculture.plugin.ArboricultureRegistration;
import forestry.arboriculture.plugin.DefaultTreeSpecies;
import forestry.core.engine.genetics.MapGenomeBuilder;
import forestry.core.platform.util.SpeciesUtil;

// not in a data package, the jar tasks exclude those and addons reach this through ITreeSpeciesType
public class TreeSpeciesProvider implements DataProvider {
	private final PackOutput.PathProvider pathProvider;
	private final CompletableFuture<HolderLookup.Provider> lookupProvider;
	private final ITreeSpeciesType type;
	private final Consumer<IArboricultureRegistration> species;

	public TreeSpeciesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ITreeSpeciesType type, Consumer<IArboricultureRegistration> species) {
		this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tree_species");
		this.lookupProvider = lookupProvider;
		this.type = type;
		this.species = species;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		return this.lookupProvider.thenCompose(provider -> {
			RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, provider);

			List<CompletableFuture<?>> futures = new ArrayList<>();
			buildDefinitions(this.type, this.species).forEach((id, def) -> futures.add(saveSpecies(cache, ops, id, def)));
			return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		});
	}

	public static Map<ResourceLocation, TreeSpeciesDefinition> buildDefinitions(ITreeSpeciesType type, Consumer<IArboricultureRegistration> species) {
		ArboricultureRegistration reg = new ArboricultureRegistration(type);
		species.accept(reg);

		Map<ResourceLocation, TreeSpeciesDefinition> definitions = new LinkedHashMap<>();
		reg.forEachSpeciesBuilder((id, builder) -> definitions.put(id, buildDefinition(builder)));
		return definitions;
	}

	public static Map<ResourceLocation, TreeSpeciesDefinition> buildDefinitions() {
		return buildDefinitions(SpeciesUtil.TREE_TYPE.get(), DefaultTreeSpecies::register);
	}

	// a data generator run never fires the datapack reload that loads species
	public static void seedLiveSpeciesForDatagen() {
		ArboricultureReloadHandler.rebuildTreeSpecies(buildDefinitions());
	}

	private static TreeSpeciesDefinition buildDefinition(ITreeSpeciesBuilder builder) {
		MapGenomeBuilder rec = new MapGenomeBuilder();
		builder.buildGenome(rec);

		return new TreeSpeciesDefinition(
			builder.getGenus(),
			builder.getSpecies(),
			builder.isDominant(),
			builder.hasGlint(),
			builder.isSecret(),
			builder.getComplexity(),
			builder.getAuthority(),
			builder.getEscritoireColor(),
			builder.getTemperature(),
			builder.getHumidity(),
			builder.getRarity(),
			rec.overrides
		);
	}

	private CompletableFuture<?> saveSpecies(CachedOutput cache, RegistryOps<JsonElement> ops, ResourceLocation id, TreeSpeciesDefinition def) {
		JsonElement json = TreeSpeciesDefinition.codec().encodeStart(ops, def).getOrThrow();
		return DataProvider.saveStable(cache, json, this.pathProvider.json(id));
	}

	@Override
	public String getName() {
		return "Forestry Tree Species";
	}
}
