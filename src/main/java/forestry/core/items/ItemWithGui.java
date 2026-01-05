package forestry.core.items;

import forestry.core.gui.ContainerItemInventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public abstract class ItemWithGui extends ItemForestry {
	public ItemWithGui(Item.Properties properties) {
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
		NetworkHooks.openScreen(serverPlayer, getMenuProvider(heldItem), buffer -> writeContainerData(serverPlayer, heldItem, buffer));
	}

	public SimpleMenuProvider getMenuProvider(ItemStack heldItem) {
		return new SimpleMenuProvider((windowId, playerInv, player) -> getContainer(windowId, player, heldItem), heldItem.getHoverName());
	}

	protected void writeContainerData(ServerPlayer player, ItemStack stack, FriendlyByteBuf buffer) {
		buffer.writeBoolean(player.getUsedItemHand() == InteractionHand.MAIN_HAND);
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack itemstack, Player player) {
		if (!itemstack.isEmpty() && player instanceof ServerPlayer && player.containerMenu instanceof ContainerItemInventory) {
			player.closeContainer();
		}

		return super.onDroppedByPlayer(itemstack, player);
	}

	@Nullable
	public abstract AbstractContainerMenu getContainer(int windowId, Player player, ItemStack heldItem);
}
