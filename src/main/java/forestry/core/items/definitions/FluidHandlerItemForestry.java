package forestry.core.items.definitions;

import forestry.core.features.CoreDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStackSimple;

/**
 * Fluid handler that consumes the container item after it was used.
 */
public class FluidHandlerItemForestry extends FluidHandlerItemStackSimple.Consumable {
	private final EnumContainerType containerType;

	public FluidHandlerItemForestry(ItemStack container, EnumContainerType containerType) {
		super(CoreDataComponents.FLUID_CONTENTS, container, FluidType.BUCKET_VOLUME);
		this.containerType = containerType;
	}

	/**
	 * Checks if the given fluid can be contained in this handler. This property gets defined by the container type of
	 * this handler.
	 * <p>
	 * Capsules can't contain fluid that are hotter or equal to the melting point of wax
	 */
	private boolean contentsAllowed(FluidStack fluidStack) {
		Fluid fluid = fluidStack.getFluid();

		if (this.containerType == EnumContainerType.CAPSULE) {
			return fluid.getFluidType().getTemperature(fluidStack) < 310.15; // melting point of wax in kelvin
		}
		return true;
	}

	@Override
	public boolean canFillFluidType(FluidStack fluid) {
		return contentsAllowed(fluid);
	}

	@Override
	public boolean canDrainFluidType(FluidStack fluid) {
		return contentsAllowed(fluid);
	}

	@Override
	protected void setFluid(FluidStack fluid) {
		super.setFluid(fluid);
		this.container.setDamageValue(1); // show the filled container model
	}
}

