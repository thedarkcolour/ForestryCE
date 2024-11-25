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
package forestry.mail.carriers;

import forestry.api.ForestryConstants;
import forestry.api.client.IForestryClientApi;
import forestry.api.mail.*;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.PlayerUtil;
import forestry.mail.EnumDeliveryState;
import forestry.mail.MailAddress;
import forestry.mail.POBox;
import forestry.mail.PostRegistry;
import forestry.mail.network.packets.PacketPOBoxInfoResponse;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CarrierPlayer implements IPostalCarrier {
	private final ResourceLocation iconID;

	public CarrierPlayer() {
		this.iconID = ForestryConstants.forestry("mail/carrier.player");
	}

	@Override
	public String getDescriptionId() {
		return "for.gui.addressee.player";
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TextureAtlasSprite getSprite() {
		return IForestryClientApi.INSTANCE.getTextureManager().getSprite(iconID);
	}

	@Override
	public IPostalState deliverLetter(ServerLevel world, IPostOffice office, IMailAddress recipient, ItemStack letterStack, boolean doDeliver) {
		POBox pobox = PostRegistry.getOrCreatePOBox(world, recipient);
		if (pobox == null) {
			return EnumDeliveryState.NO_MAILBOX;
		}

		if (!pobox.storeLetter(letterStack.copy())) {
			return EnumDeliveryState.MAILBOX_FULL;
		} else {
			Player player = PlayerUtil.getPlayer(world, recipient.getPlayerProfile());
			if (player instanceof ServerPlayer) {
				NetworkUtil.sendToPlayer(new PacketPOBoxInfoResponse(pobox.getPOBoxInfo(), false), (ServerPlayer) player);
			}
		}

		return EnumDeliveryState.OK;
	}

	@Override
	public IMailAddress getRecipient(MinecraftServer minecraftServer, String recipientName) {
		return minecraftServer.getProfileCache().get(recipientName).map(MailAddress::new).orElse(null);
	}

	@Override
	public String toString() {
		return "player";
	}
}
