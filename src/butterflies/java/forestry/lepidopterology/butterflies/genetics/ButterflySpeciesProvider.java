package forestry.lepidopterology.butterflies.genetics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

import forestry.api.lepidopterology.genetics.IButterflySpeciesType;
import forestry.api.plugin.IButterflySpeciesBuilder;
import forestry.api.plugin.ILepidopterologyRegistration;
import forestry.core.engine.genetics.MapGenomeBuilder;
import forestry.core.platform.util.SpeciesUtil;
import forestry.lepidopterology.plugin.DefaultButterflySpecies;
import forestry.lepidopterology.plugin.LepidopterologyRegistration;

// not in a data package, the jar tasks exclude those and addons reach this through IButterflySpeciesType
public class ButterflySpeciesProvider implements DataProvider {
	private final PackOutput.PathProvider pathProvider;
	private final CompletableFuture<HolderLookup.Provider> lookupProvider;
	private final IButterflySpeciesType type;
	private final Consumer<ILepidopterologyRegistration> species;

	public ButterflySpeciesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, IButterflySpeciesType type, Consumer<ILepidopterologyRegistration> species) {
		this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "butterfly_species");
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

	public static Map<ResourceLocation, ButterflySpeciesDefinition> buildDefinitions(IButterflySpeciesType type, Consumer<ILepidopterologyRegistration> species) {
		LepidopterologyRegistration reg = new LepidopterologyRegistration(type);
		species.accept(reg);

		Map<ResourceLocation, ButterflySpeciesDefinition> definitions = new LinkedHashMap<>();
		reg.forEachSpeciesBuilder((id, builder) -> definitions.put(id, buildDefinition(builder)));
		return definitions;
	}

	public static Map<ResourceLocation, ButterflySpeciesDefinition> buildDefinitions() {
		return buildDefinitions(SpeciesUtil.BUTTERFLY_TYPE.get(), DefaultButterflySpecies::register);
	}

	// a data generator run never fires the datapack reload that loads species
	public static void seedLiveSpeciesForDatagen() {
		LepidopterologyReloadHandler.rebuildButterflySpecies(buildDefinitions());
	}

	private static ButterflySpeciesDefinition buildDefinition(IButterflySpeciesBuilder builder) {
		MapGenomeBuilder rec = new MapGenomeBuilder();
		builder.buildGenome(rec);

		return new ButterflySpeciesDefinition(
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
			builder.isNocturnal(),
			builder.isMoth(),
			builder.getRarity(),
			builder.getFlightDistance(),
			builder.getSerumColor(),
			Optional.ofNullable(builder.getSpawnBiomes()),
			builder.buildProducts(),
			builder.buildCaterpillarProducts(),
			rec.overrides
		);
	}

	private CompletableFuture<?> saveSpecies(CachedOutput cache, RegistryOps<JsonElement> ops, ResourceLocation id, ButterflySpeciesDefinition def) {
		JsonElement json = ButterflySpeciesDefinition.codec().encodeStart(ops, def).getOrThrow();
		return DataProvider.saveStable(cache, json, this.pathProvider.json(id));
	}

	@Override
	public String getName() {
		return "Forestry Butterfly Species";
	}
}
