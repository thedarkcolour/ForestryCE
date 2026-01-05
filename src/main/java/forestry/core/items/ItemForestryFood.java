package forestry.core.items;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

public class ItemForestryFood extends Item {
	private boolean isDrink = false;

	public ItemForestryFood(Item.Properties properties) {
		super(properties);
	}

	public ItemForestryFood(int heal, float saturation) {
		this(heal, saturation, new Item.Properties());
	}

	public ItemForestryFood(int heal, float saturation, Item.Properties properties) {
		super(properties.food(new FoodProperties.Builder().nutrition(heal).saturationMod(saturation).build()));
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemstack) {
		if (this.isDrink) {
			return UseAnim.DRINK;
		} else {
			return UseAnim.EAT;
		}
	}

	public ItemForestryFood setIsDrink() {
        this.isDrink = true;
		return this;
	}
}
