/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.lepidopterology.genetics;

import java.util.Locale;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import forestry.api.genetics.ILifeStage;
import forestry.lepidopterology.features.LepidopterologyItems;

public enum ButterflyLifeStage implements ILifeStage {
	BUTTERFLY(LepidopterologyItems.BUTTERFLY_GE),
	SERUM(LepidopterologyItems.SERUM_GE),
	CATERPILLAR(LepidopterologyItems.CATERPILLAR_GE),
	COCOON(LepidopterologyItems.COCOON_GE);

	private final String name;
	private final ItemLike itemForm;

	ButterflyLifeStage(ItemLike itemForm) {
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
