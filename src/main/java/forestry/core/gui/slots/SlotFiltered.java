package forestry.core.gui.slots;

import com.mojang.datafixers.util.Pair;
import forestry.api.client.ForestrySprites;
import forestry.core.tiles.IFilterSlotDelegate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Slot which only takes specific items, specified by the IFilterSlotDelegate.
 */
public class SlotFiltered extends SlotWatched {
	private final IFilterSlotDelegate filterSlotDelegate;
	private Pair<ResourceLocation, ResourceLocation> blockedTexture = Pair.of(InventoryMenu.BLOCK_ATLAS, ForestrySprites.SLOT_BLOCKED);

	public <T extends Container & IFilterSlotDelegate> SlotFiltered(T inventory, int slotIndex, int xPos, int yPos) {
		super(inventory, slotIndex, xPos, yPos);
		this.filterSlotDelegate = inventory;
	}

	@Override
	public boolean mayPlace(ItemStack itemstack) {
		int slotIndex = getSlotIndex();
		return !this.filterSlotDelegate.isLocked(slotIndex) &&
			(itemstack.isEmpty() || this.filterSlotDelegate.canSlotAccept(slotIndex, itemstack));
	}

	public SlotFiltered setBlockedSprite(ResourceLocation sprite) {
		this.blockedTexture = Pair.of(InventoryMenu.BLOCK_ATLAS, sprite);
		return this;
	}

	public SlotFiltered setBackgroundSprite(ResourceLocation sprite) {
		setBackground(InventoryMenu.BLOCK_ATLAS, sprite);
		return this;
	}

	// (Atlas location, Sprite location)
	@Nullable
	@Override
	public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
		return !mayPlace(getItem()) ? this.blockedTexture : super.getNoItemIcon();
	}
}
