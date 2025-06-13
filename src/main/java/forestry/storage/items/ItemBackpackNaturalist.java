package forestry.storage.items;

import forestry.api.storage.EnumBackpackType;
import forestry.api.storage.IBackpackDefinition;
import forestry.storage.gui.NaturalistBackpackMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class ItemBackpackNaturalist extends ItemBackpack {
	public final ResourceLocation typeId;

	public ItemBackpackNaturalist(ResourceLocation typeId, IBackpackDefinition definition) {
		super(definition, EnumBackpackType.NATURALIST);
		this.typeId = typeId;
	}

	@Override
	protected void writeContainerData(RegistryFriendlyByteBuf buffer, Player player, ItemStack stack, int slotIndex) {
		// Item slot index
		buffer.writeByte(slotIndex);
		// Page number
		buffer.writeByte(0);
		// Species type
		buffer.writeResourceLocation(this.typeId);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, int slotIndex) {
		return NaturalistBackpackMenu.makeContainer(windowId, playerInv, slotIndex, 0, this.typeId);
	}
}
