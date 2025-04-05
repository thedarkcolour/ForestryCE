package forestry.core.recipes.jei;

import forestry.api.ForestryConstants;
import forestry.api.fuels.RainSubstrate;
import forestry.api.recipes.ICarpenterRecipe;
import forestry.api.recipes.ICentrifugeRecipe;
import forestry.api.recipes.IFabricatorRecipe;
import forestry.api.recipes.IFermenterRecipe;
import forestry.api.recipes.IMoistenerRecipe;
import forestry.api.recipes.ISqueezerRecipe;
import forestry.api.recipes.IStillRecipe;

import mezz.jei.api.recipe.RecipeType;

public class ForestryRecipeType {
	public static final RecipeType<ICarpenterRecipe> CARPENTER = create("carpenter", ICarpenterRecipe.class);
	public static final RecipeType<ICentrifugeRecipe> CENTRIFUGE = create("centrifuge", ICentrifugeRecipe.class);
	public static final RecipeType<IFabricatorRecipe> FABRICATOR = create("fabricator", IFabricatorRecipe.class);
	public static final RecipeType<IFermenterRecipe> FERMENTER = create("fermenter", IFermenterRecipe.class);
	public static final RecipeType<IMoistenerRecipe> MOISTENER = create("moistener", IMoistenerRecipe.class);
	public static final RecipeType<RainSubstrate> RAINMAKER = create("rainmaker", RainSubstrate.class);
	public static final RecipeType<ISqueezerRecipe> SQUEEZER = create("squeezer", ISqueezerRecipe.class);
	public static final RecipeType<IStillRecipe> STILL = create("still", IStillRecipe.class);

	private static <T> RecipeType<T> create(String uid, Class<? extends T> recipeClass) {
		return RecipeType.create(ForestryConstants.MOD_ID, uid, recipeClass);
	}
}
