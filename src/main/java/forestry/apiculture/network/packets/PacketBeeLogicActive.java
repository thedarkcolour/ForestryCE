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
package forestry.apiculture.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;

// Similar to PacketGuiStream
public record PacketBeeLogicActive(
		BlockPos pos,
		// null on client side
		IBeekeepingLogic logic,
		// null on server side
		FriendlyByteBuf payload
) implements IForestryPacketClient {
	public PacketBeeLogicActive(IBeeHousing tile) {
		this(tile.getCoordinates(), tile.getBeekeepingLogic(), null);
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.BEE_LOGIC_ACTIVE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		NetworkUtil.writePayloadBuffer(buffer, logic::writeData);
	}

	public static PacketBeeLogicActive decode(FriendlyByteBuf buffer) {
		return new PacketBeeLogicActive(buffer.readBlockPos(), null, NetworkUtil.readPayloadBuffer(buffer));
	}

	public static void handle(PacketBeeLogicActive msg, Player player) {
		IBeeHousing beeHousing = TileUtil.getTile(player.level(), msg.pos, IBeeHousing.class);
		if (beeHousing != null) {
			IBeekeepingLogic beekeepingLogic = beeHousing.getBeekeepingLogic();
			beekeepingLogic.readData(msg.payload);
		}
	}
}
