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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import forestry.mail.carriers.PostalCarriers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.mojang.authlib.GameProfile;

import forestry.Forestry;
import forestry.api.mail.ILetter;
import forestry.api.mail.IMailAddress;
import forestry.api.mail.IPostOffice;
import forestry.api.mail.IPostRegistry;
import forestry.api.mail.ITradeStation;
import forestry.api.mail.PostManager;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.PlayerUtil;
import forestry.mail.features.MailItems;
import forestry.mail.network.packets.PacketPOBoxInfoResponse;

public class PostRegistry implements IPostRegistry {
	@Nullable
	public static PostOffice cachedPostOffice;
	public static final Map<IMailAddress, POBox> cachedPOBoxes = new HashMap<>();
	public static final Map<IMailAddress, ITradeStation> cachedTradeStations = new HashMap<>();


	/**
	 * @param world   the Minecraft world the PO box will be in
	 * @param address the potential address of the PO box
	 * @return true if the passed address is valid for PO Boxes.
	 */
	@Override
	public boolean isValidPOBox(Level world, IMailAddress address) {
		return address.getCarrier().equals(PostalCarriers.PLAYER.get()) && address.getName().matches("^[a-zA-Z0-9]+$");
	}

	@Nullable
	public static POBox getPOBox(ServerLevel world, IMailAddress address) {
		if (cachedPOBoxes.containsKey(address)) {
			return cachedPOBoxes.get(address);
		}

		return world.getDataStorage().get(POBox::new, POBox.SAVE_NAME + address);
	}

	public static POBox getOrCreatePOBox(ServerLevel world, IMailAddress add) {
		POBox pobox = getPOBox(world, add);

		if (pobox == null) {
			pobox = world.getDataStorage().computeIfAbsent(POBox::new, () -> new POBox(add), POBox.SAVE_NAME + add);

			pobox.setDirty();
			cachedPOBoxes.put(add, pobox);
		}

		return pobox;
	}

	/**
	 * @param world   the Minecraft world the Trader will be in
	 * @param address the potential address of the Trader
	 * @return true if the passed address can be an address for a trade station
	 */
	@Override
	public boolean isValidTradeAddress(Level world, IMailAddress address) {
		return address.getCarrier().equals(PostalCarriers.TRADER.get()) && address.getName().matches("^[a-zA-Z0-9]+$");
	}

	/**
	 * @param world   the Minecraft world the Trader will be in
	 * @param address the potential address of the Trader
	 * @return true if the trade address has not yet been used before.
	 */
	@Override
	public boolean isAvailableTradeAddress(ServerLevel world, IMailAddress address) {
		return getTradeStation(world, address) == null;
	}

	private void registerTradeStation(ServerLevel level, IMailAddress address, ITradeStation station) {
		cachedTradeStations.put(address, station);
		getPostOffice(level).registerTradeStation(station);
	}

	@Override
	public TradeStation getTradeStation(ServerLevel world, IMailAddress address) {
		if (cachedTradeStations.containsKey(address)) {
			return (TradeStation) cachedTradeStations.get(address);
		}

		TradeStation trade = world.getDataStorage().get(TradeStation::new, TradeStation.SAVE_NAME + address);

		// Only existing and valid mail orders are returned
		if (trade != null && trade.isValid()) {
			registerTradeStation(world, address, trade);
			return trade;
		}

		return null;
	}

	@Override
	public TradeStation getOrCreateTradeStation(ServerLevel world, GameProfile owner, IMailAddress address) {
		TradeStation trade = getTradeStation(world, address);

		if (trade == null) {
			trade = world.getDataStorage().computeIfAbsent(TradeStation::new, () -> new TradeStation(owner, address), TradeStation.SAVE_NAME + address);
			trade.setDirty();
			registerTradeStation(world, address, trade);
		}

		return trade;
	}

	@Override
	public void deleteTradeStation(ServerLevel world, IMailAddress address) {
		TradeStation trade = getTradeStation(world, address);
		if (trade == null) {
			return;
		}
		// TODO: Clean this up or migrate to save in a single file, as deleting seems not possible for now
		// Need to be marked as invalid since WorldSavedData seems to do some caching of its own.
		trade.invalidate();
		cachedTradeStations.remove(address);
		getPostOffice(world).deregisterTradeStation(trade);
		//		File file = world.getSaveHandler().getMapFileFromName(trade.getName());	//TODO which file?
		boolean delete = false; //TODO fix file.delete();
		if (!delete) {
			Forestry.LOGGER.error("Failed to delete trade station file. {}", "FIXME!");//file);
		}
	}

	@Override
	public IPostOffice getPostOffice(ServerLevel world) {
		if (cachedPostOffice != null) {
			return cachedPostOffice;
		}

		PostOffice office = world.getDataStorage().computeIfAbsent(PostOffice::new, PostOffice::new, PostOffice.SAVE_NAME);

		office.setWorld(world);

		cachedPostOffice = office;
		return office;
	}

	/* LETTERS */
	@Override
	public ILetter createLetter(IMailAddress sender, IMailAddress recipient) {
		return new Letter(sender, recipient);
	}

	@Override
	public ItemStack createLetterStack(ILetter letter) {
		CompoundTag compoundNBT = new CompoundTag();
		letter.write(compoundNBT);

		ItemStack letterStack = LetterProperties.createStampedLetterStack(letter);
		letterStack.setTag(compoundNBT);

		return letterStack;
	}

	@Override
	@Nullable
	public ILetter getLetter(ItemStack itemstack) {
		if (itemstack.isEmpty()) {
			return null;
		}

		if (!PostManager.postRegistry.isLetter(itemstack)) {
			return null;
		}

		if (itemstack.getTag() == null) {
			return null;
		}

		return new Letter(itemstack.getTag());
	}

	@Override
	public boolean isLetter(ItemStack itemstack) {
		return MailItems.LETTERS.itemEqual(itemstack);
	}
}
