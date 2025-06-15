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

import forestry.api.ForestryCapabilities;
import forestry.api.ForestryConstants;
import forestry.arboriculture.capabilities.SpectacleVision;
import forestry.core.config.Constants;
import forestry.core.utils.ItemTooltipUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.List;

public class ItemSpectacles extends ArmorItem {
	public static final String TEXTURE_NATURALIST_ARMOR_PRIMARY = ForestryConstants.MOD_ID + ":" + Constants.TEXTURE_PATH_ITEM + "/naturalist_armor_1.png";

	public ItemSpectacles() {
		super(ArmorMaterials.LEATHER, Type.HELMET, (new Item.Properties()).durability(100));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return TEXTURE_NATURALIST_ARMOR_PRIMARY;
	}

	@Override
	public void appendHoverText(ItemStack stack, List<Component> tooltip) {
		ItemTooltipUtil.addInformation(stack, tooltip);
	}

	@Override
	public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new ICapabilityProvider() {
			@Override
			public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
				return cap == ForestryCapabilities.SPECTACLE_VISION ? LazyOptional.of(() -> SpectacleVision.INSTANCE).cast() : LazyOptional.empty();
			}
		};
	}
}
