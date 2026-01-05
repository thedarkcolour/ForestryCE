package forestry.core.fluids;

import forestry.api.core.tooltips.ToolTip;
import forestry.core.network.IStreamable;
import forestry.core.utils.ModUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StandardTank extends FluidTank implements IStreamable {
	private ITankUpdateHandler tankUpdateHandler = tank -> {
	};
	private int tankIndex;
	private final boolean canFill;
	private final boolean canDrain;
	//Used to bypass a second validator test
	private boolean internalTest;

	@OnlyIn(Dist.CLIENT)
	@Nullable
	protected ToolTip toolTip;

	public StandardTank(int capacity, boolean canFill, boolean canDrain) {
		super(capacity);
		this.canDrain = canDrain;
		this.canFill = canFill;
	}

	public StandardTank(int capacity) {
		super(capacity);
		this.canFill = true;
		this.canDrain = true;
	}

	public void setTankIndex(int index) {
		this.tankIndex = index;
	}

	public void setTankUpdateHandler(TankManager tankUpdateHandler) {
		this.tankUpdateHandler = tankUpdateHandler;
	}

	public int getTankIndex() {
		return this.tankIndex;
	}

	public boolean isEmpty() {
		return getFluid().isEmpty() || getFluid().getAmount() <= 0;
	}

	public boolean isFull() {
		return !getFluid().isEmpty() && getFluid().getAmount() == getCapacity();
	}

	public int getRemainingSpace() {
		return this.capacity - getFluidAmount();
	}

	@Nullable
	public Fluid getFluidType() {
		return !getFluid().isEmpty() ? getFluid().getFluid() : null;
	}

	@Override
	public boolean isFluidValid(FluidStack stack) {
		return this.internalTest || this.validator.test(stack);
	}

	public boolean canFill() {
		return this.canFill;
	}

	public boolean canDrain() {
		return this.canDrain;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (!canFill() || !isFluidValid(resource)) {
			return 0;
		}
		return fillInternal(resource, action);
	}

	public int fillInternal(FluidStack resource, FluidAction action) {
        this.internalTest = true;
		int filled = super.fill(resource, action);
		if (action == FluidAction.EXECUTE && filled > 0) {
            this.tankUpdateHandler.updateTankLevels(this);
		}
        this.internalTest = false;
		return filled;
	}

	@Nonnull
	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		if (!this.canDrain) {
			return FluidStack.EMPTY;
		}
		return drainInternal(maxDrain, action);
	}

	@Nonnull
	public FluidStack drainInternal(int maxDrain, FluidAction action) {
		FluidStack drained = super.drain(maxDrain, action);
		if (action == FluidAction.EXECUTE && !drained.isEmpty() && drained.getAmount() > 0) {
            this.tankUpdateHandler.updateTankLevels(this);
		}
		return drained;
	}

	@Nonnull
	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (!this.canDrain) {
			return FluidStack.EMPTY;
		}
		return drainInternal(resource, action);
	}

	@Nonnull
	public FluidStack drainInternal(FluidStack resource, FluidAction action) {
		FluidStack drained = super.drain(resource, action);
		if (action == FluidAction.EXECUTE && !drained.isEmpty() && drained.getAmount() > 0) {
            this.tankUpdateHandler.updateTankLevels(this);
		}
		return drained;
	}

	@Override
	public String toString() {
		return String.format("Tank: %s, %d/%d", !this.fluid.isEmpty() ? ModUtil.getRegistryName(this.fluid.getFluid()) : "Empty", getFluidAmount(), getCapacity());
	}

	protected boolean hasFluid() {
		FluidStack fluid = getFluid();
		return !fluid.isEmpty() && fluid.getAmount() > 0 && fluid.getFluid() != Fluids.EMPTY;
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		data.writeFluidStack(this.fluid);
	}

	@Override
	public void readData(FriendlyByteBuf data) {
        this.fluid = data.readFluidStack();
	}

	@OnlyIn(Dist.CLIENT)
	public ToolTip getToolTip() {
		if (this.toolTip == null) {
            this.toolTip = new TankToolTip(this);
		}
		return this.toolTip;
	}

	@OnlyIn(Dist.CLIENT)
	protected void refreshTooltip() {
		ToolTip toolTip = getToolTip();
		toolTip.clear();
		int amount = 0;
		FluidStack fluidStack = getFluid();
		if (!fluidStack.isEmpty()) {
			Fluid fluidType = fluidStack.getFluid();
			FluidType attributes = fluidType.getFluidType();
			Rarity rarity = attributes.getRarity();
			if (rarity == null) {
				rarity = Rarity.COMMON;
			}
			toolTip.add(fluidStack.getDisplayName(), rarity.color);
			amount = getFluid().getAmount();
		}
		Component liquidAmount = Component.translatable("for.gui.tooltip.liquid.amount", amount, getCapacity());
		toolTip.add(liquidAmount);
	}

	@OnlyIn(Dist.CLIENT)
	private static class TankToolTip extends ToolTip {
		private final StandardTank standardTank;

		public TankToolTip(StandardTank standardTank) {
			this.standardTank = standardTank;
		}

		@Override
		public void refresh() {
            this.standardTank.refreshTooltip();
		}
	}
}
