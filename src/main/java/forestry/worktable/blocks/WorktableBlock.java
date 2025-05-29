package forestry.worktable.blocks;

import forestry.core.blocks.BlockBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.SoundType;

import java.util.List;

public class WorktableBlock extends BlockBase<WorktableBlockType> {
	public WorktableBlock(WorktableBlockType blockType) {
		super(blockType, Properties.of().sound(SoundType.WOOD));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		if (!stack.getComponents().isEmpty()) {
			tooltip.add(Component.translatable("block.forestry.worktable_tooltip"));
		}
	}
}
