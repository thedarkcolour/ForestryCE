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
package forestry.arboriculture.items;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import forestry.api.arboriculture.IWoodType;
import forestry.arboriculture.IWoodTyped;
import forestry.arboriculture.WoodHelper;
import forestry.core.items.ItemBlockForestry;

public class ItemBlockWood<B extends Block & IWoodTyped> extends ItemBlockForestry<B> {
	private final IWoodTyped wood;
	private final IWoodType woodType;

	public ItemBlockWood(B block) {
		super(block, new Item.Properties());

		// Safeguard against Diagonal Fence's registry replacements causing crashes
		this.wood = block;
		this.woodType = block.getWoodType();
	}

	@Override
	public Component getName(ItemStack itemstack) {
		// todo use vanilla names and data generation instead of this
		return WoodHelper.getDisplayName(this.wood, this.woodType);
	}

	@Override
	public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
		if (this.wood.isFireproof()) {
			return 0;
		} else {
			return 300;
		}
	}
}
