package forestry.core.items;

import forestry.core.gui.ItemInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public abstract class ItemWithGui extends ItemForestry {
	public ItemWithGui(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!level.isClientSide) {
			// keep track of the item's slot so we can lock it
			int slotIndex = hand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : player.getInventory().selected;

			player.openMenu(new SimpleMenuProvider(
					(windowId, playerInv, p) -> createMenu(windowId, playerInv, slotIndex),
					player.getItemInHand(hand).getHoverName()
				),
				buffer -> writeContainerData(buffer, player, stack, slotIndex)
			);

			return InteractionResultHolder.consume(stack);
		} else {
			return InteractionResultHolder.success(stack);
		}
	}

	protected void writeContainerData(RegistryFriendlyByteBuf buffer, Player player, ItemStack stack, int slotIndex) {
		buffer.writeByte(slotIndex);
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack itemstack, Player player) {
		if (!itemstack.isEmpty() && player instanceof ServerPlayer && player.containerMenu instanceof ItemInventoryMenu) {
			player.closeContainer();
		}

		return super.onDroppedByPlayer(itemstack, player);
	}

	@Nullable
	public abstract AbstractContainerMenu createMenu(int containerId, Inventory playerInv, int slotIndex);
}
