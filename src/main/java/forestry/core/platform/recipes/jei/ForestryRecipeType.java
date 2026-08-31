package forestry.core.platform.recipes.jei;

import forestry.api.ForestryConstants;
import forestry.api.core.machines.*;
import mezz.jei.api.recipe.RecipeType;
import forestry.core.content.machines.recipes.jei.rainmaker.RainmakerJeiRecipe;

public class ForestryRecipeType {
	public static final RecipeType<ICarpenterRecipe> CARPENTER = create("carpenter", ICarpenterRecipe.class);
	public static final RecipeType<ICentrifugeRecipe> CENTRIFUGE = create("centrifuge", ICentrifugeRecipe.class);
	public static final RecipeType<IFabricatorRecipe> FABRICATOR = create("fabricator", IFabricatorRecipe.class);
	public static final RecipeType<IFermenterRecipe> FERMENTER = create("fermenter", IFermenterRecipe.class);
	public static final RecipeType<IMoistenerRecipe> MOISTENER = create("moistener", IMoistenerRecipe.class);
	public static final RecipeType<RainmakerJeiRecipe> RAINMAKER = create("rainmaker", RainmakerJeiRecipe.class);
	public static final RecipeType<ISmelterRecipe> SMELTER = create("smelter", ISmelterRecipe.class);
	public static final RecipeType<ISqueezerRecipe> SQUEEZER = create("squeezer", ISqueezerRecipe.class);
	public static final RecipeType<IStillRecipe> STILL = create("still", IStillRecipe.class);

	private static <T> RecipeType<T> create(String uid, Class<? extends T> recipeClass) {
		return RecipeType.create(ForestryConstants.MOD_ID, uid, recipeClass);
	}
}
