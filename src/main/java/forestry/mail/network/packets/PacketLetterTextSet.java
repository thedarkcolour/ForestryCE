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

import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.PacketIdServer;
import forestry.mail.gui.LetterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record PacketLetterTextSet(String string) implements IForestryPacketServer {
	public static void handle(PacketLetterTextSet msg, ServerPlayer player) {
		if (player.containerMenu instanceof LetterMenu letterMenu) {
			letterMenu.handleSetText(msg.string());
		}
	}

	@Override
	public ResourceLocation id() {
		return PacketIdServer.LETTER_TEXT_SET;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(this.string);
	}

	public static PacketLetterTextSet decode(FriendlyByteBuf buffer) {
		return new PacketLetterTextSet(buffer.readUtf());
	}
}
