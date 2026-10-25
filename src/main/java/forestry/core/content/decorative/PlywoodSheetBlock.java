package forestry.core.content.decorative;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PlywoodSheetBlock extends BurnableBlock {
	// the face of the block the sheet lies against, not the face the sheet points at
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	// indexed by Direction ordinal
	private static final VoxelShape[] SHAPES = {
		Shapes.box(0, 0.9375f, 0, 1, 1, 1),
		Shapes.box(0, 0, 0, 1, 0.0625f, 1),
		Shapes.box(0, 0, 0.9375f, 1, 1, 1),
		Shapes.box(0, 0, 0, 1, 1, 0.0625f),
		Shapes.box(0.9375f, 0, 0, 1, 1, 1),
		Shapes.box(0, 0, 0, 0.0625f, 1, 1),
	};

	public PlywoodSheetBlock(Properties properties) {
		super(properties);
		registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES[state.getValue(FACING).ordinal()];
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();
		BlockPos againstPos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
		BlockState againstState = level.getBlockState(againstPos);

		// copy the orientation of other sheet blocks
		if (againstState.is(this)) {
			return defaultBlockState().setValue(FACING, againstState.getValue(FACING));
		}
		return defaultBlockState().setValue(FACING, context.getClickedFace());
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.DESTROY;
	}

	// A sheet only burns on the face it lies flat against, the one face it fills
	@Override
	public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return state.isFaceSturdy(level, pos, direction);
	}

	@Override
	public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
		return state.isFaceSturdy(level, pos, direction);
	}
}
