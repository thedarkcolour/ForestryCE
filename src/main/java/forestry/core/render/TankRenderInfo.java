package forestry.core.render;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public class TankRenderInfo {
	public static final TankRenderInfo EMPTY = new TankRenderInfo(FluidStack.EMPTY, EnumTankLevel.EMPTY);

	private final FluidStack fluidStack;
	private final EnumTankLevel level;

	public TankRenderInfo(IFluidTank fluidTank) {
		this(fluidTank.getFluid(), EnumTankLevel.rateTankLevel(fluidTank));
	}

	public TankRenderInfo(FluidStack fluidStack, EnumTankLevel level) {
		this.fluidStack = fluidStack;
		this.level = level;
	}

	public FluidStack getFluidStack() {
		return this.fluidStack;
	}

	public EnumTankLevel getLevel() {
		return this.level;
	}
}
