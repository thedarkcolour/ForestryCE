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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import forestry.api.mail.IMailAddress;
import forestry.api.mail.PostManager;
import forestry.core.config.ForestryConfig;
import forestry.core.utils.NetworkUtil;
import forestry.mail.gui.GuiMailboxInfo;
import forestry.mail.network.packets.PacketPOBoxInfoResponse;

// todo this shouldn't be a separate class
public class EventHandlerMailAlert {
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level != null && GuiMailboxInfo.INSTANCE.hasPOBoxInfo()) {
			if (ForestryConfig.CLIENT.mailAlertsEnabled.get()) {
				// todo find the necessary GuiGraphics object for this to render
				//GuiMailboxInfo.INSTANCE.render(new GuiGraphics());
			}
		}
	}

	@SubscribeEvent
	public void handlePlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();

		IMailAddress address = PostManager.postRegistry.getMailAddress(player.getGameProfile());
		POBox pobox = PostRegistry.getOrCreatePOBox((ServerLevel) player.level(), address);
		PacketPOBoxInfoResponse packet = new PacketPOBoxInfoResponse(pobox.getPOBoxInfo());
		NetworkUtil.sendToPlayer(packet, (ServerPlayer) player);
	}
}
