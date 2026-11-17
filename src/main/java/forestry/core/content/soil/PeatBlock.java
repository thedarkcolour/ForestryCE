package forestry.core.content.soil;

import forestry.core.features.CoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

/**
 * Mature form of bog earth. Has no block item of its own, so picking it gives back bog earth.
 */
public class PeatBlock extends Block {
	public PeatBlock(Block.Properties properties) {
		super(properties
			.strength(0.5f)
			.sound(SoundType.MUDDY_MANGROVE_ROOTS));
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
		return new ItemStack(CoreBlocks.BOG_EARTH);
	}
}
