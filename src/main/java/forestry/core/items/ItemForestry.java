package forestry.core.items;

import forestry.core.utils.ItemTooltipUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ItemForestry extends Item {
	private final int burnTime;

	public ItemForestry() {
		this(new Properties());
	}

	public ItemForestry(Item.Properties properties) {
		super(properties);

		if (properties instanceof ItemProperties props) {
			this.burnTime = props.burnTime;
		} else {
			this.burnTime = 0;
		}
	}

	@Override
	public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
		return this.burnTime;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag advanced) {
		ItemTooltipUtil.addInformation(stack, world, tooltip, advanced);
	}
}
