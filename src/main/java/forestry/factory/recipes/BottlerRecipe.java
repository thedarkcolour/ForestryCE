package forestry.factory.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

public record BottlerRecipe(ItemStack input, FluidStack fluid, ItemStack output, boolean isFillRecipe) {
	@Nullable
	public static BottlerRecipe createEmptyingRecipe(ItemStack filled) {
		ItemStack empty = filled.copy();
		empty.setCount(1);

		IFluidHandlerItem fluidHandler = empty.getCapability(Capabilities.FluidHandler.ITEM);
		if (fluidHandler == null) {
			return null;
		}

		FluidStack drained = fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
		if (!drained.isEmpty() && drained.getAmount() > 0) {
			return new BottlerRecipe(fluidHandler.getContainer(), drained, filled, false);
		}

		return null;
	}

	@Nullable
	public static BottlerRecipe createFillingRecipe(Fluid res, ItemStack empty) {
		ItemStack filled = empty.copy();
		filled.setCount(1);

		IFluidHandlerItem fluidHandler = empty.getCapability(Capabilities.FluidHandler.ITEM);
		if (fluidHandler == null) {
			return null;
		}

		int fillAmount = fluidHandler.fill(new FluidStack(res, Integer.MAX_VALUE), IFluidHandler.FluidAction.EXECUTE);
		if (fillAmount > 0) {
			return new BottlerRecipe(empty, new FluidStack(res, fillAmount), fluidHandler.getContainer(), true);
		}

		return null;
	}

	public boolean matchEmpty(ItemStack emptyCan, FluidStack resource) {
		return !emptyCan.isEmpty() && ItemStack.isSameItem(emptyCan, this.input) && FluidStack.isSameFluidSameComponents(resource, this.fluid) && this.isFillRecipe;
	}

	public boolean matchFilled(ItemStack filledCan) {
		return !this.output.isEmpty() && !this.isFillRecipe && ItemStack.isSameItem(this.output, filledCan);
	}
}
