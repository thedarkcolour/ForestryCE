package forestry.lepidopterology.features;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import forestry.api.modules.ForestryModuleIds;
import forestry.lepidopterology.recipe.ButterflyMatingRecipe;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class LepidopterologyRecipes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.LEPIDOPTEROLOGY);
	private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = REGISTRY.getRegistry(Registries.RECIPE_SERIALIZER);

	public static final RegistryObject<SimpleCraftingRecipeSerializer<?>> MATING_SERIALIZER = SERIALIZERS.register("butterfly_mating", () -> new SimpleCraftingRecipeSerializer<>(ButterflyMatingRecipe::new));
}
