package forestry.core.gui;

import forestry.api.core.IToolPipette;
import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.packets.PacketPipetteClick;
import forestry.core.tiles.ILiquidTankTile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;

public class ContainerLiquidTanksHelper<T extends BlockEntity & ILiquidTankTile> implements IContainerLiquidTanks {
	private final T tile;

	public ContainerLiquidTanksHelper(T tile) {
		this.tile = tile;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handlePipetteClickClient(int slot, Player player) {
		ItemStack itemstack = player.containerMenu.getCarried();
		if (itemstack.getItem() instanceof IToolPipette) {
			IForestryPacketServer packet = new PacketPipetteClick(slot);
			PacketDistributor.sendToServer(packet);
		}
	}

	@Override
	public void handlePipetteClick(int slot, ServerPlayer player) {
		ItemStack stack = player.containerMenu.getCarried();
		Item held = stack.getItem();
		if (!(held instanceof IToolPipette pipette)) {
			return;
		}

		IFluidTank tank = this.tile.getTankManager().getTank(slot);
		int liquidAmount = tank.getFluidAmount();

		IFluidHandlerItem fluidHandlerItem = stack.getCapability(Capabilities.FluidHandler.ITEM);
		if (fluidHandlerItem != null) {
			if (pipette.canPipette(stack) && liquidAmount > 0) {
				// todo test pipette extraction
				FluidStack fillAmount = tank.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
				int filled = fluidHandlerItem.fill(fillAmount, IFluidHandler.FluidAction.EXECUTE);
				tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
				player.containerMenu.setCarried(fluidHandlerItem.getContainer());
				player.containerMenu.broadcastChanges();

			} else {
				FluidStack potential = fluidHandlerItem.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
				if (!potential.isEmpty()) {
					if (tank instanceof FluidTank) {
						int fill = tank.fill(potential, IFluidHandler.FluidAction.EXECUTE);
						fluidHandlerItem.drain(fill, IFluidHandler.FluidAction.EXECUTE);
						player.containerMenu.setCarried(fluidHandlerItem.getContainer());
						player.containerMenu.broadcastChanges();
					}
				}
			}
		}
	}

	@Nullable
	@Override
	public IFluidTank getTank(int slot) {
		return this.tile.getTankManager().getTank(slot);
	}
}
