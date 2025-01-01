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

import forestry.mail.postalstates.EnumDeliveryState;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import forestry.api.mail.EnumPostage;
import forestry.api.mail.ILetter;
import forestry.api.mail.IMailAddress;
import forestry.api.mail.IPostOffice;
import forestry.api.mail.IPostalCarrier;
import forestry.api.mail.IPostalState;
import forestry.api.mail.IStamps;
import forestry.mail.features.MailItems;
import forestry.mail.items.EnumStampDefinition;

public class PostOffice extends SavedData implements IPostOffice {

	// / CONSTANTS
	public static final String SAVE_NAME = "forestry_mail";
	private final int[] collectedPostage = new int[EnumPostage.values().length];

	// CONSTRUCTORS
	public PostOffice() {
	}

	public PostOffice(CompoundTag tag) {
		for (int i = 0; i < collectedPostage.length; i++) {
			if (tag.contains("CPS" + i)) {
				collectedPostage[i] = tag.getInt("CPS" + i);
			}
		}
	}

	@Override
	public CompoundTag save(CompoundTag compoundNBT) {
		for (int i = 0; i < collectedPostage.length; i++) {
			compoundNBT.putInt("CPS" + i, collectedPostage[i]);
		}
		return compoundNBT;
	}

	// / STAMP MANAGMENT
	@Override
	public ItemStack getAnyStamp(int max) {
		return getAnyStamp(EnumPostage.values(), max);
	}

	@Override
	public ItemStack getAnyStamp(EnumPostage postage, int max) {
		return getAnyStamp(new EnumPostage[]{postage}, max);
	}

	@Override
	public ItemStack getAnyStamp(EnumPostage[] postages, int max) {
		for (EnumPostage postage : postages) {
			int collected = Math.min(max, collectedPostage[postage.ordinal()]);
			collectedPostage[postage.ordinal()] -= collected;

			if (collected > 0) {
				EnumStampDefinition stampDefinition = EnumStampDefinition.getFromPostage(postage);
				return MailItems.STAMPS.stack(stampDefinition, collected);
			}
		}

		return ItemStack.EMPTY;
	}

	// / DELIVERY
	@Override
	public IPostalState lodgeLetter(ServerLevel world, ItemStack itemstack, boolean doLodge) {
		ILetter letter = LetterUtils.getLetter(itemstack);
		if (letter == null) {
			return EnumDeliveryState.NOT_MAILABLE;
		}

		if (letter.isProcessed()) {
			return EnumDeliveryState.ALREADY_MAILED;
		}

		if (!letter.isPostPaid()) {
			return EnumDeliveryState.NOT_POSTPAID;
		}

		if (!letter.isMailable()) {
			return EnumDeliveryState.NOT_MAILABLE;
		}

		IPostalState state = EnumDeliveryState.NOT_MAILABLE;
		IMailAddress address = letter.getRecipient();
		if (address != null) {
			IPostalCarrier carrier = address.getCarrier();
            state = carrier.deliverLetter(world, this, address, itemstack, doLodge);
        }

		if (!state.isOk()) {
			return state;
		}

		collectPostage(letter.getPostage());

		setDirty();
		return EnumDeliveryState.OK;

	}

	@Override
	public void collectPostage(NonNullList<ItemStack> stamps) {
		for (ItemStack stamp : stamps) {
			if (stamp == null) {
				continue;
			}

			if (stamp.getItem() instanceof IStamps) {
				EnumPostage postage = ((IStamps) stamp.getItem()).getPostage(stamp);
				collectedPostage[postage.ordinal()] += stamp.getCount();
			}
		}
	}

	public static PostOffice getOrCreate(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(PostOffice::new, PostOffice::new, PostOffice.SAVE_NAME);
	}
}
