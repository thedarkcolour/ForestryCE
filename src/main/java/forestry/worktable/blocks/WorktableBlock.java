package forestry.worktable.blocks;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;

import forestry.core.blocks.BlockBase;

import org.jetbrains.annotations.Nullable;

public class WorktableBlock extends BlockBase<WorktableBlockType> {
	public WorktableBlock(WorktableBlockType blockType) {
		super(blockType, Properties.of().sound(SoundType.WOOD));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
		if (stack.getTag() != null) {
			tooltip.add(Component.translatable("block.forestry.worktable_tooltip"));
		}
	}
}
