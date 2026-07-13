package forestry.core.fluids;

import forestry.api.recipes.*;
import forestry.core.utils.RecipeUtils;
import forestry.energy.features.EnergyRecipeTypes;
import forestry.factory.features.FactoryRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Set;
import java.util.function.Function;

public class FluidRecipeFilter extends ReloadableFluidFilter {
	public static final FluidRecipeFilter HYGROREGULATOR_INPUT = new FluidRecipeFilter(manager -> RecipeUtils.getTargetFluidsFromStacks(manager, FactoryRecipeTypes.HYGROREGULATOR.type(), IHygroregulatorRecipe::getInputFluid));
	public static final FluidRecipeFilter CARPENTER_INPUT = new FluidRecipeFilter(manager -> RecipeUtils.getTargetFluidsFromStacks(manager, FactoryRecipeTypes.CARPENTER.type(), ICarpenterRecipe::getInputFluid));
	public static final FluidRecipeFilter FERMENTER_INPUT = new FluidRecipeFilter(manager -> RecipeUtils.getTargetFluidsFromStacks(manager, FactoryRecipeTypes.FERMENTER.type(), IFermenterRecipe::getInputFluid));
	public static final FluidRecipeFilter FERMENTER_OUTPUT = new FluidRecipeFilter(manager -> RecipeUtils.getTargetFluids(manager, FactoryRecipeTypes.FERMENTER.type(), IFermenterRecipe::getOutput));
	public static final FluidRecipeFilter FABRICATOR_SMELTING_OUTPUT = new FluidRecipeFilter(manager -> RecipeUtils.getTargetFluidsFromStacks(manager, FactoryRecipeTypes.FABRICATOR_SMELTING.type(), IFabricatorSmeltingRecipe::getResultFluid));
	public static final FluidRecipeFilter STILL_INPUT = new FluidRecipeFilter(manager -> RecipeUtils.getTargetFluidsFromStacks(manager, FactoryRecipeTypes.STILL.type(), IStillRecipe::getInput));
	public static final FluidRecipeFilter STILL_OUTPUT = new FluidRecipeFilter(manager -> RecipeUtils.getTargetFluidsFromStacks(manager, FactoryRecipeTypes.STILL.type(), IStillRecipe::getOutput));
	public static final FluidRecipeFilter BIOGAS_FUEL = new FluidRecipeFilter(manager -> RecipeUtils.getTargetFluidsFromIngredients(manager, EnergyRecipeTypes.BIOGAS_FUEL.type(), IBiogasFuel::getFluidInput));

	public FluidRecipeFilter(Function<RecipeManager, Set<ResourceLocation>> filters) {
		super(() -> filters.apply(RecipeUtils.getRecipeManager()));
	}
}
