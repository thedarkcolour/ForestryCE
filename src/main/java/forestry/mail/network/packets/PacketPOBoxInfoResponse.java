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
package forestry.mail.network.packets;

import forestry.core.config.ForestryConfig;
import forestry.mail.gui.ToastMailboxInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.mail.POBoxInfo;

public record PacketPOBoxInfoResponse(int playerLetters, int tradeLetters) implements IForestryPacketClient {
	public PacketPOBoxInfoResponse(POBoxInfo info) {
		this(info.playerLetters(), info.tradeLetters());
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.POBOX_INFO_RESPONSE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(playerLetters);
		buffer.writeInt(tradeLetters);
	}

	public static PacketPOBoxInfoResponse decode(FriendlyByteBuf buffer) {
		return new PacketPOBoxInfoResponse(buffer.readInt(), buffer.readInt());
	}

	public static void handle(PacketPOBoxInfoResponse msg, Player player) {
		POBoxInfo poBox = new POBoxInfo(msg.playerLetters, msg.tradeLetters);
		if (player.equals(Minecraft.getInstance().player) && ForestryConfig.CLIENT.mailAlertsEnabled.get()) {
			ToastMailboxInfo.addOrUpdate(Minecraft.getInstance().getToasts(), poBox);
		}
	}
}
