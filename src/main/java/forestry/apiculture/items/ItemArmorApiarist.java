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
package forestry.apiculture.items;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import forestry.api.ForestryCapabilities;
import forestry.api.ForestryConstants;
import forestry.api.apiculture.IArmorApiarist;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.apiculture.features.ApicultureItems;
import forestry.core.config.Constants;
import forestry.core.features.CoreItems;
import forestry.core.items.definitions.EnumCraftingMaterial;

import org.jetbrains.annotations.Nullable;

public class ItemArmorApiarist extends ArmorItem {
	public static final String TEXTURE_APIARIST_ARMOR_PRIMARY = ForestryConstants.MOD_ID + ":" + Constants.TEXTURE_PATH_ITEM + "/apiarist_armor_1.png";
	public static final String TEXTURE_APIARIST_ARMOR_SECONDARY = ForestryConstants.MOD_ID + ":" + Constants.TEXTURE_PATH_ITEM + "/apiarist_armor_2.png";

	public static final class ApiaristArmorMaterial implements ArmorMaterial {
		private static final int[] reductions = new int[]{1, 3, 2, 1};
		private static final int[] DURABILITY = new int[]{11*3, 16*3, 15*3, 13*3};

		@Override
		public int getDurabilityForType(ArmorItem.Type type) {
			return DURABILITY[type.ordinal()];
		}

		@Override
		public int getDefenseForType(ArmorItem.Type type) {
			return reductions[type.ordinal()];
		}

		@Override
		public int getEnchantmentValue() {
			return 15;
		}

		@Override
		public SoundEvent getEquipSound() {
			return SoundEvents.ARMOR_EQUIP_LEATHER;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.of(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.WOVEN_SILK).get());
		}

		@Override
		public String getName() {
			return "APIARIST_ARMOR";
		}

		@Override
		public float getToughness() {
			return 0.0F;
		}

		@Override
		public float getKnockbackResistance() {
			return 0.0F;
		}
	}

	public enum ArmorApiarist implements IArmorApiarist {
		INSTANCE;

		@Override
		public boolean protectEntity(LivingEntity entity, ItemStack armor, @Nullable IBeeEffect cause, boolean doProtect) {
			return true;
		}
	}

	public ItemArmorApiarist(ArmorItem.Type type) {
		super(new ApiaristArmorMaterial(), type, new Item.Properties());
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		if (ApicultureItems.APIARIST_LEGS.itemEqual(stack)) {
			return TEXTURE_APIARIST_ARMOR_SECONDARY;
		} else {
			return TEXTURE_APIARIST_ARMOR_PRIMARY;
		}
	}

	@Override
	public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new ICapabilityProvider() {
			@Override
			public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
				return cap == ForestryCapabilities.ARMOR_APIARIST ? LazyOptional.of(() -> ArmorApiarist.INSTANCE).cast() : LazyOptional.empty();
			}
		};
	}
}
