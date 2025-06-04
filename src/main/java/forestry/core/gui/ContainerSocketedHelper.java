/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.core.gui;

import forestry.api.IForestryApi;
import forestry.api.circuits.ICircuitBoard;
import forestry.api.modules.IForestryPacketServer;
import forestry.core.circuits.ISocketable;
import forestry.core.circuits.ISolderingIron;
import forestry.core.features.CoreDataComponents;
import forestry.core.network.packets.PacketChipsetClick;
import forestry.core.network.packets.PacketSocketUpdate;
import forestry.core.network.packets.PacketSolderingIronClick;
import forestry.core.utils.InventoryUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

public class ContainerSocketedHelper<T extends BlockEntity & ISocketable> implements IContainerSocketed {

	private final T tile;

	public ContainerSocketedHelper(T tile) {
		this.tile = tile;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleChipsetClick(int slot) {
		IForestryPacketServer packet = new PacketChipsetClick(slot);
		PacketDistributor.sendToServer(packet);
	}

	@Override
	public void handleChipsetClickServer(int slot, ServerPlayer player, ItemStack itemstack) {
		if (!this.tile.getSocket(slot).isEmpty()) {
			return;
		}

		if (!IForestryApi.INSTANCE.getCircuitManager().isCircuitBoard(itemstack)) {
			return;
		}

        IForestryApi.INSTANCE.getCircuitManager();
        ICircuitBoard circuitBoard = itemstack.get(CoreDataComponents.CIRCUIT_BOARD);
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
        PacketDistributor.sendToPlayer(player, packet);
    }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleSolderingIronClick(int slot) {
		IForestryPacketServer packet = new PacketSolderingIronClick(slot);
		PacketDistributor.sendToServer(packet);
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
        PacketDistributor.sendToPlayer(player, packet);
    }
}
