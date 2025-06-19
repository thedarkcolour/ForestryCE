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
package forestry.mail.gui;

import forestry.Forestry;
import forestry.api.mail.*;
import forestry.api.modules.IForestryPacketClient;
import forestry.api.modules.IForestryPacketServer;
import forestry.core.gui.ItemInventoryMenu;
import forestry.core.gui.slots.SlotFiltered;
import forestry.mail.Letter;
import forestry.mail.MailAddress;
import forestry.mail.features.PostalCarriers;
import forestry.mail.carriers.trading.TradeStationRegistry;
import forestry.mail.features.MailMenuTypes;
import forestry.mail.inventory.ItemInventoryLetter;
import forestry.mail.network.packets.PacketLetterInfoResponsePlayer;
import forestry.mail.network.packets.PacketLetterInfoResponseTrader;
import forestry.mail.network.packets.PacketLetterTextSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Iterator;

public class LetterMenu extends ItemInventoryMenu<ItemInventoryLetter> implements ILetterInfoReceiver {
	private IPostalCarrier carrier = PostalCarriers.PLAYER.get();
	@Nullable
	private TradeStationInfo tradeInfo = null;

	public static LetterMenu fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
		InteractionHand hand = extraData.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		Player player = playerInv.player;
		ItemInventoryLetter inv = new ItemInventoryLetter(player, player.getItemInHand(hand));
		return new LetterMenu(windowId, player, inv);
	}

	public LetterMenu(int windowId, Player player, ItemInventoryLetter inventory) {
		super(MailMenuTypes.LETTER.menuType(), windowId, inventory, player.getInventory(), 17, 145);

		// Init slots

		// Stamps
		for (int i = 0; i < 4; i++) {
			addSlot(new SlotFiltered(inventory, Letter.SLOT_POSTAGE_1 + i, 150, 14 + i * 19).setStackLimit(1));
		}

		// Attachments
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 9; j++) {
				addSlot(new SlotFiltered(inventory, Letter.SLOT_ATTACHMENT_1 + j + i * 9, 17 + j * 18, 98 + i * 18));
			}
		}

		// Rip open delivered mails
		if (!player.level().isClientSide) {
			if (inventory.getLetter().isProcessed()) {
				inventory.onLetterOpened();
			}
		}

		// Set carrier
		ILetter letter = inventory.getLetter();
		IMailAddress recipient = letter.getRecipient();
		if (recipient != null) {
			this.carrier = recipient.getCarrier();
		}
	}

	@Override
	public void removed(Player playerEntity) {
		if (!playerEntity.level().isClientSide) {
			ILetter letter = this.inventory.getLetter();
			if (!letter.isProcessed()) {
				IMailAddress sender = new MailAddress(playerEntity.getGameProfile());
				letter.setSender(sender);
			}
		}

        this.inventory.onLetterClosed();

		super.removed(playerEntity);
	}

	public ILetter getLetter() {
		return this.inventory.getLetter();
	}

	public void setCarrier(IPostalCarrier carrier) {
		this.carrier = carrier;
	}

	public IPostalCarrier getCarrier() {
		return this.carrier;
	}

	public void advanceCarrierType() {
		Iterator<IPostalCarrier> it = PostalCarriers.REGISTRY.get().iterator();
		while (it.hasNext()) {
			if (it.next().equals(this.carrier)) {
				break;
			}
		}

		IPostalCarrier postal;
		if (it.hasNext()) {
			postal = it.next();
		} else {
			postal = PostalCarriers.REGISTRY.get().iterator().next();
		}

		setCarrier(postal);
	}

	public void handleRequestLetterInfo(Player player, String recipientName, IPostalCarrier carrier) {
		MinecraftServer server = player.getServer();
		if (server == null) {
			Forestry.LOGGER.error("Could not get server");
			return;
		}
		IMailAddress recipient = getRecipient(server, recipientName, carrier);

		getLetter().setRecipient(recipient);

		// Update the trading info
		if (recipient.getCarrier().equals(PostalCarriers.TRADER.get())) {
			updateTradeInfo(player.level(), recipient);
		}

		// TODO: Move this to the carrier to make it more extensible
		// Update info on client
		if (carrier.equals(PostalCarriers.PLAYER.get())) {
            IForestryPacketClient packet = new PacketLetterInfoResponsePlayer(recipient);
            PacketDistributor.sendToPlayer((ServerPlayer) player, packet);
        } else {
            IForestryPacketClient packet = new PacketLetterInfoResponseTrader(this.tradeInfo);
            PacketDistributor.sendToPlayer((ServerPlayer) player, packet);
        }
	}

	private static IMailAddress getRecipient(MinecraftServer minecraftServer, String recipientName, IPostalCarrier carrier) {
		return carrier.getRecipient(minecraftServer, recipientName);
	}

	@Nullable
	public IMailAddress getRecipient() {
		return getLetter().getRecipient();
	}

	public String getText() {
		return getLetter().getText();
	}

	@OnlyIn(Dist.CLIENT)
	public void setText(String text) {
		getLetter().setText(text);

		IForestryPacketServer packet = new PacketLetterTextSet(text);
		PacketDistributor.sendToServer(packet);
	}

	public void handleSetText(String text) {
		getLetter().setText(text);
	}

	/* Managing Trade info */
	private void updateTradeInfo(Level world, @Nullable IMailAddress address) {
		// Updating is done by the server.
		if (world.isClientSide) {
			return;
		}

		if (address == null) {
			setTradeInfo(null);
			return;
		}

		ITradeStation station = TradeStationRegistry.getOrCreate((ServerLevel) world).getTradeStation(address);
		if (station == null) {
			setTradeInfo(null);
			return;
		}

		setTradeInfo(station.getTradeInfo());
	}

	@Override
	public void handleLetterInfoUpdate(IPostalCarrier carrier, @Nullable IMailAddress address, @Nullable TradeStationInfo tradeInfo) {
		this.carrier = carrier;
		if (carrier.equals(PostalCarriers.PLAYER.get())) {
			getLetter().setRecipient(address);
		} else if (carrier.equals(PostalCarriers.TRADER.get())) {
			this.setTradeInfo(tradeInfo);
		}
	}

	@Nullable
	public TradeStationInfo getTradeInfo() {
		return this.tradeInfo;
	}

	private void setTradeInfo(@Nullable TradeStationInfo info) {
		this.tradeInfo = info;
		if (this.tradeInfo == null) {
			getLetter().setRecipient(null);
		} else {
			getLetter().setRecipient(this.tradeInfo.address());
		}
	}
}
