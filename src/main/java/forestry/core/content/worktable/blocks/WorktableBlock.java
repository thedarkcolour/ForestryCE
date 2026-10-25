package forestry.core.content.worktable.blocks;

import forestry.core.platform.block.BaseBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

public class WorktableBlock extends BaseBlock<WorktableBlockType> {
	public WorktableBlock(BlockBehaviour.Properties properties, WorktableBlockType blockType) {
		super(properties.sound(SoundType.WOOD), blockType);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		if (!stack.getComponents().isEmpty()) {
			tooltip.add(Component.translatable("block.forestry.worktable_tooltip"));
		}
	}
}
