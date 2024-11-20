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

package forestry.storage;

import java.util.function.Predicate;

import com.google.common.base.Preconditions;
import forestry.api.storage.EnumBackpackType;
import forestry.api.storage.IBackpackDefinition;
import forestry.api.storage.IBackpackInterface;
import forestry.storage.items.ItemBackpack;
import forestry.storage.items.ItemBackpackNaturalist;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

// todo this code is stupid and bloated. next time it breaks, simplify it and remove the silly API for it
public class BackpackInterface implements IBackpackInterface {
	@Override
	public Item createBackpack(IBackpackDefinition definition, EnumBackpackType type) {
		Preconditions.checkNotNull(definition, "definition must not be null");
		Preconditions.checkNotNull(type, "type must not be null");
		Preconditions.checkArgument(type != EnumBackpackType.NATURALIST, "type must not be NATURALIST. Use createNaturalistBackpack instead.");

		return new ItemBackpack(definition, type);
	}

	@Override
	public Item createNaturalistBackpack(IBackpackDefinition definition, ResourceLocation speciesTypeId, CreativeModeTab tab) {
		Preconditions.checkNotNull(definition, "definition must not be null");
		Preconditions.checkNotNull(speciesTypeId, "rootUid must not be null");

		return new ItemBackpackNaturalist(speciesTypeId, definition, tab);
	}

	@Override
	public Predicate<ItemStack> createNaturalistBackpackFilter(ResourceLocation speciesRootUid) {
		return new BackpackFilterNaturalist(speciesRootUid);
	}
}
