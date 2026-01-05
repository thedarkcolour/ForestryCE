package forestry.mail;

import forestry.api.mail.*;
import forestry.mail.features.MailItems;
import forestry.mail.items.EnumStampDefinition;
import forestry.mail.postalstates.EnumDeliveryState;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

public class PostOffice extends SavedData implements IPostOffice {
	public static final String SAVE_NAME = "forestry_mail";
	private final int[] collectedPostage = new int[EnumPostage.values().length];

	public PostOffice() {
	}

	public PostOffice(CompoundTag tag) {
		for (int i = 0; i < this.collectedPostage.length; i++) {
			if (tag.contains("CPS" + i)) {
                this.collectedPostage[i] = tag.getInt("CPS" + i);
			}
		}
	}

	@Override
	public CompoundTag save(CompoundTag compoundNBT) {
		for (int i = 0; i < this.collectedPostage.length; i++) {
			compoundNBT.putInt("CPS" + i, this.collectedPostage[i]);
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
			int collected = Math.min(max, this.collectedPostage[postage.ordinal()]);
            this.collectedPostage[postage.ordinal()] -= collected;

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
                this.collectedPostage[postage.ordinal()] += stamp.getCount();
			}
		}
	}

	public static PostOffice getOrCreate(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(PostOffice::new, PostOffice::new, PostOffice.SAVE_NAME);
	}
}
