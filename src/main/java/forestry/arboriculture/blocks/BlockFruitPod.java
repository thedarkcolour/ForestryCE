package forestry.arboriculture.blocks;

import forestry.arboriculture.tiles.TileFruitPod;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class BlockFruitPod extends CocoaBlock implements EntityBlock {
	protected static final VoxelShape[] EAST_SMALL_AABB = new VoxelShape[]{Block.box(11, 8, 6, 15, 12, 10), Block.box(9, 6, 5, 15, 12, 11), Block.box(7, 4, 4, 15, 12, 12)};
	protected static final VoxelShape[] WEST_SMALL_AABB = new VoxelShape[]{Block.box(1, 8, 6, 5, 12, 10), Block.box(1, 6, 5, 7, 12, 11), Block.box(1, 4, 4, 9, 12, 12)};
	protected static final VoxelShape[] NORTH_SMALL_AABB = new VoxelShape[]{Block.box(6, 8, 1, 10, 12, 5), Block.box(5, 6, 1, 11, 12, 7), Block.box(4, 4, 1, 12, 12, 9)};
	protected static final VoxelShape[] SOUTH_SMALL_AABB = new VoxelShape[]{Block.box(6, 8, 11, 10, 12, 15), Block.box(5, 6, 9, 11, 12, 15), Block.box(4, 4, 7, 12, 12, 15)};

	private final ForestryPodType podType;

	public BlockFruitPod(ForestryPodType podType) {
		super(BlockSapling.Properties.of().randomTicks().strength(0.2f, 3.0f).sound(SoundType.WOOD));
		this.podType = podType;
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		TileFruitPod tile = TileUtil.getTile(level, pos, TileFruitPod.class);
		if (tile == null) {
			return ItemStack.EMPTY;
		}
		return tile.getPickBlock();
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
		if (!canSurvive(state, level, pos)) {
			dropResources(state, level, pos);
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			return;
		}

		TileFruitPod tile = TileUtil.getTile(level, pos, TileFruitPod.class);
		if (tile == null) {
			return;
		}

		tile.onBlockTick(rand);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder context) {
		BlockPos pos = BlockUtil.getPos(context);

		if (context.getLevel().getBlockEntity(pos) instanceof TileFruitPod pod) {
			return pod.getDrops();
		} else if (context.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TileFruitPod pod) {
			return pod.getDrops();
		}

		return List.of();
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		return BlockUtil.isValidPodLocation(level, pos, facing, this.podType.getFruit().getLogTag());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileFruitPod(pos, state);
	}

	/* IGrowable */
	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
		TileFruitPod podTile = TileUtil.getTile(level, pos, TileFruitPod.class);
		return podTile != null && podTile.canMature();
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource rand, BlockPos pos, BlockState state) {
		TileFruitPod podTile = TileUtil.getTile(level, pos, TileFruitPod.class);
		if (podTile != null) {
			podTile.addRipeness(0.5f);
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (this.podType != ForestryPodType.COCONUT) {
			return super.getShape(state, level, pos, context);
		}

		// smaller hitbox for Coconut
		int i = state.getValue(AGE);
		return switch (state.getValue(FACING)) {
			case SOUTH -> SOUTH_SMALL_AABB[i];
			case WEST -> WEST_SMALL_AABB[i];
			case EAST -> EAST_SMALL_AABB[i];
			default -> NORTH_SMALL_AABB[i];
		};
	}
}
