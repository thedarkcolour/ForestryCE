package forestry.mail.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import forestry.api.core.ForestryError;
import forestry.api.core.IError;
import forestry.api.core.IErrorSource;
import forestry.api.mail.ILetter;
import forestry.core.inventory.ItemInventory;
import forestry.core.items.ItemWithGui;
import forestry.core.utils.SlotUtil;
import forestry.mail.Letter;
import forestry.mail.LetterProperties;
import forestry.mail.items.ItemStamp;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemInventoryLetter extends ItemInventory implements IErrorSource {
	private final ILetter letter;

	public ItemInventoryLetter(Player player, ItemStack itemstack) {
		super(player, 0, itemstack);
		CompoundTag tagCompound = itemstack.getTag();
		Preconditions.checkNotNull(tagCompound);
        this.letter = new Letter(tagCompound);
	}

	public ILetter getLetter() {
		return this.letter;
	}

	public void onLetterClosed() {
		ItemStack parent = getParent();
		setParent(LetterProperties.closeLetter(parent, this.letter));
	}

	public void onLetterOpened() {
		ItemStack parent = getParent();
		setParent(LetterProperties.openLetter(parent));
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		ItemStack result = this.letter.removeItem(index, count);
		CompoundTag tagCompound = getParent().getTag();
		Preconditions.checkNotNull(tagCompound);
        this.letter.write(tagCompound);
		return result;
	}

	@Override
	public void setItem(int index, ItemStack itemstack) {
        this.letter.setItem(index, itemstack);
		CompoundTag tagCompound = getParent().getTag();
		Preconditions.checkNotNull(tagCompound);
        this.letter.write(tagCompound);
	}

	@Override
	public ItemStack getItem(int i) {
		return this.letter.getItem(i);
	}

	@Override
	public int getContainerSize() {
		return this.letter.getContainerSize();
	}

	@Override
	public int getMaxStackSize() {
		return this.letter.getMaxStackSize();
	}

	@Override
	public boolean stillValid(Player player) {
		return this.letter.stillValid(player);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return this.letter.removeItemNoUpdate(slot);
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (this.letter.isProcessed()) {
			return false;
		} else if (SlotUtil.isSlotInRange(slotIndex, Letter.SLOT_POSTAGE_1, Letter.SLOT_POSTAGE_COUNT)) {
			Item item = stack.getItem();
			return item instanceof ItemStamp;
		} else if (SlotUtil.isSlotInRange(slotIndex, Letter.SLOT_ATTACHMENT_1, Letter.SLOT_ATTACHMENT_COUNT)) {
			return !(stack.getItem() instanceof ItemWithGui);
		}
		return false;
	}

	/* IErrorSource */
	@Override
	public ImmutableSet<IError> getErrors() {

		ImmutableSet.Builder<IError> errorStates = ImmutableSet.builder();

		if (!this.letter.hasRecipient()) {
			errorStates.add(ForestryError.NO_RECIPIENT);
		}

		if (!this.letter.isProcessed() && !this.letter.isPostPaid()) {
			errorStates.add(ForestryError.NOT_POST_PAID);
		}

		return errorStates.build();
	}
}
