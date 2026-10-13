package forestry.gametest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import forestry.api.ForestryConstants;
import forestry.api.core.genetics.alleles.ButterflyChromosomes;
import forestry.api.core.genetics.alleles.IChromosome;
import forestry.api.lepidopterology.ForestryButterflyEffects;
import forestry.api.lepidopterology.ForestryButterflySpecies;
import forestry.api.lepidopterology.ForestryCocoons;
import forestry.api.lepidopterology.IButterflyCocoon;
import forestry.api.lepidopterology.IButterflyEffect;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.api.lepidopterology.genetics.IButterflySpeciesType;
import forestry.lepidopterology.butterflies.genetics.ButterflySpeciesProvider;
import forestry.core.engine.genetics.GeneticsReloadHandler;
import forestry.core.engine.genetics.SpeciesType;
import forestry.core.platform.util.SpeciesUtil;
import forestry.lepidopterology.butterflies.genetics.ButterflySpeciesDefinition;
import forestry.lepidopterology.butterflies.genetics.LepidopterologyReloadHandler;

/**
 * Behavioral oracle for {@link ButterflyChromosomes#SPECIES}'s fail-soft resolver: a saved individual can reference a
 * butterfly species id that a datapack has since removed, and resolving it must fall back to the default species
 * instead of throwing (mirrors {@code TreeSpeciesFallbackTest}'s resolver coverage).
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class ButterflySpeciesFallbackTest {
	@GameTest(template = "empty")
	@SuppressWarnings("unchecked")
	public static void speciesResolverFallsSoft(GameTestHelper helper) {
		IButterflySpeciesType type = SpeciesUtil.BUTTERFLY_TYPE.get();

		IChromosome.IReferenceResolver<IButterflySpecies> resolver =
			(IChromosome.IReferenceResolver<IButterflySpecies>) ButterflyChromosomes.SPECIES.resolver();
		ResourceLocation unknownId = ForestryConstants.forestry("does_not_exist");
		IButterflySpecies resolved = resolver.get(unknownId);

		if (resolved != type.getDefaultSpecies()) {
			helper.fail("Expected unknown butterfly species id to resolve to the default species");
			return;
		}
		helper.succeed();
	}

	/**
	 * Companion to {@link #speciesResolverFallsSoft}, but for the {@link ButterflyChromosomes#COCOON}/{@link
	 * ButterflyChromosomes#EFFECT} reference chromosomes. Unlike {@code SPECIES}, the cocoon/effect registries are
	 * code-registered and never touched by a datapack reload of {@code butterfly_species}, but a datapack-authored
	 * species (any Stage 5+ JSON definition) can still set a {@code cocoon}/{@code butterfly_effect} genome override
	 * to an id nothing registered - that must fall back to {@link ForestryCocoons#DEFAULT}/{@link
	 * ForestryButterflyEffects#NONE} instead of crashing cocoon maturation ({@code TileCocoon}) or the analyzer.
	 */
	@GameTest(template = "empty")
	@SuppressWarnings("unchecked")
	public static void cocoonAndEffectResolversFallSoft(GameTestHelper helper) {
		IButterflySpeciesType type = SpeciesUtil.BUTTERFLY_TYPE.get();
		ResourceLocation unknownId = ForestryConstants.forestry("does_not_exist");

		IChromosome.IReferenceResolver<IButterflyCocoon> cocoonResolver =
			(IChromosome.IReferenceResolver<IButterflyCocoon>) ButterflyChromosomes.COCOON.resolver();
		IButterflyCocoon resolvedCocoon = cocoonResolver.get(unknownId);
		if (resolvedCocoon != type.getCocoonSafe(ForestryCocoons.DEFAULT)) {
			helper.fail("Expected unknown cocoon id to resolve to the default cocoon");
			return;
		}

		IChromosome.IReferenceResolver<IButterflyEffect> effectResolver =
			(IChromosome.IReferenceResolver<IButterflyEffect>) ButterflyChromosomes.EFFECT.resolver();
		IButterflyEffect resolvedEffect = effectResolver.get(unknownId);
		if (resolvedEffect != type.getButterflyEffectSafe(ForestryButterflyEffects.NONE)) {
			helper.fail("Expected unknown butterfly_effect id to resolve to the default (no-op) effect");
			return;
		}

		helper.succeed();
	}

	/**
	 * A def map missing a built-in species (as if a datapack had deleted its JSON file) must be skipped by
	 * {@link LepidopterologyReloadHandler#rebuildButterflySpecies} without crashing, must not disturb the other built-ins,
	 * and reading the now-removed id back through the fail-soft {@link ButterflyChromosomes#SPECIES} resolver must
	 * return the default species rather than throw (the exact scenario a saved/synced stack referencing that id hits
	 * post-removal).
	 * <p>
	 * Butterflies have no per-species code-side bindings to omit (unlike trees' {@code TreeBlockBindings}), so the
	 * "removed by a datapack" case is simulated directly by dropping one built-in definition from the map passed to
	 * {@code rebuildButterflySpecies}, rather than authoring a binding-less phantom definition.
	 * <p>
	 * Mutates and restores the live {@code BUTTERFLY_TYPE} species map exactly like {@code TreeSpeciesFallbackTest}'s
	 * {@code bindinglessDefinitionSkippedNoCrash} (snapshot before, {@code setSpecies(snapshot)} + {@code
	 * rebuildMutations} in a {@code finally} block), so later GameTests in the same server session (notably {@code
	 * MutationRecipeTest}'s butterfly mutation assertions) are unaffected.
	 */
	@GameTest(template = "empty")
	@SuppressWarnings("unchecked")
	public static void bindinglessDefinitionSkippedNoCrash(GameTestHelper helper) {
		IButterflySpeciesType type = SpeciesUtil.BUTTERFLY_TYPE.get();

		// Snapshot the live, full species map before mutating it, so it can be restored afterwards.
		ImmutableMap<ResourceLocation, IButterflySpecies> snapshot = ImmutableMap.copyOf(
			type.getAllSpeciesIds().stream().collect(Collectors.toMap(id -> id, type::getSpecies))
		);

		// GLASSWING is a plain built-in (not MONARCH, the karyotype's default species), so removing it lets us
		// distinguish "the removed species is gone" from "the default species is gone".
		ResourceLocation removedId = ForestryButterflySpecies.GLASSWING;
		Map<ResourceLocation, ButterflySpeciesDefinition> defs = new LinkedHashMap<>(ButterflySpeciesProvider.buildDefinitions());
		if (defs.remove(removedId) == null) {
			helper.fail("Expected " + removedId + " to be a built-in butterfly species definition");
			return;
		}

		try {
			LepidopterologyReloadHandler.rebuildButterflySpecies(defs);

			if (type.getSpeciesSafe(removedId) != null) {
				helper.fail("Expected the removed butterfly species to be absent from the live map");
				return;
			}
			if (type.getAllSpecies().isEmpty()) {
				helper.fail("Expected the real built-ins to still load");
				return;
			}

			// The fail-soft path a saved/synced stack referencing the now-removed id actually goes through.
			IChromosome.IReferenceResolver<IButterflySpecies> resolver =
				(IChromosome.IReferenceResolver<IButterflySpecies>) ButterflyChromosomes.SPECIES.resolver();
			IButterflySpecies resolved = resolver.get(removedId);
			if (resolved != type.getDefaultSpecies()) {
				helper.fail("Expected the removed species id to resolve to the default species instead of throwing");
				return;
			}
		} finally {
			// Restore the live state so later tests in this same server session (e.g. MutationRecipeTest's butterfly
			// mutation assertions) still see the full built-in butterfly species set, and re-pair the mutation index
			// with the restored species (rebuildMutations rebuilds all species types, mirroring TreeSpeciesFallbackTest).
			((SpeciesType<IButterflySpecies, ?>) type).setSpecies(snapshot);
			GeneticsReloadHandler.rebuildMutations(helper.getLevel().getServer().getRecipeManager());
		}

		helper.succeed();
	}
}
