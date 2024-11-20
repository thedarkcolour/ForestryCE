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

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import forestry.core.gui.slots.SlotForestry;
import forestry.core.gui.slots.SlotLocked;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.SlotUtil;

public abstract class ContainerForestry extends AbstractContainerMenu {
	public static final int PLAYER_HOTBAR_OFFSET = 27;
	public static final int PLAYER_INV_SLOTS = PLAYER_HOTBAR_OFFSET + 9;
	// number of items that have been shift-click-transfered during this click
	private int transferCount = 0;
	// null on client side
	@Nullable
	protected final ServerPlayer player;

	protected ContainerForestry(int containerId, MenuType<?> type, @Nullable Player player) {
		super(type, containerId);

		if (player instanceof ServerPlayer serverPlayer) {
			this.player = serverPlayer;
		} else {
			this.player = null;
		}
	}

	protected final void addPlayerInventory(Inventory playerInventory, int xInv, int yInv) {
		// Player inventory
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlot(playerInventory, column + row * 9 + 9, xInv + column * 18, yInv + row * 18);
			}
		}
		// Player hotbar
		for (int column = 0; column < 9; column++) {
			addHotbarSlot(playerInventory, column, xInv + column * 18, yInv + 58);
		}
	}

	protected void addHotbarSlot(Inventory playerInventory, int slot, int x, int y) {
		super.addSlot(new Slot(playerInventory, slot, x, y));
	}

	protected void addSlot(Inventory playerInventory, int slot, int x, int y) {
		super.addSlot(new Slot(playerInventory, slot, x, y));
	}

	// Public override
	@Override
	public Slot addSlot(Slot slot) {
		return super.addSlot(slot);
	}

	@Override
	public void clicked(int slotId, int button, ClickType clickTypeIn, Player player) {
		if (!canAccess(player)) {
			return;
		}

		if (clickTypeIn == ClickType.SWAP && button >= 0 && button < 9) {
			// hotkey used to move item from slot to hotbar
			int hotbarSlotIndex = PLAYER_HOTBAR_OFFSET + button;
			Slot hotbarSlot = getSlot(hotbarSlotIndex);
			if (hotbarSlot instanceof SlotLocked) {
				return;
			}
		}

		Slot slot = slotId < 0 ? null : getSlot(slotId);
		if (slot instanceof SlotForestry slotForestry) {
			if (slotForestry.isPhantom()) {
				SlotUtil.slotClickPhantom(slotForestry, button, clickTypeIn, player);
				return;
			}
		}

		transferCount = 0;
		super.clicked(slotId, button, clickTypeIn, player);
	}

	public Slot getForestrySlot(int slot) {
		return getSlot(PLAYER_INV_SLOTS + slot);
	}

	@Override
	public final ItemStack quickMoveStack(Player player, int slotIndex) {
		if (!canAccess(player)) {
			return ItemStack.EMPTY;
		}

		if (transferCount < 64) {
			transferCount++;
			return SlotUtil.transferStackInSlot(slots, player, slotIndex);
		}
		return ItemStack.EMPTY;
	}

	protected abstract boolean canAccess(Player player);

	protected final void sendPacketToListeners(IForestryPacketClient packet) {
		if (player != null) {
			NetworkUtil.sendToPlayer(packet, player);
		}
	}
}
