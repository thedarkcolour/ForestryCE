package forestry.api.apiculture.bee;

import forestry.api.genetics.ILifeStage;
import forestry.apiculture.features.ApicultureItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.Locale;

public enum BeeLifeStage implements ILifeStage {
	DRONE(ApicultureItems.BEE_DRONE),
	PRINCESS(ApicultureItems.BEE_PRINCESS),
	QUEEN(ApicultureItems.BEE_QUEEN),
	LARVAE(ApicultureItems.BEE_LARVAE);

	private final String name;
	private final ItemLike itemForm;

	BeeLifeStage(ItemLike supplier) {
		this.name = name().toLowerCase(Locale.ENGLISH);
		this.itemForm = supplier;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	@Override
	public Item getItemForm() {
		return this.itemForm.asItem();
	}
}
