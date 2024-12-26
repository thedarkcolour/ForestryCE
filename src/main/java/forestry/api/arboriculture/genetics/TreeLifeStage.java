/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.arboriculture.genetics;

import java.util.Locale;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import forestry.api.genetics.ILifeStage;
import forestry.arboriculture.features.ArboricultureItems;

public enum TreeLifeStage implements ILifeStage {
	SAPLING(ArboricultureItems.SAPLING),
	POLLEN(ArboricultureItems.POLLEN_FERTILE);

	private final String name;
	private final ItemLike itemForm;

	TreeLifeStage(ItemLike itemForm) {
		this.name = name().toLowerCase(Locale.ENGLISH);
		this.itemForm = itemForm;
	}

	public String getSerializedName() {
		return this.name;
	}

	@Override
	public Item getItemForm() {
		return this.itemForm.asItem();
	}
}
