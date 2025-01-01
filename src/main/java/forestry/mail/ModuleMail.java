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

import java.util.function.Consumer;

import forestry.api.mail.IMailAddress;
import forestry.core.utils.NetworkUtil;
import forestry.mail.carriers.PostalCarriers;
import forestry.mail.carriers.players.POBox;
import forestry.mail.carriers.players.POBoxRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IPacketRegistry;
import forestry.core.network.PacketIdClient;
import forestry.core.network.PacketIdServer;
import forestry.mail.client.MailClientHandler;
import forestry.mail.commands.CommandMail;
import forestry.mail.network.packets.PacketLetterInfoRequest;
import forestry.mail.network.packets.PacketLetterInfoResponsePlayer;
import forestry.mail.network.packets.PacketLetterInfoResponseTrader;
import forestry.mail.network.packets.PacketLetterTextSet;
import forestry.mail.network.packets.PacketPOBoxInfoResponse;
import forestry.mail.network.packets.PacketTraderAddressRequest;
import forestry.mail.network.packets.PacketTraderAddressResponse;
import forestry.modules.BlankForestryModule;

@ForestryModule
public class ModuleMail extends BlankForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.MAIL;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		MinecraftForge.EVENT_BUS.addListener(ModuleMail::handlePlayerLoggedIn);
		PostalCarriers.register(modBus);
	}

	public static void handlePlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		if (player.level.isClientSide) {
			return;
		}

		IMailAddress address = new MailAddress(player.getGameProfile());
		POBox pobox = POBoxRegistry.getOrCreate((ServerLevel) player.level).getOrCreatePOBox(address);
		PacketPOBoxInfoResponse packet = new PacketPOBoxInfoResponse(pobox.getPOBoxInfo(), false);
		NetworkUtil.sendToPlayer(packet, (ServerPlayer) player);
	}

	@Override
	public void addToRootCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(CommandMail.register());
	}

	@Override
	public void registerPackets(IPacketRegistry registry) {
		registry.serverbound(PacketIdServer.LETTER_INFO_REQUEST, PacketLetterInfoRequest.class, PacketLetterInfoRequest::decode, PacketLetterInfoRequest::handle);
		registry.serverbound(PacketIdServer.TRADING_ADDRESS_REQUEST, PacketTraderAddressRequest.class, PacketTraderAddressRequest::decode, PacketTraderAddressRequest::handle);
		registry.serverbound(PacketIdServer.LETTER_TEXT_SET, PacketLetterTextSet.class, PacketLetterTextSet::decode, PacketLetterTextSet::handle);

		registry.clientbound(PacketIdClient.LETTER_INFO_RESPONSE_PLAYER, PacketLetterInfoResponsePlayer.class, PacketLetterInfoResponsePlayer::decode, PacketLetterInfoResponsePlayer::handle);
		registry.clientbound(PacketIdClient.LETTER_INFO_RESPONSE_TRADER, PacketLetterInfoResponseTrader.class, PacketLetterInfoResponseTrader::decode, PacketLetterInfoResponseTrader::handle);
		registry.clientbound(PacketIdClient.TRADING_ADDRESS_RESPONSE, PacketTraderAddressResponse.class, PacketTraderAddressResponse::decode, PacketTraderAddressResponse::handle);
		registry.clientbound(PacketIdClient.POBOX_INFO_RESPONSE, PacketPOBoxInfoResponse.class, PacketPOBoxInfoResponse::decode, PacketPOBoxInfoResponse::handle);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new MailClientHandler());
	}
}
