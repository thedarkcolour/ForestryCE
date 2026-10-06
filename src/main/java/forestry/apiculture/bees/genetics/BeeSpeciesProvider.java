package forestry.apiculture.bees.genetics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.plugin.IApicultureRegistration;
import forestry.api.plugin.IBeeSpeciesBuilder;
import forestry.apiculture.plugin.ApicultureRegistration;
import forestry.apiculture.plugin.DefaultBeeSpecies;
import forestry.core.engine.genetics.MapGenomeBuilder;
import forestry.core.platform.util.SpeciesUtil;

// not in a data package, the jar tasks exclude those and addons reach this through IBeeSpeciesType
public class BeeSpeciesProvider implements DataProvider {
	private final PackOutput.PathProvider pathProvider;
	private final CompletableFuture<HolderLookup.Provider> lookupProvider;
	private final IBeeSpeciesType type;
	private final Consumer<IApicultureRegistration> species;

	public BeeSpeciesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, IBeeSpeciesType type, Consumer<IApicultureRegistration> species) {
		this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "bee_species");
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

	public static Map<ResourceLocation, BeeSpeciesDefinition> buildDefinitions(IBeeSpeciesType type, Consumer<IApicultureRegistration> species) {
		ApicultureRegistration reg = new ApicultureRegistration(type);
		species.accept(reg);

		Set<ResourceLocation> actionJubilances = reg.getJubilances().keySet();
		Map<ResourceLocation, BeeSpeciesDefinition> definitions = new LinkedHashMap<>();
		reg.forEachSpeciesBuilder((id, builder) -> definitions.put(id, buildDefinition(type, actionJubilances, id, builder)));
		return definitions;
	}

	public static Map<ResourceLocation, BeeSpeciesDefinition> buildDefinitions() {
		return buildDefinitions(SpeciesUtil.BEE_TYPE.get(), DefaultBeeSpecies::register);
	}

	// a data generator run never fires the datapack reload that loads species
	public static void seedLiveSpeciesForDatagen() {
		ApicultureReloadHandler.rebuildSpecies(buildDefinitions());
	}

	private static BeeSpeciesDefinition buildDefinition(IBeeSpeciesType type, Set<ResourceLocation> actionJubilances, ResourceLocation id, IBeeSpeciesBuilder builder) {
		MapGenomeBuilder rec = new MapGenomeBuilder();
		builder.buildGenome(rec);

		ResourceLocation jubilanceId = builder.getJubilance();
		if (type.getJubilanceSafe(jubilanceId) == null && !actionJubilances.contains(jubilanceId)) {
			throw new IllegalStateException("Bee species " + id + " uses a jubilance that is not registered: " + jubilanceId);
		}

		return new BeeSpeciesDefinition(
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
			builder.getBody(),
			builder.getStripes(),
			builder.getOutline(),
			builder.buildProducts(),
			builder.buildSpecialties(),
			jubilanceId,
			rec.overrides
		);
	}

	private CompletableFuture<?> saveSpecies(CachedOutput cache, RegistryOps<JsonElement> ops, ResourceLocation id, BeeSpeciesDefinition def) {
		JsonElement json = BeeSpeciesDefinition.codec().encodeStart(ops, def).getOrThrow();
		return DataProvider.saveStable(cache, json, this.pathProvider.json(id));
	}

	@Override
	public String getName() {
		return "Forestry Bee Species";
	}
}
