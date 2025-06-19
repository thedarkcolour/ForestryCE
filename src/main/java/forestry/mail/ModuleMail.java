package forestry.mail;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import forestry.api.client.IClientModuleHandler;
import forestry.api.mail.IMailAddress;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.core.network.PacketIdClient;
import forestry.core.network.PacketIdServer;
import forestry.mail.carriers.players.POBox;
import forestry.mail.carriers.players.POBoxRegistry;
import forestry.mail.client.MailClientHandler;
import forestry.mail.commands.CommandMail;
import forestry.mail.network.packets.*;
import forestry.modules.BlankForestryModule;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Consumer;

@ForestryModule
public class ModuleMail extends BlankForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.MAIL;
	}

	@Override
	public void registerEvents(IEventBus modBus) {
		NeoForge.EVENT_BUS.addListener(ModuleMail::handlePlayerLoggedIn);
	}

	public static void handlePlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		if (player.level().isClientSide) {
			return;
		}

		IMailAddress address = new MailAddress(player.getGameProfile());
		POBox pobox = POBoxRegistry.getOrCreate((ServerLevel) player.level()).getOrCreatePOBox(address);
		PacketPOBoxInfoResponse packet = new PacketPOBoxInfoResponse(pobox.getPOBoxInfo(), false);
		PacketDistributor.sendToPlayer((ServerPlayer) player, packet);
	}

	@Override
	public void addToRootCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(CommandMail.register());
	}

	@Override
	public void registerPackets(PayloadRegistrar registrar) {
		registrar.playToServer(PacketIdServer.LETTER_INFO_REQUEST, StreamCodec.of(PacketLetterInfoRequest::encode, PacketLetterInfoRequest::decode), PacketLetterInfoRequest::handle);
		registrar.playToServer(PacketIdServer.TRADING_ADDRESS_REQUEST, StreamCodec.of(PacketTraderAddressRequest::encode, PacketTraderAddressRequest::decode), PacketTraderAddressRequest::handle);
		registrar.playToServer(PacketIdServer.LETTER_TEXT_SET, StreamCodec.of(PacketLetterTextSet::encode, PacketLetterTextSet::decode), PacketLetterTextSet::handle);

		registrar.playToClient(PacketIdClient.LETTER_INFO_RESPONSE_PLAYER, StreamCodec.of(PacketLetterInfoResponsePlayer::encode, PacketLetterInfoResponsePlayer::decode), PacketLetterInfoResponsePlayer::handle);
		registrar.playToClient(PacketIdClient.LETTER_INFO_RESPONSE_TRADER, StreamCodec.of(PacketLetterInfoResponseTrader::encode, PacketLetterInfoResponseTrader::decode), PacketLetterInfoResponseTrader::handle);
		registrar.playToClient(PacketIdClient.TRADING_ADDRESS_RESPONSE, StreamCodec.of(PacketTraderAddressResponse::encode, PacketTraderAddressResponse::decode), PacketTraderAddressResponse::handle);
		registrar.playToClient(PacketIdClient.POBOX_INFO_RESPONSE, StreamCodec.of(PacketPOBoxInfoResponse::encode, PacketPOBoxInfoResponse::decode), PacketPOBoxInfoResponse::handle);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new MailClientHandler());
	}
}
