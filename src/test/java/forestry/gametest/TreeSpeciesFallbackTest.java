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
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.core.genetics.alleles.IChromosome;
import forestry.api.core.genetics.alleles.TreeChromosomes;
import forestry.arboriculture.trees.genetics.ArboricultureReloadHandler;
import forestry.arboriculture.trees.genetics.TreeSpeciesDefinition;
import forestry.arboriculture.trees.genetics.TreeSpeciesProvider;
import forestry.core.engine.genetics.GeneticsReloadHandler;
import forestry.core.engine.genetics.SpeciesType;
import forestry.core.platform.util.SpeciesUtil;

/**
 * Behavioral oracle for {@link TreeChromosomes#SPECIES}'s fail-soft resolver: a saved individual can reference a
 * tree species id that a datapack has since removed, and resolving it must fall back to the default species
 * instead of throwing (mirrors {@code SpeciesFallbackTest}'s bee coverage).
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class TreeSpeciesFallbackTest {
	@GameTest(template = "empty")
	@SuppressWarnings("unchecked")
	public static void unknownSpeciesIdResolvesToDefault(GameTestHelper helper) {
		ITreeSpeciesType type = SpeciesUtil.TREE_TYPE.get();

		IChromosome.IReferenceResolver<ITreeSpecies> resolver =
			(IChromosome.IReferenceResolver<ITreeSpecies>) TreeChromosomes.SPECIES.resolver();
		ResourceLocation unknownId = ForestryConstants.forestry("does_not_exist");
		ITreeSpecies resolved = resolver.get(unknownId);

		if (resolved != type.getDefaultSpecies()) {
			helper.fail("Expected unknown tree species id to resolve to the default species");
			return;
		}
		helper.succeed();
	}

	/**
	 * A definition referencing a species id with no code-side {@link forestry.arboriculture.trees.genetics.TreeBlockBindings}
	 * (no {@code ArboricultureRegistration} builder was ever registered for it - a "phantom" datapack-only species)
	 * must be skipped by {@link ArboricultureReloadHandler#rebuildTreeSpecies} without crashing, and must not disturb the
	 * real built-in species alongside it.
	 * <p>
	 * Mutates and restores the live {@code TREE_TYPE} species map exactly like {@code TreeSpeciesReloadTest}'s tests
	 * (snapshot before, {@code setSpecies(snapshot)} + {@code rebuildMutations} in a {@code finally} block), so later
	 * GameTests in the same server session (notably {@code MutationRecipeTest}'s tree mutation assertions) are
	 * unaffected.
	 */
	@GameTest(template = "empty")
	@SuppressWarnings("unchecked")
	public static void bindinglessDefinitionSkippedNoCrash(GameTestHelper helper) {
		ITreeSpeciesType type = SpeciesUtil.TREE_TYPE.get();

		// Snapshot the live, full species map before mutating it, so it can be restored afterwards.
		ImmutableMap<ResourceLocation, ITreeSpecies> snapshot = ImmutableMap.copyOf(
			type.getAllSpeciesIds().stream().collect(Collectors.toMap(id -> id, type::getSpecies))
		);

		Map<ResourceLocation, TreeSpeciesDefinition> defs = new LinkedHashMap<>(TreeSpeciesProvider.buildDefinitions());
		ResourceLocation phantomId = ForestryConstants.forestry("phantom_no_bindings");
		defs.put(phantomId, TestSpeciesDefinitions.tree("Quercus", "phantom").build());

		try {
			ArboricultureReloadHandler.rebuildTreeSpecies(defs);

			if (type.getSpeciesSafe(phantomId) != null) {
				helper.fail("Expected the binding-less phantom species to be skipped");
				return;
			}
			if (type.getAllSpecies().isEmpty()) {
				helper.fail("Expected the real built-ins to still load");
				return;
			}
		} finally {
			// Restore the live state so later tests in this same server session (e.g. MutationRecipeTest's tree
			// mutation assertions) still see the full built-in tree species set, and re-pair the mutation index with
			// the restored species (rebuildMutations rebuilds all species types, mirroring TreeSpeciesReloadTest).
			((SpeciesType<ITreeSpecies, ?>) type).setSpecies(snapshot);
			GeneticsReloadHandler.rebuildMutations(helper.getLevel().getServer().getRecipeManager());
		}

		helper.succeed();
	}
}
