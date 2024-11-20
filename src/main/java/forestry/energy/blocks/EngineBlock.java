package forestry.energy.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import forestry.core.blocks.BlockBase;
import forestry.core.tiles.TileUtil;
import forestry.energy.EnergyHelper;
import forestry.energy.ForestryEnergyStorage;
import forestry.energy.tiles.EngineBlockEntity;

import org.jetbrains.annotations.Nullable;

public class EngineBlock extends BlockBase<EngineBlockType> {
	private static final VoxelShape[] SHAPE_FOR_DIRECTIONS = new VoxelShape[6];

	public static final EnumProperty<Direction> VERTICAL_FACING = EnumProperty.create("facing", Direction.class, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.UP);

	static {
		SHAPE_FOR_DIRECTIONS[Direction.EAST.ordinal()] = Shapes.or(Block.box(0, 0, 0, 6, 16, 16), Block.box(6, 2, 2, 10, 14, 14), Block.box(10, 4, 4, 16, 12, 12));
		SHAPE_FOR_DIRECTIONS[Direction.WEST.ordinal()] = Shapes.or(Block.box(0, 4, 4, 6, 12, 12), Block.box(6, 2, 2, 10, 14, 14), Block.box(10, 0, 0, 16, 16, 16));
		SHAPE_FOR_DIRECTIONS[Direction.SOUTH.ordinal()] = Shapes.or(Block.box(0, 0, 0, 16, 16, 6), Block.box(2, 2, 6, 14, 14, 10), Block.box(4, 4, 10, 12, 12, 16));
		SHAPE_FOR_DIRECTIONS[Direction.NORTH.ordinal()] = Shapes.or(Block.box(4, 4, 0, 12, 12, 6), Block.box(2, 2, 6, 14, 14, 10), Block.box(0, 0, 10, 16, 16, 16));
		SHAPE_FOR_DIRECTIONS[Direction.UP.ordinal()] = Shapes.or(Block.box(0, 0, 0, 16, 6, 16), Block.box(2, 6, 2, 14, 10, 14), Block.box(4, 10, 4, 12, 16, 12));
		SHAPE_FOR_DIRECTIONS[Direction.DOWN.ordinal()] = Shapes.or(Block.box(0, 10, 0, 16, 16, 16), Block.box(2, 6, 2, 14, 10, 14), Block.box(4, 0, 4, 12, 6, 12));
	}

	public EngineBlock(EngineBlockType blockType) {
		super(blockType, Properties.of().sound(SoundType.METAL));

		registerDefaultState(getStateDefinition().any().setValue(VERTICAL_FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(VERTICAL_FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
		Direction orientation = state.getValue(VERTICAL_FACING);
		return SHAPE_FOR_DIRECTIONS[orientation.ordinal()];
	}

	@Override
	public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rot) {
		return rotate(state, world, pos);
	}

	private static boolean isOrientedAtEnergyReciever(LevelAccessor world, BlockPos pos, Direction orientation) {
		BlockPos offsetPos = pos.relative(orientation);
		BlockEntity tile = TileUtil.getTile(world, offsetPos);
		return EnergyHelper.isEnergyReceiverOrEngine(orientation.getOpposite(), tile);
	}

	private static BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos) {
		Direction blockFacing = state.getValue(VERTICAL_FACING);
		for (int i = blockFacing.ordinal() + 1; i <= blockFacing.ordinal() + 6; ++i) {
			Direction orientation = Direction.VALUES[i % 6];
			if (isOrientedAtEnergyReciever(world, pos, orientation)) {
				return state.setValue(VERTICAL_FACING, orientation);
			}
		}
		return state;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction orientation = context.getClickedFace().getOpposite();
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (isOrientedAtEnergyReciever(world, pos, orientation)) {
			return defaultBlockState().setValue(VERTICAL_FACING, orientation);
		}
		return rotate(defaultBlockState().setValue(VERTICAL_FACING, context.getHorizontalDirection()), world, pos);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
		EngineBlockEntity tileEngine = TileUtil.getTile(level, pos, EngineBlockEntity.class);
		if (tileEngine != null) {
			ForestryEnergyStorage energyStorage = tileEngine.getEnergyManager();
			return energyStorage.calculateRedstone();
		}
		return 0;
	}
}
