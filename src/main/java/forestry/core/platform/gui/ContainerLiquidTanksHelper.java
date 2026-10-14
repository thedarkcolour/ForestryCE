package forestry.core.platform.gui;

import forestry.api.core.IToolPipette;
import forestry.core.platform.fluids.ITankManager;
import forestry.core.platform.fluids.StandardTank;
import forestry.core.platform.network.packets.PacketPipetteClick;
import forestry.core.platform.tile.ILiquidTankTile;
import forestry.core.platform.util.NetworkUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.Optional;

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
			NetworkUtil.sendToServer(new PacketPipetteClick(slot));
		}
	}

	@Override
	public void handlePipetteClick(int slot, ServerPlayer player) {
		ItemStack itemstack = player.containerMenu.getCarried();
		Item held = itemstack.getItem();
		if (!(held instanceof IToolPipette pipette)) {
			return;
		}

		ITankManager tankManager = this.tile.getTankManager();
		// the slot comes from the client
		if (slot < 0 || slot >= tankManager.getTanks()) {
			return;
		}
		IFluidTank tank = tankManager.getTank(slot);
		if (tank == null) {
			return;
		}
		int liquidAmount = tank.getFluidAmount();

		Optional<IFluidHandlerItem> fluidCap = FluidUtil.getFluidHandler(itemstack);
		fluidCap.ifPresent(fluidHandlerItem -> {
			if (pipette.canPipette(itemstack) && liquidAmount > 0) {
				if (tank instanceof StandardTank standard) {
					FluidStack fillAmount = standard.drainInternal(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
					int filled = fluidHandlerItem.fill(fillAmount, IFluidHandler.FluidAction.EXECUTE);
					// not drain, which does nothing on a tank that is drain-locked for automation
					standard.drainInternal(filled, IFluidHandler.FluidAction.EXECUTE);
					player.containerMenu.setCarried(fluidHandlerItem.getContainer());
					player.containerMenu.broadcastChanges();
				} else {//TODO: Test if this works
					FluidStack fillAmount = tank.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
					int filled = fluidHandlerItem.fill(fillAmount, IFluidHandler.FluidAction.EXECUTE);
					tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
					player.containerMenu.setCarried(fluidHandlerItem.getContainer());
					player.containerMenu.broadcastChanges();
				}
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
		});
	}

	@Nullable
	@Override
	public IFluidTank getTank(int slot) {
		return this.tile.getTankManager().getTank(slot);
	}
}
