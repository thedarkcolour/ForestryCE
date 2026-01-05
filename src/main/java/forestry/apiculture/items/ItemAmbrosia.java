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
				.alwaysEat()
				.nutrition(Constants.FOOD_AMBROSIA_HEAL)
				.saturationMod(0.6f)
				.effect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0), 1.0F)
				.build()));
	}

	@Override
	public boolean isFoil(ItemStack itemstack) {
		return true;
	}

}
