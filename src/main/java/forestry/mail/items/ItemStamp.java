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
package forestry.mail.items;

import forestry.api.mail.EnumPostage;
import forestry.api.mail.IStamps;
import forestry.core.items.ItemOverlay;
import net.minecraft.world.item.ItemStack;

public class ItemStamp extends ItemOverlay implements IStamps {
	private final EnumStampDefinition def;

	public ItemStamp(EnumStampDefinition def) {
		super(def);
		this.def = def;
	}

	@Override
	public EnumPostage getPostage(ItemStack itemstack) {
		return this.def.getPostage();
	}
}
