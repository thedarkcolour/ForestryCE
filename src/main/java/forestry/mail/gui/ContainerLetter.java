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

import javax.annotation.Nullable;
import java.util.Iterator;

import forestry.api.mail.*;
import forestry.mail.*;
import forestry.mail.carriers.PostalCarriers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.Forestry;
import forestry.api.mail.PostManager;
import forestry.core.gui.ContainerItemInventory;
import forestry.core.gui.slots.SlotFiltered;
import forestry.core.utils.NetworkUtil;
import forestry.mail.features.MailMenuTypes;
import forestry.mail.inventory.ItemInventoryLetter;
import forestry.mail.network.packets.PacketLetterInfoResponsePlayer;
import forestry.mail.network.packets.PacketLetterInfoResponseTrader;
import forestry.mail.network.packets.PacketLetterTextSet;

public class ContainerLetter extends ContainerItemInventory<ItemInventoryLetter> implements ILetterInfoReceiver {
	private IPostalCarrier carrier = PostalCarriers.PLAYER.get();
	@Nullable
	private ITradeStationInfo tradeInfo = null;

	public static ContainerLetter fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
		InteractionHand hand = extraData.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		Player player = playerInv.player;
		ItemInventoryLetter inv = new ItemInventoryLetter(player, player.getItemInHand(hand));
		return new ContainerLetter(windowId, player, inv);
	}

	public ContainerLetter(int windowId, Player player, ItemInventoryLetter inventory) {
		super(windowId, inventory, player.getInventory(), 17, 145, MailMenuTypes.LETTER.menuType());

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
		if (!player.level.isClientSide) {
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

		if (!playerEntity.level.isClientSide) {
			ILetter letter = inventory.getLetter();
			if (!letter.isProcessed()) {
				IMailAddress sender = new MailAddress(playerEntity.getGameProfile());
				letter.setSender(sender);
			}
		}

		inventory.onLetterClosed();

		super.removed(playerEntity);
	}

	public ILetter getLetter() {
		return inventory.getLetter();
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
			if (it.next().equals(carrier)) {
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
			updateTradeInfo(player.level, recipient);
		}

		// TODO: Move this to the carrier to make it more extensible
		// Update info on client
		if (carrier.equals(PostalCarriers.PLAYER.get())) {
            NetworkUtil.sendToPlayer(new PacketLetterInfoResponsePlayer(recipient), (ServerPlayer) player);
        } else {
			NetworkUtil.sendToPlayer(new PacketLetterInfoResponseTrader(tradeInfo), (ServerPlayer) player);
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

		NetworkUtil.sendToServer(new PacketLetterTextSet(text));
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

		ITradeStation station = PostManager.postRegistry.getTradeStation((ServerLevel) world, address);
		if (station == null) {
			setTradeInfo(null);
			return;
		}

		setTradeInfo(station.getTradeInfo());
	}

	@Override
	public void handleLetterInfoUpdate(IPostalCarrier carrier, @Nullable IMailAddress address, @Nullable ITradeStationInfo tradeInfo) {
		this.carrier = carrier;
		if (carrier.equals(PostalCarriers.PLAYER.get())) {
			getLetter().setRecipient(address);
		} else if (carrier.equals(PostalCarriers.TRADER.get())) {
			this.setTradeInfo(tradeInfo);
		}
	}

	@Nullable
	public ITradeStationInfo getTradeInfo() {
		return this.tradeInfo;
	}

	private void setTradeInfo(@Nullable ITradeStationInfo info) {
		this.tradeInfo = info;
		if (tradeInfo == null) {
			getLetter().setRecipient(null);
		} else {
			getLetter().setRecipient(tradeInfo.address());
		}
	}
}
