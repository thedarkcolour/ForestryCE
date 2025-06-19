package forestry.core.render;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;

public record TankRenderInfo(FluidStack fluidStack, EnumTankLevel level) {
	public static final TankRenderInfo EMPTY = new TankRenderInfo(FluidStack.EMPTY, EnumTankLevel.EMPTY);

	public TankRenderInfo(IFluidTank fluidTank) {
		this(fluidTank.getFluid(), EnumTankLevel.rateTankLevel(fluidTank));
	}
}
