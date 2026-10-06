package forestry.gametest;

import java.util.Map;

import com.mojang.serialization.JsonOps;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.ForestryBeeEffects;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.core.genetics.ForestryTaxa;
import forestry.api.core.genetics.IGenome;
import forestry.api.core.genetics.alleles.Allele;
import forestry.api.core.genetics.alleles.AlleleOverride;
import forestry.api.core.genetics.alleles.AllelePair;
import forestry.api.core.genetics.alleles.BeeChromosomes;
import forestry.api.core.genetics.alleles.ForestryAlleles;
import forestry.api.core.genetics.alleles.IKaryotype;
import forestry.apiculture.bees.BeeSpecies;
import forestry.apiculture.bees.genetics.BeeSpeciesDefinition;
import forestry.apiculture.bees.genetics.BeeSpeciesProjector;
import forestry.core.engine.genetics.MapGenomeBuilder;
import forestry.core.engine.genetics.GenomeCodecs;
import forestry.core.platform.util.SpeciesUtil;

/**
 * Behavioral oracle for one-sided and heterozygous genome overrides, the data-side counterpart of
 * {@link forestry.api.plugin.IGenomeBuilder#setActive} and {@link forestry.api.plugin.IGenomeBuilder#setInactive}.
 * Covers the three legs a datapack override travels: the JSON shapes {@link AlleleOverride} accepts, the projection
 * of those overrides onto a species' default genome, and the datagen shim that records the same three setters.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class GenomeOverrideTest {
	/**
	 * The single-allele shape written before overrides could name a side still parses, and still means "set both".
	 */
	@GameTest(template = "empty")
	public static void legacyJsonShapeStillSetsBothSides(GameTestHelper helper) {
		AlleleOverride<?> override = parseOne(helper, """
			{"forestry:bee_effect": {"value": "forestry:bee_effect_mycophilic"}}""", BeeChromosomes.EFFECT.id());
		if (override == null) {
			return;
		}

		Allele<ResourceLocation> expected = Allele.reference(ForestryBeeEffects.MYCOPHILIC);
		if (!expected.equals(override.active()) || !expected.equals(override.inactive())) {
			helper.fail("Expected both sides set to " + expected + " but got " + override);
			return;
		}
		helper.succeed();
	}

	/**
	 * Naming one side leaves the other unset, and naming both carries a separate dominance for each.
	 */
	@GameTest(template = "empty")
	public static void perSideJsonShapesParse(GameTestHelper helper) {
		AlleleOverride<?> activeOnly = parseOne(helper, """
			{"forestry:bee_effect": {"active": {"value": "forestry:bee_effect_mycophilic"}}}""", BeeChromosomes.EFFECT.id());
		if (activeOnly == null) {
			return;
		}
		if (!Allele.reference(ForestryBeeEffects.MYCOPHILIC).equals(activeOnly.active()) || activeOnly.inactive() != null) {
			helper.fail("Expected only the active side to be set but got " + activeOnly);
			return;
		}

		AlleleOverride<?> inactiveOnly = parseOne(helper, """
			{"forestry:fertility": {"inactive": {"value": 4}}}""", BeeChromosomes.FERTILITY.id());
		if (inactiveOnly == null) {
			return;
		}
		if (inactiveOnly.active() != null || !ForestryAlleles.FERTILITY_4.equals(inactiveOnly.inactive())) {
			helper.fail("Expected only the inactive side to be set but got " + inactiveOnly);
			return;
		}

		AlleleOverride<?> both = parseOne(helper, """
			{"forestry:fertility": {"active": {"value": 1, "dominant": true}, "inactive": {"value": 4}}}""", BeeChromosomes.FERTILITY.id());
		if (both == null) {
			return;
		}
		if (!ForestryAlleles.FERTILITY_1.equals(both.active()) || !ForestryAlleles.FERTILITY_4.equals(both.inactive())) {
			helper.fail("Expected a dominant 1 over a recessive 4 but got " + both);
			return;
		}
		helper.succeed();
	}

	/**
	 * An override that names only the inactive side must leave the active one at whatever the karyotype and taxon
	 * defaults produced, so the oracle projects the same species twice and compares.
	 */
	@GameTest(template = "empty")
	public static void oneSidedOverrideLeavesTheOtherSideAlone(GameTestHelper helper) {
		AllelePair<Integer> untouched = projectFertility(helper, "test_override_baseline", Map.of());
		if (untouched == null) {
			return;
		}
		AllelePair<Integer> overridden = projectFertility(helper, "test_override_one_sided",
			Map.of(BeeChromosomes.FERTILITY.id(), AlleleOverride.onlyInactive(ForestryAlleles.FERTILITY_10)));
		if (overridden == null) {
			return;
		}

		if (!untouched.active().equals(overridden.active())) {
			helper.fail("Expected the active allele to stay at its default " + untouched.active() + " but got " + overridden.active());
			return;
		}
		if (!ForestryAlleles.FERTILITY_10.equals(overridden.inactive())) {
			helper.fail("Expected the inactive allele to be " + ForestryAlleles.FERTILITY_10 + " but got " + overridden.inactive());
			return;
		}
		helper.succeed();
	}

	/**
	 * A heterozygous override lands verbatim: the projector keeps the authored sides rather than reordering them by
	 * dominance, so a recessive allele stays in the active slot.
	 */
	@GameTest(template = "empty")
	public static void heterozygousOverrideKeepsAuthoredSides(GameTestHelper helper) {
		AllelePair<Integer> pair = projectFertility(helper, "test_override_heterozygous",
			Map.of(BeeChromosomes.FERTILITY.id(), new AlleleOverride<>(ForestryAlleles.FERTILITY_4, ForestryAlleles.FERTILITY_1)));
		if (pair == null) {
			return;
		}

		if (!ForestryAlleles.FERTILITY_4.equals(pair.active())) {
			helper.fail("Expected the recessive 4 to stay active but got " + pair.active());
			return;
		}
		if (!ForestryAlleles.FERTILITY_1.equals(pair.inactive())) {
			helper.fail("Expected the dominant 1 to stay inactive but got " + pair.inactive());
			return;
		}
		helper.succeed();
	}

	/**
	 * A heterozygous reference chromosome takes each side's dominance from the referenced value, not from the
	 * placeholder dominance the id was serialized with.
	 */
	@GameTest(template = "empty")
	public static void heterozygousReferenceResolvesDominancePerSide(GameTestHelper helper) {
		IBeeSpeciesType type = SpeciesUtil.BEE_TYPE.get();
		BeeSpeciesDefinition def = TestSpeciesDefinitions.bee(ForestryTaxa.GENUS_HONEY, ForestryTaxa.SPECIES_FOREST)
			.genome(Map.of(BeeChromosomes.EFFECT.id(), new AlleleOverride<>(
				Allele.reference(ForestryBeeEffects.MYCOPHILIC), Allele.reference(ForestryBeeEffects.BEATIFIC))))
			.build();
		BeeSpecies species = BeeSpeciesProjector.project(type, ForestryConstants.forestry("test_override_reference"), def);
		if (species == null) {
			helper.fail("Projection returned null for a valid definition");
			return;
		}

		AllelePair<ResourceLocation> pair = species.getDefaultGenome().getAllelePair(BeeChromosomes.EFFECT);
		if (!ForestryBeeEffects.MYCOPHILIC.equals(pair.active().value()) || !ForestryBeeEffects.BEATIFIC.equals(pair.inactive().value())) {
			helper.fail("Expected mycophilic over beatific but got " + pair);
			return;
		}

		IChromosomeDominance expected = new IChromosomeDominance(
			BeeChromosomes.EFFECT.resolver().isDominant(ForestryBeeEffects.MYCOPHILIC),
			BeeChromosomes.EFFECT.resolver().isDominant(ForestryBeeEffects.BEATIFIC));
		if (pair.active().dominant() != expected.active() || pair.inactive().dominant() != expected.inactive()) {
			helper.fail("Expected dominance " + expected + " resolved from the referenced effects but got " + pair);
			return;
		}
		helper.succeed();
	}

	/**
	 * The datagen shim records all three setters, so a genome closure that names one side generates the one-sided
	 * override the projector reads back.
	 */
	@GameTest(template = "empty")
	public static void datagenShimRecordsEverySetter(GameTestHelper helper) {
		MapGenomeBuilder recorder = new MapGenomeBuilder();
		recorder.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_FASTEST);
		recorder.setActive(BeeChromosomes.FERTILITY, ForestryAlleles.FERTILITY_4);
		recorder.setInactive(BeeChromosomes.FERTILITY, ForestryAlleles.FERTILITY_1);
		recorder.setInactive(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORT);

		Map<ResourceLocation, AlleleOverride<?>> expected = Map.of(
			BeeChromosomes.SPEED.id(), AlleleOverride.both(ForestryAlleles.SPEED_FASTEST),
			BeeChromosomes.FERTILITY.id(), new AlleleOverride<>(ForestryAlleles.FERTILITY_4, ForestryAlleles.FERTILITY_1),
			BeeChromosomes.LIFESPAN.id(), AlleleOverride.onlyInactive(ForestryAlleles.LIFESPAN_SHORT)
		);
		if (!expected.equals(recorder.overrides)) {
			helper.fail("Expected recorded overrides " + expected + " but got " + recorder.overrides);
			return;
		}
		helper.succeed();
	}

	/**
	 * The encoder writes the documented shapes back out: a bare allele when both sides match, and named sides
	 * carrying a whole allele each when they do not. Uses only integer-valued and reference chromosomes: a float
	 * allele widens to a different double than the same literal parses to, which no encoder can fix.
	 */
	@GameTest(template = "empty")
	public static void encodedShapesMatchTheDocumentedForm(GameTestHelper helper) {
		String expected = """
			{
				"forestry:bee_effect": {"value": "forestry:bee_effect_mycophilic"},
				"forestry:fertility": {"active": {"value": 1, "dominant": true}, "inactive": {"value": 4}},
				"forestry:lifespan": {"inactive": {"value": 30, "dominant": true}}
			}""";
		Map<ResourceLocation, AlleleOverride<?>> overrides = Map.of(
			BeeChromosomes.EFFECT.id(), AlleleOverride.both(Allele.reference(ForestryBeeEffects.MYCOPHILIC)),
			BeeChromosomes.FERTILITY.id(), new AlleleOverride<>(ForestryAlleles.FERTILITY_1, ForestryAlleles.FERTILITY_4),
			BeeChromosomes.LIFESPAN.id(), AlleleOverride.onlyInactive(ForestryAlleles.LIFESPAN_SHORT)
		);

		IKaryotype karyotype = SpeciesUtil.BEE_TYPE.get().getKaryotype();
		JsonElement encoded = GenomeCodecs.overrideMapCodec(karyotype).encodeStart(JsonOps.INSTANCE, overrides).getOrThrow();
		if (!JsonParser.parseString(expected).equals(encoded)) {
			helper.fail("Expected " + expected + " but encoded " + encoded);
			return;
		}
		helper.succeed();
	}

	private record IChromosomeDominance(boolean active, boolean inactive) {
	}

	// Parses a whole override map from JSON and returns the one entry the caller names, failing the test if the
	// map does not parse or does not hold it.
	private static AlleleOverride<?> parseOne(GameTestHelper helper, String json, ResourceLocation chromosome) {
		IKaryotype karyotype = SpeciesUtil.BEE_TYPE.get().getKaryotype();
		Map<ResourceLocation, AlleleOverride<?>> parsed = GenomeCodecs.overrideMapCodec(karyotype)
			.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
			.getOrThrow();

		AlleleOverride<?> override = parsed.get(chromosome);
		if (override == null) {
			helper.fail("Parsed override map has no entry for " + chromosome + ": " + parsed);
		}
		return override;
	}

	// Projects a bee species carrying the given overrides and returns its default fertility pair.
	private static AllelePair<Integer> projectFertility(GameTestHelper helper, String id, Map<ResourceLocation, AlleleOverride<?>> genome) {
		IBeeSpeciesType type = SpeciesUtil.BEE_TYPE.get();
		BeeSpeciesDefinition def = TestSpeciesDefinitions.bee(ForestryTaxa.GENUS_HONEY, ForestryTaxa.SPECIES_FOREST)
			.genome(genome)
			.build();
		BeeSpecies species = BeeSpeciesProjector.project(type, ForestryConstants.forestry(id), def);
		if (species == null) {
			helper.fail("Projection returned null for a valid definition");
			return null;
		}
		IGenome defaultGenome = species.getDefaultGenome();
		return defaultGenome.getAllelePair(BeeChromosomes.FERTILITY);
	}
}
