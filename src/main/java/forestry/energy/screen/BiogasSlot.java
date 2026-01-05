package forestry.energy.screen;


import forestry.api.core.tooltips.ToolTip;
import forestry.core.gui.widgets.ReservoirWidget;
import forestry.core.gui.widgets.WidgetManager;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public class BiogasSlot extends ReservoirWidget {
	public BiogasSlot(WidgetManager manager, int xPos, int yPos, int slot) {
		super(manager, xPos, yPos, slot);
	}

	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		ToolTip toolTip = new ToolTip();
		IFluidTank tank = getTank();
		if (tank != null) {
			FluidStack fluid = tank.getFluid();
			if (fluid.isEmpty()) {
				toolTip.add(Component.translatable("for.gui.empty"));
			} else {
				toolTip.add(fluid.getDisplayName());
			}
		}
		return toolTip;
	}

	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton) {
		// do not allow pipette
	}
}
