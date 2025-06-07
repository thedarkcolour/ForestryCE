package forestry.core.fluids;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public interface ITankManager extends IFluidHandler {
	// Used to send all tanks to the player upon first opening the screen
	void sendAllTanks(AbstractContainerMenu container, ServerPlayer player);

	// Used to send incremental changes in tanks to players who already have the initial state of the tanks
	void broadcastChanges(AbstractContainerMenu container, ServerPlayer players);

	// Used to clean up cached item stacks when a player closes the screen
	void onClosed(AbstractContainerMenu container);

	@Nullable
	IFluidTank getTank(int tankIndex);

	boolean canFillFluidType(FluidStack fluidStack);

	/**
	 * For updating tanks on the client
	 */
	void processTankUpdate(int tankIndex, @Nullable FluidStack contents);
}
