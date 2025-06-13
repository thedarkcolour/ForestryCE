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

import forestry.api.IForestryApi;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.circuits.SolderingIronMenu;
import forestry.core.network.PacketIdClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record PacketGuiLayoutSelect(String layoutUid) implements IForestryPacketClient {
	@Override
	public ResourceLocation id() {
		return PacketIdClient.GUI_LAYOUT_SELECT;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(this.layoutUid);
	}

	public static PacketGuiLayoutSelect decode(FriendlyByteBuf buffer) {
		return new PacketGuiLayoutSelect(buffer.readUtf());
	}

	public static void handle(PacketGuiLayoutSelect msg, Player player) {
		if (player.containerMenu instanceof SolderingIronMenu solderingIron) {
			ICircuitLayout layout = IForestryApi.INSTANCE.getCircuitManager().getLayout(msg.layoutUid);

			if (layout != null) {
				solderingIron.setLayout(layout);
			}
		}
	}
}
