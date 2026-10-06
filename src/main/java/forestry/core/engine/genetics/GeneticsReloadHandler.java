package forestry.core.engine.genetics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import forestry.Forestry;
import forestry.api.IForestryApi;
import forestry.api.core.genetics.IMutation;
import forestry.api.core.genetics.ISpecies;
import forestry.api.core.genetics.ISpeciesType;
import forestry.apiimpl.GeneticManager;
import forestry.core.features.GeneticsRecipeTypes;
import forestry.core.engine.genetics.mutations.Mutation;
import forestry.core.engine.genetics.mutations.MutationRecipe;
import forestry.core.platform.registration.FeatureRecipeType;

/**
 * Rebuilds the engine-level runtime genetics state from loaded data. The per-species-type rebuilds live with their
 * module: {@code ApicultureReloadHandler}, {@code ArboricultureReloadHandler} and
 * {@code LepidopterologyReloadHandler}.
 * <p>
 * {@link #rebuildTaxa} merges the datapack-loaded taxa onto the code-registered taxonomy.
 * <p>
 * {@link #rebuildMutations} rebuilds each species type's {@link MutationManager} from the loaded
 * {@link MutationRecipe}s. Fires on server datapack (re)load (via {@code AddReloadListenerEvent}) and on client
 * recipe sync (via {@code RecipesUpdatedEvent}), so the runtime mutation index always mirrors the active recipes for
 * both gameplay and JEI/analyzer display.
 * <p>
 * <b>Ordering matters:</b> mutation recipes resolve their species by looking them up (by id) in the live
 * {@code allSpecies} map at rebuild time, and the resulting {@link IMutation}s are indexed by species <em>object
 * identity</em> ({@link MutationManager} uses an {@code IdentityHashMap}). Species must therefore always be rebuilt
 * before mutations - every call site in this codebase does a module rebuild then {@code rebuildMutations}, in that
 * order, on the same thread/executor.
 * <p>
 * Safe to run with zero definitions/recipes: species/mutation indexes are simply left/set empty.
 */
public final class GeneticsReloadHandler {
	/**
	 * Merges the datapack-loaded taxa onto the code-registered taxonomy in the genetic manager. Must run before any
	 * species rebuild, because a species' genus is resolved to a taxon as it is projected.
	 */
	public static void rebuildTaxa(Collection<TaxonDefinition> taxa) {
		((GeneticManager) IForestryApi.INSTANCE.getGeneticManager()).applyDatapackTaxa(taxa);
	}

	public static void rebuildMutations(RecipeManager recipeManager) {
		for (ISpeciesType<?, ?> type : IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypes()) {
			rebuildOne(type, recipeManager);
		}
	}

	private static <S extends ISpecies<?>> void rebuildOne(ISpeciesType<S, ?> type, RecipeManager rm) {
		FeatureRecipeType<MutationRecipe> featureType = GeneticsRecipeTypes.forType(type.id());
		if (featureType == null) {
			return; // species type without a mutation recipe type (e.g. third-party)
		}
		// build id -> species lookup once
		Map<ResourceLocation, S> lookup = new HashMap<>();
		for (S species : type.getAllSpecies()) {
			lookup.put(species.id(), species);
		}
		ImmutableList.Builder<IMutation<S>> builder = ImmutableList.builder();
		for (RecipeHolder<MutationRecipe> holder : rm.getAllRecipesFor(featureType.type())) {
			Mutation<S> mutation = holder.value().toMutation(type, lookup::get);
			if (mutation != null) {
				builder.add(mutation);
			} else {
				Forestry.LOGGER.warn("Skipping mutation recipe {} (unknown or mismatched species)", holder.id());
			}
		}
		ImmutableList<IMutation<S>> mutations = builder.build();
		((SpeciesType<S, ?>) type).setMutations(new MutationManager<>(mutations));
		Forestry.LOGGER.debug("Loaded {} {} mutation recipes", mutations.size(), type.id());
	}
}
