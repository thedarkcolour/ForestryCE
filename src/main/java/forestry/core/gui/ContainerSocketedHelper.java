package forestry.core.gui;

import forestry.api.IForestryApi;
import forestry.api.circuits.ICircuitBoard;
import forestry.core.circuits.ISocketable;
import forestry.core.circuits.ISolderingIron;
import forestry.core.network.packets.PacketChipsetClick;
import forestry.core.network.packets.PacketSocketUpdate;
import forestry.core.network.packets.PacketSolderingIronClick;
import forestry.core.utils.InventoryUtil;
import forestry.core.utils.NetworkUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ContainerSocketedHelper<T extends BlockEntity & ISocketable> implements IContainerSocketed {

	private final T tile;

	public ContainerSocketedHelper(T tile) {
		this.tile = tile;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleChipsetClick(int slot) {
		NetworkUtil.sendToServer(new PacketChipsetClick(slot));
	}

	@Override
	public void handleChipsetClickServer(int slot, ServerPlayer player, ItemStack itemstack) {
		if (!this.tile.getSocket(slot).isEmpty()) {
			return;
		}

		if (!IForestryApi.INSTANCE.getCircuitManager().isCircuitBoard(itemstack)) {
			return;
		}

		ICircuitBoard circuitBoard = IForestryApi.INSTANCE.getCircuitManager().getCircuitBoard(itemstack);
		if (circuitBoard == null) {
			return;
		}

		if (!this.tile.getSocketType().equals(circuitBoard.getSocketType())) {
			return;
		}

		ItemStack toSocket = itemstack.copy();
		toSocket.setCount(1);
        this.tile.setSocket(slot, toSocket);

		ItemStack stack = player.containerMenu.getCarried();
		stack.shrink(1);
		player.containerMenu.broadcastChanges();

		PacketSocketUpdate packet = PacketSocketUpdate.create(this.tile);
		NetworkUtil.sendToPlayer(packet, player);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleSolderingIronClick(int slot) {
		NetworkUtil.sendToServer(new PacketSolderingIronClick(slot));
	}

	@Override
	public void handleSolderingIronClickServer(int slot, ServerPlayer player, ItemStack itemstack) {
		ItemStack socket = this.tile.getSocket(slot);
		if (socket.isEmpty() || !(itemstack.getItem() instanceof ISolderingIron)) {
			return;
		}

		// Not sufficient space in player's inventory. failed to stow.
		if (!InventoryUtil.stowInInventory(socket, player.getInventory(), false)) {
			return;
		}

        this.tile.setSocket(slot, ItemStack.EMPTY);
		InventoryUtil.stowInInventory(socket, player.getInventory(), true);
		itemstack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(p.getUsedItemHand()));    //TODO onBreak
		player.inventoryMenu.broadcastChanges();

		PacketSocketUpdate packet = PacketSocketUpdate.create(this.tile);
		NetworkUtil.sendToPlayer(packet, player);
	}
}
