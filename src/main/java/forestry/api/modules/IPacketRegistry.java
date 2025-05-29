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
package forestry.api.modules;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;

public interface IPacketRegistry {
	/**
	 * Register a packet during the play phase that is handled on the main server thread when the sender is not null.
	 */
	<P extends IForestryPacketServer> void serverbound(CustomPacketPayload.Type<P> type, StreamDecoder<RegistryFriendlyByteBuf, P> decoder, BiConsumer<P, ServerPlayer> packetHandler);

	/**
	 * Register a packet during the play phase that is handled on the main render thread on the client.
	 */
	<P extends IForestryPacketClient> void clientbound(CustomPacketPayload.Type<P> type, StreamDecoder<RegistryFriendlyByteBuf, P> decoder, BiConsumer<P, Player> packetHandler);
}
