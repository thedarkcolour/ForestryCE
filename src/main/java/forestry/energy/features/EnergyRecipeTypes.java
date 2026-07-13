package forestry.energy.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.api.recipes.IBiogasFuel;
import forestry.api.recipes.IPeatFuel;
import forestry.energy.recipes.BiogasFuelRecipe;
import forestry.energy.recipes.PeatFuelRecipe;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.FeatureRecipeType;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class EnergyRecipeTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.ENERGY);

	public static final FeatureRecipeType<IBiogasFuel> BIOGAS_FUEL = REGISTRY.recipeType("biogas_fuel", BiogasFuelRecipe.Serializer::new);
	public static final FeatureRecipeType<IPeatFuel> PEAT_FUEL = REGISTRY.recipeType("peat_fuel", PeatFuelRecipe.Serializer::new);
}
