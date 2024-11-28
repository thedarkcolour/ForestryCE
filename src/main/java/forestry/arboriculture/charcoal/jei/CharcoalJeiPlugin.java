package forestry.arboriculture.charcoal.jei;

import net.minecraft.resources.ResourceLocation;

import forestry.api.ForestryConstants;
import forestry.api.arboriculture.ICharcoalManager;
import forestry.api.arboriculture.ICharcoalPileWall;
import forestry.api.arboriculture.TreeManager;
import forestry.api.modules.ForestryModuleIds;
import forestry.arboriculture.features.CharcoalBlocks;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

@JeiPlugin
public class CharcoalJeiPlugin implements IModPlugin {
	public static final RecipeType<ICharcoalPileWall> RECIPE_TYPE = RecipeType.create(ForestryConstants.MOD_ID, "charcoal.pile", ICharcoalPileWall.class);

	@Override
	public ResourceLocation getPluginUid() {
		return ForestryModuleIds.CHARCOAL;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
		registry.addRecipeCategories(new CharcoalPileWallCategory(guiHelper));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		ICharcoalManager charcoalManager = TreeManager.charcoalManager;
		if (charcoalManager == null) {
			return;
		}

		registration.addRecipes(RECIPE_TYPE, charcoalManager.getWalls());
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(CharcoalBlocks.LOG_PILE.stack(), RECIPE_TYPE);
	}
}
