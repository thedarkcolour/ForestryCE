package forestry.core.data;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import forestry.api.apiculture.ForestryBeeJubilances;
import forestry.api.apiculture.IBeeJubilance;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.plugin.IBeeSpeciesBuilder;
import forestry.apiculture.genetics.BeeSpeciesDefinition;
import forestry.apiculture.genetics.DefaultBeeJubilance;
import forestry.apiculture.genetics.HermitBeeJubilance;
import forestry.apiimpl.plugin.ApicultureRegistration;
import forestry.core.genetics.GeneticsReloadHandler;
import forestry.core.utils.SpeciesUtil;
import forestry.plugin.DefaultBeeSpecies;

/**
 * Generates {@code data/forestry/bee_species/*.json} for every built-in bee, read directly from the
 * {@code DefaultBeeSpecies} builders via {@link ApicultureRegistration#forEachSpeciesBuilder} - the same builders the
 * code-registration path uses, so the generated definitions are a faithful parallel artifact of the code-built
 * species (proven by {@code BeeSpeciesEquivalenceTest}).
 * <p>
 * This is a plain {@link DataProvider} rather than a {@code RecipeOutput}-based one: bee species are not recipes, and
 * ModKit's {@code DataHelper} has no generic JSON sink.
 */
public class BeeSpeciesProvider implements DataProvider {
	private final PackOutput.PathProvider pathProvider;
	private final CompletableFuture<HolderLookup.Provider> lookupProvider;

	public BeeSpeciesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "bee_species");
		this.lookupProvider = lookupProvider;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		return this.lookupProvider.thenCompose(provider -> {
			RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, provider);

			List<CompletableFuture<?>> futures = new ArrayList<>();
			buildDefinitions().forEach((id, def) -> futures.add(saveSpecies(cache, ops, id, def)));
			return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		});
	}

	/**
	 * Builds every built-in bee species definition straight from the {@code DefaultBeeSpecies} builders - the same
	 * definitions {@link #run} serializes to {@code bee_species/*.json}. Needs no registry access (unlike {@link
	 * #run}, which only needs {@link RegistryOps} to encode the result to JSON), so it can also be used to seed the
	 * live species type - see {@link #seedLiveSpeciesForDatagen()}.
	 */
	public static Map<ResourceLocation, BeeSpeciesDefinition> buildDefinitions() {
		IBeeSpeciesType type = SpeciesUtil.BEE_TYPE.get();
		ApicultureRegistration reg = new ApicultureRegistration(type);
		// DefaultBeeSpecies.register never calls registerBeeJubilance itself (it sets IBeeJubilance
		// *instances* directly on each builder), so this fresh registration needs the same companion
		// registrations DefaultForestryPlugin#registerApiculture makes, to invert instance -> id below.
		reg.registerBeeJubilance(ForestryBeeJubilances.DEFAULT, DefaultBeeJubilance.INSTANCE);
		reg.registerBeeJubilance(ForestryBeeJubilances.HERMIT, HermitBeeJubilance.INSTANCE);
		DefaultBeeSpecies.register(reg);

		Map<IBeeJubilance, ResourceLocation> jubilanceIds = new IdentityHashMap<>();
		reg.getJubilances().forEach((id, instance) -> jubilanceIds.put(instance, id));

		Map<ResourceLocation, BeeSpeciesDefinition> definitions = new LinkedHashMap<>();
		reg.forEachSpeciesBuilder((id, builder) -> definitions.put(id, buildDefinition(jubilanceIds, builder)));
		return definitions;
	}

	/**
	 * Populates the live bee species type directly from {@link #buildDefinitions()}, bypassing the datapack JSON
	 * round trip. Only for use by the standalone data generator ({@code Data#preDataGen}): a data-generator
	 * invocation never fires the {@code AddReloadListenerEvent}/datapack-reload cycle that loads species at real
	 * server start. The sole remaining consumer that needs live species in memory is the centrifuge recipe whose
	 * result is a concrete bee {@code ItemStack} ({@code BEE_TYPE.createStack(RELIC, DRONE)}) - loot references bees
	 * by id and needs no lookup. Species built here come from the identical {@code DefaultBeeSpecies} source the
	 * generated JSON itself is derived from, so this does not reintroduce a second, divergent species source.
	 */
	public static void seedLiveSpeciesForDatagen() {
		GeneticsReloadHandler.rebuildSpecies(buildDefinitions());
	}

	private static BeeSpeciesDefinition buildDefinition(Map<IBeeJubilance, ResourceLocation> jubilanceIds, IBeeSpeciesBuilder builder) {
		RecordingGenomeBuilder rec = new RecordingGenomeBuilder();
		builder.buildGenome(rec);

		ResourceLocation jubilanceId = jubilanceIds.getOrDefault(builder.getJubilance(), ForestryBeeJubilances.DEFAULT);

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
