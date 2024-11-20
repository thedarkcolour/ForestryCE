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
package forestry.core.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import forestry.api.core.ILocationProvider;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.IStreamableGui;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;

// Streamable is used on the server side to serialize the packet data (payload is null)
// Payload is used on the client side to sync the packet data (streamable is null)
public record PacketGuiStream(
		BlockPos pos,
		// null on client side
		IStreamableGui guiStreamable,
		// null on server side
		FriendlyByteBuf payload
) implements IForestryPacketClient {
	public <T extends IStreamableGui & ILocationProvider> PacketGuiStream(T guiStreamable) {
		this(guiStreamable.getCoordinates(), guiStreamable, null);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		NetworkUtil.writePayloadBuffer(buffer, guiStreamable::writeGuiData);
	}

	public static PacketGuiStream decode(FriendlyByteBuf data) {
		return new PacketGuiStream(data.readBlockPos(), null, NetworkUtil.readPayloadBuffer(data));
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.GUI_UPDATE;
	}

	public static void handle(PacketGuiStream msg, Player player) {
		IStreamableGui tile = TileUtil.getTile(player.level(), msg.pos, IStreamableGui.class);
		if (tile != null) {
			tile.readGuiData(msg.payload);
		}
	}
}
