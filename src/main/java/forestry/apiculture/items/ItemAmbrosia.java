package forestry.apiculture.items;

import forestry.core.config.Constants;
import forestry.core.items.ItemForestryFood;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemAmbrosia extends ItemForestryFood {
	public ItemAmbrosia() {
		super(new Item.Properties()
			.food(new FoodProperties.Builder()
				.alwaysEdible()
				.nutrition(Constants.FOOD_AMBROSIA_HEAL)
				.saturationModifier(0.6f)
				.effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 40, 0), 1f)
				.build()));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
}
