package forestry.lepidopterology.blocks;

import forestry.core.utils.ItemStackUtil;
import forestry.lepidopterology.tiles.TileCocoon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class BlockSolidCocoon extends Block implements EntityBlock {
	public BlockSolidCocoon() {
		super(Block.Properties.of()
			.strength(0.5F)
			.randomTicks()
			.sound(SoundType.GRAVEL));
		registerDefaultState(getStateDefinition().any().setValue(BlockCocoon.AGE, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockCocoon.AGE);
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
		if (canHarvestBlock(state, level, pos, player) && blockEntity instanceof TileCocoon cocoon) {
			List<ItemStack> drops = cocoon.getCocoonDrops();

			for (ItemStack stack : drops) {
				ItemStackUtil.dropItemStackAsEntity(stack, level, pos);
			}
		}

		super.playerDestroy(level, player, pos, state, blockEntity, itemStack);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileCocoon(pos, state, true);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (facing != Direction.UP || !facingState.isAir()) {
			return state;
		}
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return BlockCocoon.BOUNDING_BOX;
	}
}
