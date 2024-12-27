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
package forestry.mail;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import forestry.api.mail.IMailAddress;
import forestry.core.utils.NetworkUtil;
import forestry.mail.network.packets.PacketPOBoxInfoResponse;

// todo this shouldn't be a separate class
public class EventHandlerMailAlert {
	@SubscribeEvent
	public void handlePlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		if (player.level.isClientSide) {
			return;
		}

		IMailAddress address = new MailAddress(player.getGameProfile());
		POBox pobox = POBoxRegistry.getOrCreate((ServerLevel) player.level).getOrCreatePOBox(address);
		PacketPOBoxInfoResponse packet = new PacketPOBoxInfoResponse(pobox.getPOBoxInfo(), false);
		NetworkUtil.sendToPlayer(packet, (ServerPlayer) player);
	}
}
