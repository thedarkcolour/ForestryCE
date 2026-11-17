package forestry.core.platform.item;

import forestry.core.platform.util.ItemTooltipUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ForestryBlockItem<B extends Block> extends BlockItem {
	public ForestryBlockItem(B block, Item.Properties builder) {
		super(block, builder);
	}

	public ForestryBlockItem(B block) {
		this(block, new Item.Properties());
	}

	@Override
	public B getBlock() {
		//noinspection unchecked
		return (B) super.getBlock();
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag advanced) {
		super.appendHoverText(stack, context, tooltip, advanced);
		ItemTooltipUtil.addInformation(stack, tooltip);
	}
}
