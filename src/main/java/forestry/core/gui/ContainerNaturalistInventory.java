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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerListener;

import forestry.api.genetics.ISpeciesType;
import forestry.core.features.CoreMenuTypes;
import forestry.core.gui.slots.SlotFilteredInventory;
import forestry.core.tiles.IFilterSlotDelegate;
import forestry.core.tiles.TileNaturalistChest;
import forestry.core.tiles.TileUtil;

public class ContainerNaturalistInventory extends ContainerTile<TileNaturalistChest> implements IGuiSelectable, INaturalistMenu {
	public static final int MAX_PAGE = 5;
	private final int page;
	public boolean closing = true;

	public ContainerNaturalistInventory(int windowId, Inventory player, TileNaturalistChest tile, int page) {
		super(windowId, CoreMenuTypes.NATURALIST_INVENTORY.menuType(), player, tile, 18, 120);

		this.page = page;
		addInventory(this, tile, page);
	}

	public static <T extends Container & IFilterSlotDelegate> void addInventory(ContainerForestry container, T inventory, int selectedPage) {
		int page = Mth.clamp(selectedPage, 0, MAX_PAGE);
		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 5; y++) {
				int slot = y + page * 25 + x * 5;

				container.addSlot(new SlotFilteredInventory(inventory, slot, 100 + y * 18, 21 + x * 18));
			}
		}
	}

	public static ContainerNaturalistInventory fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
		TileNaturalistChest tile = TileUtil.getTile(playerInv.player.level(), extraData.readBlockPos(), TileNaturalistChest.class);
		return new ContainerNaturalistInventory(windowId, playerInv, tile, extraData.readVarInt());
	}

	@Override
	public void handleSelectionRequest(ServerPlayer player, int primary, int secondary) {
		closing = false;
		tile.flipPage(player, (short) primary);
	}

	@Override
	public ISpeciesType<?, ?> getSpeciesType() {
		return tile.getSpeciesType();
	}

	@Override
	public int getCurrentPage() {
		return page;
	}

	@Override
	public void onFlipPage() {
		// stop chest from playing closing animation and sound
		this.closing = false;
	}

	@Override
	public void addSlotListener(ContainerListener listener) {
		super.addSlotListener(listener);

		if (page == 0) {
			tile.increaseNumPlayersUsing();
		}
	}

	@Override
	public void removed(Player player) {
		super.removed(player);

		if (closing) {
			tile.decreaseNumPlayersUsing();
		}
	}
}
