package forestry.core.content.sorting.blocks;

import forestry.core.platform.block.ForestryBlock;
import forestry.core.platform.tile.IForestryTicker;
import forestry.core.platform.tile.TileUtil;
import forestry.core.content.sorting.features.SortingBlockEntities;
import forestry.core.content.sorting.tiles.GeneticFilterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class GeneticFilterBlock extends ForestryBlock implements EntityBlock {
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

	public GeneticFilterBlock(Block.Properties properties) {
		super(properties
				.strength(0.25f, 3.0f)
				.dynamicShape()
				.noOcclusion(),
			false
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
		GeneticFilterBlockEntity geneticFilter = TileUtil.getTile(world, pos, GeneticFilterBlockEntity.class);
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
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player playerIn, BlockHitResult rayTraceResult) {
		GeneticFilterBlockEntity tile = TileUtil.getTile(worldIn, pos, GeneticFilterBlockEntity.class);
		if (tile != null) {
			if (TileUtil.isUsableByPlayer(playerIn, tile)) {
				if (!worldIn.isClientSide) {
					ServerPlayer sPlayer = (ServerPlayer) playerIn;
					sPlayer.openMenu(tile, pos);
				}
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult rayTraceResult) {
		return useWithoutItem(state, worldIn, pos, playerIn, rayTraceResult).consumesAction()
			? ItemInteractionResult.sidedSuccess(worldIn.isClientSide)
			: ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
	}

	@Override
	@Nullable
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new GeneticFilterBlockEntity(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> actualType) {
		if (level.isClientSide || actualType != SortingBlockEntities.GENETIC_FILTER.tileType()) {
			return null;
		}
		// IForestryTicker and not a bare lambda. It advances the TickHelper that updateOnInterval reads
		IForestryTicker<GeneticFilterBlockEntity> ticker = GeneticFilterBlockEntity::serverTick;
		//noinspection unchecked
		return (BlockEntityTicker<T>) ticker;
	}
}
