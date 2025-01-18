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

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.network.chat.TextColor;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import net.minecraftforge.common.Tags;

import forestry.api.ForestryTags;
import forestry.api.mail.EnumPostage;
import forestry.core.items.ItemOverlay;

public enum EnumStampDefinition implements ItemOverlay.IOverlayInfo {
	P_1("1n", EnumPostage.P_1, ForestryTags.Items.GEMS_APATITE, TextColor.fromRgb(0x4a8ca7), TextColor.fromRgb(0xffffff)),
	P_2("2n", EnumPostage.P_2, Items.COPPER_INGOT, TextColor.fromRgb(0xe8c814), TextColor.fromRgb(0xffffff)),
	P_5("5n", EnumPostage.P_5, ForestryTags.Items.INGOTS_TIN, TextColor.fromRgb(0x9c0707), TextColor.fromRgb(0xffffff)),
	P_10("10n", EnumPostage.P_10, Tags.Items.INGOTS_GOLD, TextColor.fromRgb(0x7bd1b8), TextColor.fromRgb(0xffffff)),
	P_20("20n", EnumPostage.P_20, Tags.Items.GEMS_DIAMOND, TextColor.fromRgb(0xff9031), TextColor.fromRgb(0xfff7dd)),
	P_50("50n", EnumPostage.P_50, Tags.Items.GEMS_EMERALD, TextColor.fromRgb(0x6431d7), TextColor.fromRgb(0xfff7dd)),
	P_100("100n", EnumPostage.P_100, Items.NETHER_STAR, TextColor.fromRgb(0xd731ba), TextColor.fromRgb(0xfff7dd)),
	;

	public static final EnumStampDefinition[] VALUES = values();
	private static final Map<EnumPostage, EnumStampDefinition> POSTAGE_MAP = new EnumMap<>(EnumPostage.class);

	static {
		for (EnumStampDefinition stampDefinition : VALUES) {
			POSTAGE_MAP.put(stampDefinition.getPostage(), stampDefinition);
		}
	}

	private final String name;
	private final int primaryColor;
	private final int secondaryColor;
	private final Supplier<Ingredient> craftingIngredient;
	private final EnumPostage postage;

	EnumStampDefinition(String name, EnumPostage postage, TagKey<Item> crafting, TextColor primaryColor, TextColor secondaryColor) {
		this(name, postage, () -> Ingredient.of(crafting), primaryColor, secondaryColor);
	}

	EnumStampDefinition(String name, EnumPostage postage, Item crafting, TextColor primaryColor, TextColor secondaryColor) {
		this(name, postage, () -> Ingredient.of(crafting), primaryColor, secondaryColor);
	}

	EnumStampDefinition(String name, EnumPostage postage, Supplier<Ingredient> crafting, TextColor primaryColor, TextColor secondaryColor) {
		this.name = name;
		this.primaryColor = primaryColor.getValue();
		this.secondaryColor = secondaryColor.getValue();
		this.craftingIngredient = crafting;
		this.postage = postage;
	}

	public EnumPostage getPostage() {
		return this.postage;
	}

	public Ingredient getCraftingIngredient() {
		return this.craftingIngredient.get();
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	@Override
	public int getPrimaryColor() {
		return primaryColor;
	}

	@Override
	public int getSecondaryColor() {
		return secondaryColor;
	}

	public static EnumStampDefinition getFromPostage(EnumPostage postage) {
		return POSTAGE_MAP.get(postage);
	}
}
