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
package forestry.apiculture.gui;

import java.util.Objects;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

import forestry.api.modules.IForestryPacketClient;
import forestry.apiculture.features.ApicultureMenuTypes;
import forestry.apiculture.tiles.TileBeeHousingBase;
import forestry.core.gui.ContainerTile;
import forestry.core.network.packets.PacketGuiStream;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;

public class ContainerBeeHousing extends ContainerTile<TileBeeHousingBase> implements IContainerBeeHousing {
	private final IGuiBeeHousingDelegate delegate;
	private final GuiBeeHousing.Icon icon;

	public static ContainerBeeHousing fromNetwork(int windowId, Inventory inv, FriendlyByteBuf buffer) {
		TileBeeHousingBase tile = TileUtil.getTile(inv.player.level(), buffer.readBlockPos(), TileBeeHousingBase.class);
		boolean hasFrames = buffer.readBoolean();
		GuiBeeHousing.Icon icon = NetworkUtil.readEnum(buffer, GuiBeeHousing.Icon.VALUES);
		return new ContainerBeeHousing(windowId, inv, Objects.requireNonNull(tile), hasFrames, icon);
	}

	public ContainerBeeHousing(int windowId, Inventory playerInv, TileBeeHousingBase tile, boolean hasFrames, GuiBeeHousing.Icon icon) {
		super(windowId, ApicultureMenuTypes.BEE_HOUSING.menuType(), playerInv, tile, 8, 108);
		ContainerBeeHelper.addSlots(this, tile, hasFrames);

		tile.getBeekeepingLogic().clearCachedValues();

		this.delegate = tile;
		this.icon = icon;
	}

	private int beeProgress = -1;

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		int beeProgress = this.tile.getBeekeepingLogic().getBeeProgressPercent();
		if (this.beeProgress != beeProgress) {
			this.beeProgress = beeProgress;
			IForestryPacketClient packet = new PacketGuiStream(this.tile);
			sendPacketToListeners(packet);
		}
	}

	@Override
	public IGuiBeeHousingDelegate getDelegate() {
		return this.delegate;
	}

	@Override
	public GuiBeeHousing.Icon getIcon() {
		return this.icon;
	}
}
