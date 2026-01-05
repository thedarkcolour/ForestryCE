package forestry.core.fluids;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

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
	@OnlyIn(Dist.CLIENT)
	void processTankUpdate(int tankIndex, @Nullable FluidStack contents);
}
