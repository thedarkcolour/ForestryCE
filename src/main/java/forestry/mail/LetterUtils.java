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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import forestry.api.mail.ILetter;
import forestry.api.mail.IMailAddress;
import forestry.mail.features.MailItems;

public class LetterUtils {
	public static ILetter createLetter(IMailAddress sender, IMailAddress recipient) {
		return new Letter(sender, recipient);
	}

	public static ItemStack createLetterStack(ILetter letter) {
		CompoundTag compoundNBT = new CompoundTag();
		letter.write(compoundNBT);

		ItemStack letterStack = LetterProperties.createStampedLetterStack(letter);
		letterStack.setTag(compoundNBT);

		return letterStack;
	}

	@Nullable
	public static ILetter getLetter(ItemStack itemstack) {
		if (itemstack.isEmpty()) {
			return null;
		}

		if (!LetterUtils.isLetter(itemstack)) {
			return null;
		}

		if (itemstack.getTag() == null) {
			return null;
		}

		return new Letter(itemstack.getTag());
	}

	public static boolean isLetter(ItemStack itemstack) {
		return MailItems.LETTERS.itemEqual(itemstack);
	}
}
