/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.mail;

import forestry.mail.LetterUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.api.client.ITextureManager;

/**
 * Postal Carriers are systems which can be hooked into Forestry's mail system to handle mail delivery.
 * <p>
 * The two available carriers in vanilla Forestry are
 * "player" - Delivers mail to individual players.
 * "trader" - Handles mail addressed to trade stations.
 */
public interface IPostalCarrier {

	/**
	 * @return The translation key for the human-readable name for this carrier.
	 */
	String getDescriptionId();

	/**
	 * Sprite registered to the Gui Texture Map at {@link ITextureManager}.
	 * Must be 26 pixels wide and 15 pixels tall. The texture itself must be 32x32 square.
	 */
	@OnlyIn(Dist.CLIENT)
	TextureAtlasSprite getSprite();

	/**
	 * Handle delivery of a letter addressed to this carrier.
	 *
	 * @param world       The world the {@link IPostOffice} handles.
	 * @param office      {link @IPostOffice} which received this letter and handed it to the carrier.
	 * @param recipient   An identifier for the recipient as typed by the player into the address field.
	 * @param letterstack ItemStack representing the letter. See {@link LetterUtils} for helper functions to validate and extract it.
	 * @param doDeliver   Whether or not the letter is supposed to actually be delivered or if delivery is only to be simulated.
	 * @return {link IPostalState} holding information on success or failure for delivery.
	 */
	IPostalState deliverLetter(ServerLevel world, IPostOffice office, IMailAddress recipient, ItemStack letterstack, boolean doDeliver);

	IMailAddress getRecipient(MinecraftServer minecraftServer, String recipientName);
}
