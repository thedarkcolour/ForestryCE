package forestry.core.fluids;

import forestry.core.utils.ItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

// todo nah wtf is this
//TODO: Fix isFillable's
public class FluidHelper {
	public static boolean canAcceptFluid(Level level, BlockPos pos, Direction facing, FluidStack fluid) {
		IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, facing);
		if (handler == null) {
			return false;
		}

		for (int tank = 0; tank < handler.getTanks(); tank++) {
			int amountFilled = handler.fill(fluid, IFluidHandler.FluidAction.SIMULATE);
			if (amountFilled > 0) {
				return true;
			}
		}

		return false;
	}

	public enum FillStatus {
		SUCCESS, INVALID_INPUT, NO_FLUID, NO_SPACE, NO_SPACE_FLUID
	}

	public static FillStatus fillContainers(IFluidHandler fluidHandler, Container inv, int inputSlot, int outputSlot, Fluid fluidToFill, boolean doFill) {
		return fillContainers(fluidHandler, inv, inputSlot, outputSlot, fluidToFill, getEmptyContainer(inv.getItem(inputSlot)), doFill);
	}

	public static FillStatus fillContainers(IFluidHandler fluidHandler, Container inv, int inputSlot, int outputSlot, Fluid fluidToFill, ItemStack emptyStack, boolean doFill) {
		ItemStack input = inv.getItem(inputSlot);
		if (input.isEmpty()) {
			return FillStatus.INVALID_INPUT;
		}
		ItemStack output = inv.getItem(outputSlot);

		ItemStack filled = input.copy();
		filled.setCount(1);

		if (emptyStack.isEmpty()) {
			emptyStack = filled;
		}

		IFluidHandlerItem fluidFilledHandler = filled.getCapability(Capabilities.FluidHandler.ITEM);
		IFluidHandlerItem fluidEmptyHandler = emptyStack.getCapability(Capabilities.FluidHandler.ITEM);
		if (fluidFilledHandler == null || fluidEmptyHandler == null) {
			return FillStatus.INVALID_INPUT;
		}

		int containerEmptyCapacity = fluidEmptyHandler.fill(new FluidStack(fluidToFill, Integer.MAX_VALUE), IFluidHandler.FluidAction.SIMULATE);
		int containerCapacity = fluidFilledHandler.fill(new FluidStack(fluidToFill, Integer.MAX_VALUE), IFluidHandler.FluidAction.SIMULATE);
		if (containerCapacity <= 0 && containerEmptyCapacity <= 0) {
			return FillStatus.INVALID_INPUT;
		}

		FluidStack canDrain = fluidHandler.drain(new FluidStack(fluidToFill, containerCapacity), IFluidHandler.FluidAction.SIMULATE);
		if (canDrain.isEmpty()) {
			return FillStatus.NO_FLUID;
		}

		if (fluidFilledHandler.fill(canDrain, IFluidHandler.FluidAction.EXECUTE) <= 0) {
			return FillStatus.NO_FLUID; // standard containers will not fill if there isn't enough fluid
		}

		FluidStack fluidInContainer = fluidFilledHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
		if (fluidInContainer.isEmpty()) {
			return FillStatus.INVALID_INPUT;
		}

		filled = fluidFilledHandler.getContainer();

		boolean moveToOutput = fluidInContainer.getAmount() >= containerCapacity;
		if (moveToOutput) {
			if (!output.isEmpty() && (output.getCount() >= output.getMaxStackSize() || !ItemStackUtil.areItemStacksEqualIgnoreCount(filled, output))) {
				return FillStatus.NO_SPACE;
			}
		} else {
			if (input.getCount() > 1) {
				return FillStatus.NO_SPACE;
			}
		}

		if (doFill) {
			fluidHandler.drain(canDrain, IFluidHandler.FluidAction.EXECUTE);
			if (moveToOutput) {
				if (output.isEmpty()) {
					inv.setItem(outputSlot, filled);
				} else {
					output.grow(1);
				}
				inv.removeItem(inputSlot, 1);
			} else {
				inv.setItem(inputSlot, filled);
			}
		}

		return FillStatus.SUCCESS;
	}

	public static boolean drainContainers(IFluidHandler fluidHandler, Container inv, int inputSlot) {
		ItemStack input = inv.getItem(inputSlot);
		if (input.isEmpty()) {
			return false;
		}

		FluidActionResult fluidActionSimulated = FluidUtil.tryEmptyContainer(input, fluidHandler, FluidType.BUCKET_VOLUME, null, false);
		if (!fluidActionSimulated.isSuccess()) {
			return false;
		}

		ItemStack drainedItemSimulated = fluidActionSimulated.getResult();
		if (input.getCount() == 1 || drainedItemSimulated.isEmpty()) {
			FluidActionResult fluidActionResult = FluidUtil.tryEmptyContainer(input, fluidHandler, FluidType.BUCKET_VOLUME, null, true);
			if (fluidActionResult.isSuccess()) {
				ItemStack drainedItem = fluidActionResult.getResult();
				if (!drainedItem.isEmpty()) {
					inv.setItem(inputSlot, drainedItem);
				} else {
					inv.removeItem(inputSlot, 1);
				}
				return true;
			}
		}

		return false;
	}

	public static FillStatus drainContainers(IFluidHandler fluidHandler, Container inv, int inputSlot, int outputSlot, boolean doDrain) {
		ItemStack input = inv.getItem(inputSlot);
		if (input.isEmpty()) {
			return FillStatus.INVALID_INPUT;
		}
		ItemStack outputStack = inv.getItem(outputSlot);

		//Only needed so we can test if the container can be filled
		FluidStack content = FluidUtil.getFluidContained(input).orElse(FluidStack.EMPTY);
		FluidActionResult drainedResultSimulated = FluidUtil.tryEmptyContainer(input, fluidHandler, FluidType.BUCKET_VOLUME, null, false);
		if (!drainedResultSimulated.isSuccess()) {
			return FillStatus.INVALID_INPUT;
		}

		ItemStack drainedItemSimulated = drainedResultSimulated.getResult();

		if (outputStack.isEmpty() || drainedItemSimulated.isEmpty() || ItemStackUtil.isIdenticalItem(outputStack, drainedItemSimulated) && outputStack.getCount() + drainedItemSimulated.getCount() < outputStack.getMaxStackSize()) {
			if (doDrain) {
				FluidActionResult drainedResult = FluidUtil.tryEmptyContainer(input, fluidHandler, FluidType.BUCKET_VOLUME, null, true);
				if (drainedResult.isSuccess()) {
					ItemStack drainedItem = drainedResult.getResult();
					if (!drainedItem.isEmpty()) {
						ItemStack newStack = drainedItem.copy();
						if (!outputStack.isEmpty()) {
							newStack.grow(outputStack.getCount());
						}
						if (!isFillableContainer(newStack, content) || isFillableContainerAndEmpty(newStack, content)) {
							inv.setItem(outputSlot, newStack);
							inv.removeItem(inputSlot, 1);
						}
						if (isDrainableContainer(newStack) && !isEmpty(newStack)) {
							inv.setItem(inputSlot, newStack);
						}
					} else {
						inv.removeItem(inputSlot, 1);
					}
					return FillStatus.SUCCESS;
				}
			}
			return FillStatus.SUCCESS;
		}

		return FillStatus.NO_SPACE;
	}

	public static boolean isFillableContainer(ItemStack container, FluidStack content) {
		IFluidHandlerItem handler = container.getCapability(Capabilities.FluidHandler.ITEM);

		return handler != null && (handler.fill(content.copyWithAmount(1), IFluidHandler.FluidAction.SIMULATE) > 0);
	}

	public static boolean isFillableContainerAndEmpty(ItemStack container, FluidStack content) {
		IFluidHandlerItem handler = container.getCapability(Capabilities.FluidHandler.ITEM);

		return handler != null && (handler.fill(content.copyWithAmount(1), IFluidHandler.FluidAction.SIMULATE) > 0) && (handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE).isEmpty());
	}

	public static ItemStack getEmptyContainer(ItemStack container) {
		ItemStack empty = container.copy();
		empty.setCount(1);
		IFluidHandlerItem handler = empty.getCapability(Capabilities.FluidHandler.ITEM);

		return (handler != null && handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE).isEmpty()) ? empty : ItemStack.EMPTY;
	}

	public static boolean isFillableContainerWithRoom(ItemStack container) {
		LazyOptional<IFluidHandlerItem> fluidHandlerCap = FluidUtil.getFluidHandler(container);
		return fluidHandlerCap.isPresent();
		/*if (!fluidHandlerCap.isPresent()) {
			return false;
		}

		IFluidHandlerItem fluidHandler = fluidHandlerCap.orElse(null);

		IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
		for (IFluidTankProperties properties : tankProperties) {
			if (properties.canFill() && properties.getCapacity() > 0) {
				FluidStack contents = properties.getContents();
				if (contents == null) {
					return true;
				} else if (contents.amount < properties.getCapacity()) {
					return true;
				}
			}
		}

		return false;*/
	}

	public static boolean isFillableEmptyContainer(ItemStack empty) {
		LazyOptional<IFluidHandlerItem> fluidHandlerCap = FluidUtil.getFluidHandler(empty);
		return fluidHandlerCap.isPresent();
		/*if (!fluidHandlerCap.isPresent()) {
			return false;
		}

		IFluidHandlerItem fluidHandler = fluidHandlerCap.orElse(null);

		IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
		for (IFluidTankProperties properties : tankProperties) {
			if (!properties.canFill()) {
				return false;
			}

			FluidStack contents = properties.getContents();
			if (contents != null && contents.amount > 0) {
				return false;
			}
		}

		return true;*/
	}

	// used by squeezer to check if the item's fluid can be extracted from it
	public static boolean isDrainableFilledContainer(ItemStack container) {
		LazyOptional<IFluidHandlerItem> fluidHandlerCap = FluidUtil.getFluidHandler(container);
		if (!fluidHandlerCap.isPresent()) {
			return false;
		}

		IFluidHandlerItem fluidHandler = fluidHandlerCap.orElse(null);
		int capacity = fluidHandler.getTankCapacity(0);

		if (fluidHandler.getFluidInTank(0).getAmount() == capacity) {
			return fluidHandler.drain(capacity, IFluidHandler.FluidAction.SIMULATE).getAmount() == capacity;
		} else {
			return false;
		}
	}

	public static boolean isDrainableContainer(ItemStack container) {
		LazyOptional<IFluidHandlerItem> fluidHandlerCap = FluidUtil.getFluidHandler(container);
		return fluidHandlerCap.isPresent();
		/*if (!fluidHandlerCap.isPresent()) {
			return false;
		}

		IFluidHandlerItem fluidHandler = fluidHandlerCap.orElse(null);

		IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
		for (IFluidTankProperties properties : tankProperties) {
			if (properties.canDrain()) {
				return true;
			}
		}

		return false;*/
	}

	public static boolean isEmpty(ItemStack container) {
		LazyOptional<IFluidHandlerItem> fluidHandlerCap = FluidUtil.getFluidHandler(container);
		if (!fluidHandlerCap.isPresent()) {
			return false;
		}

		return fluidHandlerCap.filter(fluidHandler -> {
			for (int i = 0; i < fluidHandler.getTanks(); i++) {
				if (!fluidHandler.getFluidInTank(i).isEmpty()) {
					return false;
				}
			}
			return true;
		}).isPresent();
	}

}
