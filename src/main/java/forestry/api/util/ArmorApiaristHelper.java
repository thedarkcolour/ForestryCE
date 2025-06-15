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
package forestry.api.util;

import forestry.api.ForestryCapabilities;
import forestry.api.apiculture.IBeeProtection;
import forestry.api.apiculture.bee.IBeeEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ArmorApiaristHelper {
	public static boolean isArmorApiarist(ItemStack stack, LivingEntity entity, IBeeEffect cause, boolean execute) {
		IBeeProtection capability = stack.getCapability(ForestryCapabilities.BEE_PROTECTION);

		if (capability != null) {
			return capability.doBeeProtection(entity, stack, cause, execute);
		}
	}

	@Override
	public int wearsItems(LivingEntity entity, @Nullable IBeeEffect cause, boolean doProtect) {
		int count = 0;

		for (ItemStack armorItem : entity.getAllSlots()) {
			if (isArmorApiarist(armorItem, entity, cause, doProtect)) {
				count++;
			}
		}

		return count;
	}
}
