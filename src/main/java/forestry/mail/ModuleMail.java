package forestry.mail;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import forestry.api.client.IClientModuleHandler;
import forestry.api.mail.IMailAddress;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.modules.IPacketRegistry;
import forestry.core.network.PacketIdClient;
import forestry.core.network.PacketIdServer;
import forestry.core.utils.NetworkUtil;
import forestry.mail.carriers.PostalCarriers;
import forestry.mail.carriers.players.POBox;
import forestry.mail.carriers.players.POBoxRegistry;
import forestry.mail.client.MailClientHandler;
import forestry.mail.commands.CommandMail;
import forestry.mail.network.packets.*;
import forestry.modules.BlankForestryModule;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.function.Consumer;

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
		if (player.level().isClientSide) {
			return;
		}

		IMailAddress address = new MailAddress(player.getGameProfile());
		POBox pobox = POBoxRegistry.getOrCreate((ServerLevel) player.level()).getOrCreatePOBox(address);
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
