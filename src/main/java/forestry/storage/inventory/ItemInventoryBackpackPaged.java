package forestry.storage.inventory;

import forestry.core.gui.IPagedInventory;
import forestry.storage.gui.NaturalistBackpackMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;

public class ItemInventoryBackpackPaged extends ItemInventoryBackpack implements IPagedInventory {
	private final ResourceLocation typeId;

	public ItemInventoryBackpackPaged(int size, ItemStack itemstack, ResourceLocation typeId) {
		super(size, itemstack);
		this.typeId = typeId;
	}

	@Override
	public void flipPage(ServerPlayer player, short page) {
		ItemStack backpack = getParent();
		SimpleMenuProvider provider = new SimpleMenuProvider((windowId, playerInv, p) -> NaturalistBackpackMenu.makeContainer(windowId, p, backpack, page, this.typeId), backpack.getHoverName());
		player.openMenu(provider, buffer -> {
			buffer.writeByte(page);
			buffer.writeResourceLocation(this.typeId);
		});
	}
}
