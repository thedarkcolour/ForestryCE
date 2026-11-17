package forestry.core.content.burnbarrel;

import forestry.core.features.CoreTiles;
import forestry.core.platform.block.ForestryBlock;
import forestry.core.platform.tile.IForestryTicker;
import forestry.core.platform.tile.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

// This class is its own unique thing, separate from TileForestry or anything like that, because the barrel wants a
// custom shape and its own properties. Deviation from 1.20.1: that tree extended BaseEntityBlock, which in 1.21.1
// forces a MapCodec and renders INVISIBLE by default. BlockForestry + EntityBlock is what every other tile-bearing
// block in this tree does, and it keeps RenderShape.MODEL without an override
public class BurnBarrelBlock extends ForestryBlock implements EntityBlock {
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final BooleanProperty HAS_ASH = BooleanProperty.create("has_ash");

	private static final VoxelShape BLOCK_SHAPE = Shapes.or(
		Block.box(3, 0, 3, 4, 16, 13),
		Block.box(3, 0, 3, 13, 16, 4),
		Block.box(12, 0, 3, 13, 16, 13),
		Block.box(3, 0, 12, 13, 16, 13),

		Block.box(3, 0, 3, 13, 2, 13)
	);
	private static final VoxelShape INTERACTION_SHAPE = Block.box(3, 0, 3, 13, 16, 13);

	public BurnBarrelBlock(Block.Properties properties) {
		super(properties);

		registerDefaultState(getStateDefinition().any()
			.setValue(LIT, false)
			.setValue(HAS_ASH, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT);
		builder.add(HAS_ASH);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BurnBarrelBlockEntity(pos, state);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return BLOCK_SHAPE;
	}

	@Override
	protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return INTERACTION_SHAPE;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide) {
			BurnBarrelBlockEntity barrel = TileUtil.getTile(level, pos, BurnBarrelBlockEntity.class);
			if (barrel != null) {
				barrel.openGui((ServerPlayer) player, InteractionHand.MAIN_HAND, pos);
			}
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	// Deviation from 1.20.1: Block#use split into useItemOn and useWithoutItem. The barrel lights from a held flint
	// and steel or fire charge, so this path forwards the hand the player actually used and consumes the click,
	// which also stops flint and steel from placing a fire against the barrel
	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!level.isClientSide) {
			BurnBarrelBlockEntity barrel = TileUtil.getTile(level, pos, BurnBarrelBlockEntity.class);
			if (barrel != null) {
				barrel.openGui((ServerPlayer) player, hand, pos);
			}
		}
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> actualType) {
		if (level.isClientSide || actualType != CoreTiles.BURN_BARREL.tileType()) {
			return null;
		}
		// IForestryTicker and not a bare lambda. It advances the TickHelper that updateOnInterval reads
		IForestryTicker<BurnBarrelBlockEntity> ticker = BurnBarrelBlockEntity::serverTick;
		//noinspection unchecked
		return (BlockEntityTicker<T>) ticker;
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock())) {
			BurnBarrelBlockEntity barrel = TileUtil.getTile(level, pos, BurnBarrelBlockEntity.class);
			if (barrel != null) {
				Containers.dropContents(level, pos, barrel.getInternalInventory());
			}
			level.removeBlockEntity(pos);
		}

		super.onRemove(state, level, pos, newState, movedByPiston);
	}

	// Deviation from 1.20.1: this overrode a vanilla BlockBehaviour method. 1.21.1 moved it to NeoForge's
	// IBlockExtension, where returning null means "use the value from the block properties"
	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.DESTROY;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) {
			return;
		}
		addParticlesAndSound(level, pos.getCenter().add(0, 0.55f, 0), random);
	}

	// Adapted from Jumbo Candles which was adapted from vanilla candles
	private static void addParticlesAndSound(Level level, Vec3 offset, RandomSource random) {
		float f = random.nextFloat();
		if (f < 0.6F) {
			float randX = (random.nextFloat() - 0.5f) / 10f;
			float randZ = (random.nextFloat() - 0.5f) / 10f;
			level.addParticle(ParticleTypes.SMOKE, offset.x, offset.y, offset.z, randX, 0.05, randZ);
			if (f < 0.085F) {
				level.playLocalSound(offset.x + 0.5, offset.y + 0.5, offset.z + 0.5, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
			}
		}
		if (f < 0.1F) {
			level.addParticle(ParticleTypes.LAVA, offset.x, offset.y, offset.z, 0.0, 0.0, 0.0);
		}
	}
}
