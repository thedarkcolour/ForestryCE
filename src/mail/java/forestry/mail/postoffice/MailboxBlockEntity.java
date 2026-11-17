package forestry.mail.postoffice;

import com.mojang.authlib.GameProfile;
import forestry.api.mail.ILetter;
import forestry.api.mail.IMailAddress;
import forestry.api.mail.IPostalState;
import forestry.core.platform.inventory.InventoryAdapter;
import forestry.core.platform.tile.BaseBlockEntity;
import forestry.mail.letters.LetterUtils;
import forestry.mail.letters.MailAddress;
import forestry.mail.carriers.players.POBox;
import forestry.mail.carriers.players.POBoxRegistry;
import forestry.mail.features.MailBlockEntities;
import forestry.mail.gui.MailboxMenu;
import forestry.mail.letters.EnumDeliveryState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public class MailboxBlockEntity extends BaseBlockEntity {
	// virtual slot for mail, successful insertion immediately sends one letter
	private final IItemHandler automatedMailHandler = new IItemHandler() {
		@Override
		public int getSlots() {
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (slot != 0 || !isItemValid(slot, stack)) {
				return stack;
			}

			if (!simulate) {
				if (!(MailboxBlockEntity.this.level instanceof ServerLevel) || !tryDispatchLetter(stack.copyWithCount(1)).isOk()) {
					return stack;
				}
			}

			return stack.getCount() == 1 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - 1);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			if (slot != 0) {
				return false;
			}

			ILetter letter = LetterUtils.getLetter(stack);
			return letter != null && letter.isMailable() && letter.isPostPaid();
		}
	};

	public MailboxBlockEntity(BlockPos pos, BlockState state) {
		super(MailBlockEntities.MAILBOX.tileType(), pos, state);
		setInternalInventory(new InventoryAdapter(POBox.SLOT_SIZE, "Letters").disableAutomation());
	}

	/* GUI */
	@Override
	public void openGui(ServerPlayer player, InteractionHand hand, BlockPos pos) {
		if (this.level.isClientSide) {
			return;
		}

		ItemStack heldItem = player.getItemInHand(player.getUsedItemHand());
		// Handle letter sending
		if (LetterUtils.isLetter(heldItem)) {
			IPostalState result = this.tryDispatchLetter(heldItem);
			if (!result.isOk()) {
				player.sendSystemMessage(result.getDescription());
			} else {
				heldItem.shrink(1);
			}
		} else {
			super.openGui(player, hand, pos);
		}
	}

	/* MAIL HANDLING */
	public IItemHandler getAutomatedMailHandler() {
		return this.automatedMailHandler;
	}

	public Container getOrCreateMailInventory(Level world, GameProfile playerProfile) {
		if (world.isClientSide) {
			return getInternalInventory();
		}

		IMailAddress address = new MailAddress(playerProfile);
		return POBoxRegistry.getOrCreate((ServerLevel) world).getOrCreatePOBox(address);
	}

	private IPostalState tryDispatchLetter(ItemStack letterStack) {
		ILetter letter = LetterUtils.getLetter(letterStack);
		IPostalState result;

		if (letter != null) {
			//this is only called after !world.isRemote has been checked, so I believe the cast is OK
			ServerLevel world = (ServerLevel) this.level;
			result = PostOffice.getOrCreate(world).lodgeLetter(world, letterStack, true);
		} else {
			result = EnumDeliveryState.NOT_MAILABLE;
		}

		return result;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new MailboxMenu(windowId, inv, this);
	}
}
