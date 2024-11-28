/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.mail;

import javax.annotation.Nullable;
import java.util.Map;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import com.mojang.authlib.GameProfile;

public interface IPostRegistry {
	/* POST OFFICE */
	IPostOffice getPostOffice(ServerLevel level);

	/* MAIL ADDRESSES */
	IMailAddress createMailAddress(GameProfile gameProfile);

	IMailAddress createMailAddress(String traderName);

	/* LETTERS */
	boolean isLetter(ItemStack itemstack);

	ILetter createLetter(IMailAddress sender, IMailAddress recipient);

	@Nullable
	ILetter getLetter(ItemStack itemstack);

	ItemStack createLetterStack(ILetter letter);

	/* CARRIERS */

	/**
	 * Registers a new {@link IPostalCarrier}. See {@link IPostalCarrier} for details.
	 *
	 * @param carrier {@link IPostalCarrier} to register.
	 */
	void registerCarrier(IPostalCarrier carrier);

	@Nullable
	IPostalCarrier getCarrier(EnumAddressee uid);

	Map<EnumAddressee, IPostalCarrier> getRegisteredCarriers();

	/* TRADE STATIONS */
	void deleteTradeStation(ServerLevel world, IMailAddress address);

	ITradeStation getOrCreateTradeStation(ServerLevel world, GameProfile owner, IMailAddress address);

	@Nullable
	ITradeStation getTradeStation(ServerLevel world, IMailAddress address);

	/**
	 * Determines if a mailing address is not already in use.
	 *
	 * @param world   the Minecraft world the Trader will be in
	 * @param address the potential address of the Trader
	 * @return {@code true} if the trade address has not yet been used before.
	 */
	boolean isAvailableTradeAddress(ServerLevel world, IMailAddress address);

	/**
	 * Determines whether a mailing address is valid for a Trade Station.
	 *
	 * @param address the potential address of the Trader
	 * @return {@code true} if the address is alphanumeric and is for a trade station
	 */
	boolean isValidTradeAddress(IMailAddress address);

	/**
	 * Determines whether a mailing address is valid for a player PO box.
	 *
	 * @param address the potential address of the PO box
	 * @return {@code true} if the passed address is valid for PO Boxes.
	 */
	boolean isValidPOBox(IMailAddress address);
}
