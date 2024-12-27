/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.mail;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;

public interface IPostRegistry {

	/* POST OFFICE */
	IPostOffice getPostOffice(ServerLevel world);

	/* LETTERS */
	boolean isLetter(ItemStack itemstack);

	ILetter createLetter(IMailAddress sender, IMailAddress recipient);

	@Nullable
	ILetter getLetter(ItemStack itemstack);

	ItemStack createLetterStack(ILetter letter);
}
