package forestry.core.fluids;

import forestry.core.utils.ItemStackUtil;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;

/**
 * Helper to slowly fill containers from a machine's internal tank.
 * Moves filled container from inputSlot to outputSlot.
 */
public class ContainerFiller {
	private final FluidTank fluidTank;
	private final int fillingTime;
	private final Container inventory;
	private final int inputSlot;
	private final int outputSlot;

	@Nullable
	private ItemStack usedInput;
	private int fillingProgress;

	public ContainerFiller(FluidTank fluidTank, int fillingTime, Container inventory, int inputSlot, int outputSlot) {
		this.fluidTank = fluidTank;
		this.fillingTime = fillingTime;
		this.inventory = inventory;
		this.inputSlot = inputSlot;
		this.outputSlot = outputSlot;
	}

	public void updateServerSide() {
		ItemStack input = this.inventory.getItem(this.inputSlot);
		if (this.usedInput == null || !ItemStackUtil.isIdenticalItem(this.usedInput, input)) {
			this.fillingProgress = 0;
			this.usedInput = input;
		}

		if (this.usedInput != null) {
			FluidStack tankContents = this.fluidTank.getFluid();
			if (!tankContents.isEmpty() && tankContents.getAmount() > 0) {
				if (this.fillingProgress == 0) {
					Fluid tankFluid = tankContents.getFluid();
					FluidHelper.FillStatus canFill = FluidHelper.fillContainers(this.fluidTank, this.inventory, this.inputSlot, this.outputSlot, tankFluid, false);
					if (canFill == FluidHelper.FillStatus.SUCCESS) {
						this.fillingProgress = 1;
					}
				} else {
					this.fillingProgress++;
					if (this.fillingProgress >= this.fillingTime) {
						Fluid tankFluid = tankContents.getFluid();
						FluidHelper.FillStatus filled = FluidHelper.fillContainers(this.fluidTank, this.inventory, this.inputSlot, this.outputSlot, tankFluid, true);
						if (filled == FluidHelper.FillStatus.SUCCESS) {
							this.fillingProgress = 0;
						}
					}
				}
			}
		}
	}

	public int getFillingProgress() {
		return this.fillingProgress;
	}
}
