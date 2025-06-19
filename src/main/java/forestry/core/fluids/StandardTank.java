package forestry.core.fluids;

import forestry.api.core.tooltips.ToolTip;
import forestry.core.network.IStreamable;
import forestry.core.utils.ModUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;

public class StandardTank extends FluidTank implements IStreamable {
	private ITankUpdateHandler tankUpdateHandler = tank -> {
	};
	private int tankIndex;
	private final boolean canFill;
	private final boolean canDrain;
	//Used to bypass a second validator test
	private boolean internalTest;

	// used on client only
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

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		if (!this.canDrain) {
			return FluidStack.EMPTY;
		}
		return drainInternal(maxDrain, action);
	}

	// ignores "canDrain" property
	public FluidStack drainInternal(int maxDrain, FluidAction action) {
		FluidStack drained = super.drain(maxDrain, action);
		if (action == FluidAction.EXECUTE && !drained.isEmpty() && drained.getAmount() > 0) {
			this.tankUpdateHandler.updateTankLevels(this);
		}
		return drained;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (!this.canDrain) {
			return FluidStack.EMPTY;
		}
		return drainInternal(resource, action);
	}

	// ignores "canDrain" property
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
	public void writeData(RegistryFriendlyByteBuf buffer) {
		FluidStack.STREAM_CODEC.encode(buffer, this.fluid);
	}

	@Override
	public void readData(RegistryFriendlyByteBuf buffer) {
		this.fluid = FluidStack.STREAM_CODEC.decode(buffer);
	}

	@OnlyIn(Dist.CLIENT)
	public ToolTip getToolTip() {
		if (this.toolTip == null) {
			this.toolTip = new TankToolTip(this);
		}
		return this.toolTip;
	}

	protected void refreshTooltip() {
		ToolTip toolTip = getToolTip();
		toolTip.clear();
		int amount = 0;
		FluidStack fluidStack = getFluid();
		if (!fluidStack.isEmpty()) {
			Fluid fluidType = fluidStack.getFluid();
			FluidType attributes = fluidType.getFluidType();
			Rarity rarity = attributes.getRarity();
            toolTip.add(fluidStack.getHoverName().copy().withStyle(rarity.getStyleModifier()));
			amount = getFluid().getAmount();
		}
		Component liquidAmount = Component.translatable("for.gui.tooltip.liquid.amount", amount, getCapacity());
		toolTip.add(liquidAmount);
	}

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
