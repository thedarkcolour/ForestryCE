package forestry.core.items;

import forestry.api.core.IItemSubtype;
import forestry.core.items.definitions.IColoredItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Base class for items with an overlay color and multiple layer models.
 *
 * @see forestry.core.items.ItemElectronTube
 * @see forestry.apiculture.items.ItemPollenCluster
 * @see forestry.apiculture.items.ItemPropolis
 * @see forestry.mail.items.ItemStamp
 */
public class ItemOverlay extends ItemForestry implements IColoredItem {
	// Variant of subtype that has primary/secondary color fields
	public interface IOverlayInfo extends IItemSubtype {
		int getPrimaryColor();

		int getSecondaryColor();
	}

	protected final IOverlayInfo overlay;

	public ItemOverlay(CreativeModeTab tab, IOverlayInfo overlay) {
		super(new Item.Properties());

		this.overlay = overlay;
	}

	@Override
	public int getColorFromItemStack(ItemStack stack, int tintIndex) {
		if (tintIndex == 0 || this.overlay.getSecondaryColor() == 0) {
			return this.overlay.getPrimaryColor();
		} else {
			return this.overlay.getSecondaryColor();
		}
	}
}
