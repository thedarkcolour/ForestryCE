package forestry.storage.inventory;

import forestry.core.gui.IPagedInventory;
import forestry.storage.gui.ContainerNaturalistBackpack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

public class ItemInventoryBackpackPaged extends ItemInventoryBackpack implements IPagedInventory {
	private final ResourceLocation typeId;

	public ItemInventoryBackpackPaged(Player player, int size, ItemStack itemstack, ResourceLocation typeId) {
		super(player, size, itemstack);
		this.typeId = typeId;
	}

	@Override
	public void flipPage(ServerPlayer player, short page) {
		ItemStack backpack = getParent();
		SimpleMenuProvider provider = new SimpleMenuProvider((windowId, playerInv, p) -> ContainerNaturalistBackpack.makeContainer(windowId, p, backpack, page, this.typeId), backpack.getHoverName());
		NetworkHooks.openScreen(player, provider, buffer -> {
			buffer.writeByte(page);
			buffer.writeResourceLocation(this.typeId);
		});
	}
}
