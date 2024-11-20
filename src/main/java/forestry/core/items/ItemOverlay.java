/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.core.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import forestry.api.core.IItemSubtype;
import forestry.core.items.definitions.IColoredItem;

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
		if (tintIndex == 0 || overlay.getSecondaryColor() == 0) {
			return overlay.getPrimaryColor();
		} else {
			return overlay.getSecondaryColor();
		}
	}
}
