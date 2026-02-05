package forestry.core.items;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

public class ItemForestryFood extends Item {
	private boolean isDrink = false;
	private int useTime = 32; //Set to 16 for 'fast'

	//Constructor for when properties are constructed manually
	public ItemForestryFood(Item.Properties properties) {
		super(properties);
	}

	//Simplified constructor for only declaring heal amount and saturation
	public ItemForestryFood(int heal, float saturation) {
		this(heal, saturation, new Item.Properties());
	}

	//Simplified constructor for declaring heal amount, saturation, and overriding use time
	public ItemForestryFood(int heal, float saturation, int useTime) {
		this(heal, saturation, new Item.Properties());
		this.useTime = useTime;
	}

	//Actual constructor that puts everything together.
	public ItemForestryFood(int heal, float saturation, Item.Properties properties) {
		super(properties.food(new FoodProperties.Builder().nutrition(heal).saturationMod(saturation).build()));
	}

	@Override
	public int getUseDuration(ItemStack pStack) {
		if (pStack.getItem().isEdible()) { //Unnecessary check? All food is edible?
			return useTime;
		} else {
			return 0;
		}
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
