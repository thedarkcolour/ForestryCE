package forestry.arboriculture.compat;

import forestry.api.ForestryConstants;
import forestry.api.IForestryApi;
import forestry.api.arboriculture.ICharcoalPileWall;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.api.modules.ForestryModuleIds;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.features.CharcoalBlocks;
import forestry.core.utils.JeiUtil;
import forestry.core.utils.SpeciesUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class ArboricultureJeiPlugin implements IModPlugin {
	public static final RecipeType<ICharcoalPileWall> CHARCOAL_PILE = RecipeType.create(ForestryConstants.MOD_ID, "charcoal.pile", ICharcoalPileWall.class);

	@Override
	public ResourceLocation getPluginUid() {
		return ForestryModuleIds.ARBORICULTURE;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		JeiUtil.registerItemSubtypes(registration, TreeChromosomes.SPECIES, SpeciesUtil.TREE_TYPE.get());
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		JeiUtil.addDescription(registration, ArboricultureItems.GRAFTER.item(), ArboricultureItems.GRAFTER_PROVEN.item());

		registration.addRecipes(CHARCOAL_PILE, IForestryApi.INSTANCE.getTreeManager().getWalls());
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(new CharcoalPileWallCategory(guiHelper));
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(CharcoalBlocks.LOG_PILE.stack(), CHARCOAL_PILE);
	}
}
