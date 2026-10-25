package forestry.core.platform.item;

import forestry.core.platform.gui.ItemInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public abstract class WithScreenItem extends ItemForestry {
	public WithScreenItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, InteractionHand handIn) {
		ItemStack stack = player.getItemInHand(handIn);

		if (player instanceof ServerPlayer serverPlayer) {
			openGui(serverPlayer, stack);
		}

		return InteractionResultHolder.success(stack);
	}

	protected void openGui(ServerPlayer serverPlayer, ItemStack heldItem) {
		serverPlayer.openMenu(getMenuProvider(heldItem), buffer -> writeContainerData(serverPlayer, heldItem, buffer));
	}

	public SimpleMenuProvider getMenuProvider(ItemStack heldItem) {
		return new SimpleMenuProvider((windowId, playerInv, player) -> getContainer(windowId, player, heldItem), heldItem.getHoverName());
	}

	protected void writeContainerData(ServerPlayer player, ItemStack stack, RegistryFriendlyByteBuf buffer) {
		buffer.writeBoolean(player.getUsedItemHand() == InteractionHand.MAIN_HAND);
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack itemstack, Player player) {
		if (!itemstack.isEmpty() && player instanceof ServerPlayer && player.containerMenu instanceof ItemInventoryMenu) {
			player.closeContainer();
		}

		return super.onDroppedByPlayer(itemstack, player);
	}

	@Nullable
	public abstract AbstractContainerMenu getContainer(int containerId, Player player, ItemStack heldItem);
}
