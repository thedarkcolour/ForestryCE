package forestry.core.recipes.jei;

import forestry.api.ForestryConstants;
import forestry.api.fuels.RainmakerFuel;
import forestry.api.recipes.*;
import mezz.jei.api.recipe.RecipeType;

public class ForestryRecipeType {
	public static final RecipeType<ICarpenterRecipe> CARPENTER = create("carpenter", ICarpenterRecipe.class);
	public static final RecipeType<ICentrifugeRecipe> CENTRIFUGE = create("centrifuge", ICentrifugeRecipe.class);
	public static final RecipeType<IFabricatorRecipe> FABRICATOR = create("fabricator", IFabricatorRecipe.class);
	public static final RecipeType<IFermenterRecipe> FERMENTER = create("fermenter", IFermenterRecipe.class);
	public static final RecipeType<IMoistenerRecipe> MOISTENER = create("moistener", IMoistenerRecipe.class);
	public static final RecipeType<RainmakerFuel> RAINMAKER = create("rainmaker", RainmakerFuel.class);
	public static final RecipeType<ISqueezerRecipe> SQUEEZER = create("squeezer", ISqueezerRecipe.class);
	public static final RecipeType<IStillRecipe> STILL = create("still", IStillRecipe.class);

	private static <T> RecipeType<T> create(String uid, Class<? extends T> recipeClass) {
		return RecipeType.create(ForestryConstants.MOD_ID, uid, recipeClass);
	}
}
