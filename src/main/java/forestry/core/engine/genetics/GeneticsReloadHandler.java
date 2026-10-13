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

public final class GeneticsReloadHandler {
	// must run before any species rebuild, a species resolves its genus to a taxon when projected
	public static void rebuildTaxa(Collection<TaxonDefinition> taxa) {
		((GeneticManager) IForestryApi.INSTANCE.getGeneticManager()).applyDatapackTaxa(taxa);
	}

	// must run after every species rebuild, MutationManager is keyed by species identity
	public static void rebuildMutations(RecipeManager recipeManager) {
		for (ISpeciesType<?, ?> type : IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypes()) {
			rebuildOne(type, recipeManager);
		}
	}

	private static <S extends ISpecies<?>> void rebuildOne(ISpeciesType<S, ?> type, RecipeManager rm) {
		FeatureRecipeType<MutationRecipe> featureType = GeneticsRecipeTypes.forType(type.id());
		if (featureType == null) {
			// third-party species types have no mutation recipe type
			return;
		}
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
