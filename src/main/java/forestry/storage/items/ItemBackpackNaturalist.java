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
package forestry.storage.items;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import forestry.api.storage.EnumBackpackType;
import forestry.api.storage.IBackpackDefinition;
import forestry.storage.gui.ContainerNaturalistBackpack;

public class ItemBackpackNaturalist extends ItemBackpack {
	public final ResourceLocation typeId;

	public ItemBackpackNaturalist(ResourceLocation typeId, IBackpackDefinition definition, CreativeModeTab tab) {
		super(definition, EnumBackpackType.NATURALIST);
		this.typeId = typeId;
	}

	@Override
	protected void writeContainerData(ServerPlayer player, ItemStack stack, FriendlyByteBuf buffer) {
		buffer.writeByte(0);
		buffer.writeResourceLocation(this.typeId);
	}

	@Override
	public AbstractContainerMenu getContainer(int windowId, Player player, ItemStack heldItem) {
		return ContainerNaturalistBackpack.makeContainer(windowId, player, heldItem, 0, this.typeId);
	}
}
