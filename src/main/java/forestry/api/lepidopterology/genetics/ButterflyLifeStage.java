package forestry.api.lepidopterology.genetics;

import forestry.api.genetics.ILifeStage;
import forestry.lepidopterology.features.LepidopterologyItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.Locale;

public enum ButterflyLifeStage implements ILifeStage {
	BUTTERFLY(LepidopterologyItems.BUTTERFLY_GE),
	SERUM(LepidopterologyItems.SERUM),
	CATERPILLAR(LepidopterologyItems.CATERPILLAR),
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
