package forestry.core.fluids;

import forestry.api.recipes.*;
import forestry.core.utils.RecipeUtil;
import forestry.factory.features.FactoryRecipeTypes;
import forestry.factory.recipes.FabricatorSmeltingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class FluidRecipeFilter extends ReloadableFluidFilter {
	public static final FluidRecipeFilter HYGROREGULATOR_INPUT = new FluidRecipeFilter(manager -> RecipeUtil.getTargetFluidsFromIngredients(manager, FactoryRecipeTypes.HYGROREGULATOR.type(), recipe -> recipe.getInputFluid().ingredient()));
	public static final FluidRecipeFilter CARPENTER_INPUT = new FluidRecipeFilter(manager -> RecipeUtil.getTargetFluidsFromIngredients(manager, FactoryRecipeTypes.CARPENTER.type(), recipe -> recipe.getInputFluid().map(SizedFluidIngredient::ingredient).orElse(FluidIngredient.empty())));
	public static final FluidRecipeFilter FERMENTER_INPUT = new FluidRecipeFilter(manager -> RecipeUtil.getTargetFluidsFromIngredients(manager, FactoryRecipeTypes.FERMENTER.type(), IFermenterRecipe::getInputFluid));
	public static final FluidRecipeFilter FERMENTER_OUTPUT = new FluidRecipeFilter(manager -> RecipeUtil.getTargetFluids(manager, FactoryRecipeTypes.FERMENTER.type(), IFermenterRecipe::getOutputFluid));
	public static final FluidRecipeFilter FABRICATOR_SMELTING_OUTPUT = new FluidRecipeFilter(manager -> RecipeUtil.getTargetFluidsFromIngredients(manager, FactoryRecipeTypes.FABRICATOR_SMELTING.type(), FabricatorSmeltingRecipe::result));
	public static final FluidRecipeFilter STILL_INPUT = new FluidRecipeFilter(manager -> RecipeUtil.getTargetFluidsFromIngredients(manager, FactoryRecipeTypes.STILL.type(), IStillRecipe::getInput));
	public static final FluidRecipeFilter STILL_OUTPUT = new FluidRecipeFilter(manager -> RecipeUtil.getTargetFluids(manager, FactoryRecipeTypes.STILL.type(), recipe -> Stream.of(recipe.getOutput().getFluid())));

	public FluidRecipeFilter(Function<RecipeManager, Set<ResourceLocation>> filters) {
		super(() -> filters.apply(RecipeUtil.getRecipeManager()));
	}
}
