package forestry.mail.tiles;

import com.mojang.authlib.GameProfile;
import forestry.api.mail.ILetter;
import forestry.api.mail.IMailAddress;
import forestry.api.mail.IPostalState;
import forestry.core.inventory.InventoryAdapter;
import forestry.core.tiles.TileBase;
import forestry.mail.LetterUtils;
import forestry.mail.MailAddress;
import forestry.mail.PostOffice;
import forestry.mail.carriers.players.POBox;
import forestry.mail.carriers.players.POBoxRegistry;
import forestry.mail.features.MailTiles;
import forestry.mail.gui.ContainerMailbox;
import forestry.mail.postalstates.EnumDeliveryState;
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

public class TileMailbox extends TileBase {

	public TileMailbox(BlockPos pos, BlockState state) {
		super(MailTiles.MAILBOX.tileType(), pos, state);
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
		return new ContainerMailbox(windowId, inv, this);
	}
}
