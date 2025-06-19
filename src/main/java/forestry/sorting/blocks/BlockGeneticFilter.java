package forestry.sorting.blocks;

import forestry.core.blocks.BlockForestry;
import forestry.core.tiles.TileUtil;
import forestry.sorting.tiles.TileGeneticFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

public class BlockGeneticFilter extends BlockForestry implements EntityBlock {
	public static final BooleanProperty NORTH = BooleanProperty.create("north");
	public static final BooleanProperty EAST = BooleanProperty.create("east");
	public static final BooleanProperty SOUTH = BooleanProperty.create("south");
	public static final BooleanProperty WEST = BooleanProperty.create("west");
	public static final BooleanProperty UP = BooleanProperty.create("up");
	public static final BooleanProperty DOWN = BooleanProperty.create("down");

	private static final AABB BOX_CENTER = new AABB(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875);
	private static final AABB BOX_DOWN = new AABB(0.25, 0, 0.25, 0.75, 0.3125, 0.75);
	private static final AABB BOX_UP = new AABB(0.25, 0.6875, 0.25, 0.75, 1, 0.75);
	private static final AABB BOX_NORTH = new AABB(0.25, 0.25, 0, 0.75, 0.75, 0.3125);
	private static final AABB BOX_SOUTH = new AABB(0.25, 0.25, 0.6875, 0.75, 0.75, 1);
	private static final AABB BOX_WEST = new AABB(0, 0.25, 0.25, 0.3125, 0.75, 0.75);
	private static final AABB BOX_EAST = new AABB(0.6875, 0.25, 0.25, 1, 0.75, 0.75);
	private static final AABB[] BOX_FACES = {BOX_DOWN, BOX_UP, BOX_NORTH, BOX_SOUTH, BOX_WEST, BOX_EAST};

	public BlockGeneticFilter() {
		super(Block.Properties.of()
			.strength(0.25f, 3.0f)
			.dynamicShape()
			.noOcclusion()
		);
		this.registerDefaultState(this.getStateDefinition().any()
			.setValue(NORTH, false)
			.setValue(EAST, false)
			.setValue(SOUTH, false)
			.setValue(WEST, false)
			.setValue(UP, false)
			.setValue(DOWN, false));
	}

	public BlockState updateShape(BlockState state, Direction direction, BlockState changedState, LevelAccessor world, BlockPos pos, BlockPos changedPos) {
		TileGeneticFilter geneticFilter = TileUtil.getTile(world, pos, TileGeneticFilter.class);
		if (geneticFilter == null) {
			return defaultBlockState();
		}
		return state.setValue(NORTH, geneticFilter.isConnected(Direction.NORTH))
			.setValue(EAST, geneticFilter.isConnected(Direction.EAST))
			.setValue(SOUTH, geneticFilter.isConnected(Direction.SOUTH))
			.setValue(WEST, geneticFilter.isConnected(Direction.WEST))
			.setValue(UP, geneticFilter.isConnected(Direction.UP))
			.setValue(DOWN, geneticFilter.isConnected(Direction.DOWN));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.getBlockEntity(pos) instanceof TileGeneticFilter filter) {
			if (!level.isClientSide) {
				player.openMenu(filter, pos);

				return InteractionResult.CONSUME;
			} else {
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileGeneticFilter(pos, state);
	}
}
