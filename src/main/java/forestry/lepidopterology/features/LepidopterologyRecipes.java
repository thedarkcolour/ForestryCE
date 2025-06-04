package forestry.lepidopterology.features;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

import forestry.api.modules.ForestryModuleIds;
import forestry.lepidopterology.recipe.ButterflyMatingRecipe;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

import net.neoforged.neoforge.registries.DeferredRegister;

@FeatureProvider
public class LepidopterologyRecipes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.LEPIDOPTEROLOGY);
	private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = REGISTRY.getRegistry(Registries.RECIPE_SERIALIZER);

	public static final Holder<RecipeSerializer<?>> MATING_SERIALIZER = SERIALIZERS.register("butterfly_mating", () -> new SimpleCraftingRecipeSerializer<>(ButterflyMatingRecipe::new));
}
