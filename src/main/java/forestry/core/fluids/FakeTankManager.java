package forestry.core.fluids;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import javax.annotation.Nullable;

public class FakeTankManager extends EmptyFluidHandler implements ITankManager {
	public static final FakeTankManager instance = new FakeTankManager();

	private FakeTankManager() {
	}

	@Override
	public void sendAllTanks(AbstractContainerMenu container, ServerPlayer player) {
	}

	@Override
	public void broadcastChanges(AbstractContainerMenu container, ServerPlayer players) {
	}

	@Override
	public void onClosed(AbstractContainerMenu container) {
	}

	@Nullable
	@Override
	public IFluidTank getTank(int tankIndex) {
		return null;
	}

	@Override
	public boolean canFillFluidType(FluidStack fluidStack) {
		return false;
	}

	@Override
	public void processTankUpdate(int tankIndex, @Nullable FluidStack contents) {
	}
}
