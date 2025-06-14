package forestry.factory.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.api.recipes.*;
import forestry.apiculture.recipes.HygroregulatorRecipe;
import forestry.factory.recipes.*;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.FeatureRecipeType;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class FactoryRecipeTypes {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.FACTORY);

	public static final FeatureRecipeType<ICarpenterRecipe> CARPENTER = REGISTRY.recipeType("carpenter", CarpenterRecipe.Serializer::new);
	public static final FeatureRecipeType<ICentrifugeRecipe> CENTRIFUGE = REGISTRY.recipeType("centrifuge", CentrifugeRecipe.Serializer::new);
	public static final FeatureRecipeType<IFabricatorRecipe> FABRICATOR = REGISTRY.recipeType("fabricator", FabricatorRecipe.Serializer::new);
	public static final FeatureRecipeType<FabricatorSmeltingRecipe> FABRICATOR_SMELTING = REGISTRY.recipeType("fabricator_smelting", FabricatorSmeltingRecipe.Serializer::new);
	public static final FeatureRecipeType<IFermenterRecipe> FERMENTER = REGISTRY.recipeType("fermenter", FermenterRecipe.Serializer::new);
	public static final FeatureRecipeType<IHygroregulatorRecipe> HYGROREGULATOR = REGISTRY.recipeType("hygroregulator", HygroregulatorRecipe.Serializer::new);
	public static final FeatureRecipeType<IMoistenerRecipe> MOISTENER = REGISTRY.recipeType("moistener", MoistenerRecipe.Serializer::new);
	public static final FeatureRecipeType<ISqueezerRecipe> SQUEEZER = REGISTRY.recipeType("squeezer", SqueezerRecipe.Serializer::new);
	public static final FeatureRecipeType<ISqueezerContainerRecipe> SQUEEZER_CONTAINER = REGISTRY.recipeType("squeezer_container", SqueezerContainerRecipe.Serializer::new);
	public static final FeatureRecipeType<IStillRecipe> STILL = REGISTRY.recipeType("still", StillRecipe.Serializer::new);
}
