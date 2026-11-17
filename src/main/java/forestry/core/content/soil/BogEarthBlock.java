package forestry.core.content.soil;

import forestry.core.features.CoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * bog earth, which becomes peat.
 * <a href="https://forestryce.miraheze.org/wiki/Bog_Earth">Bog Earth - Forestry: Community Edition</a>
 */
public class BogEarthBlock extends Block {
	// maturity at which bogEarth becomes peat
	private static final int MAX_MATURITY = 2;
	public static final IntegerProperty MATURITY = IntegerProperty.create("maturity", 0, MAX_MATURITY);

	public BogEarthBlock(Block.Properties properties) {
		super(properties
			.randomTicks()
			.strength(0.5f)
			.sound(SoundType.GRAVEL));

		registerDefaultState(this.getStateDefinition().any().setValue(MATURITY, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(MATURITY);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.isClientSide || level.random.nextInt(13) != 0) {
			return;
		}

		int maturity = state.getValue(MATURITY);
		if (isMoistened(level, pos)) {
			// less-than, not equals, so an out-of-range maturity can't overflow the property
			if (maturity < MAX_MATURITY) {
				level.setBlock(pos, state.setValue(MATURITY, maturity + 1), UPDATE_CLIENTS);
			} else {
				level.setBlock(pos, CoreBlocks.PEAT.defaultState(), UPDATE_CLIENTS);
			}
		}
	}

	private static boolean isMoistened(Level world, BlockPos pos) {
		for (BlockPos waterPos : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
			BlockState blockState = world.getBlockState(waterPos);
			Block block = blockState.getBlock();
			// also counts waterlogged blocks (ex. seagrass, waterlogged stairs), not just source water
			if (block == Blocks.WATER || (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED))) {
				return true;
			}
		}

		return false;
	}
}
