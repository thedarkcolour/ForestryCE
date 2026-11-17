package forestry.core.content.decorative;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A candle half a block wide and three quarters of a block tall. Big candles do not stack.
 */
public class BigCandleBlock extends Block implements SimpleWaterloggedBlock {
	// todo Wouldn't it be cool if you could stack big candles on top of each other?
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private static final VoxelShape SHAPE = Shapes.box(0.25, 0, 0.25, 0.75, 0.75, 0.75);

	private final SimpleParticleType flame;

	public BigCandleBlock(BlockBehaviour.Properties properties, BigCandleBlockType type) {
		super(properties);
		this.registerDefaultState(this.getStateDefinition().any()
			.setValue(LIT, false)
			.setValue(WATERLOGGED, false));
		this.flame = type == BigCandleBlockType.REFRACTORY ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT);
		builder.add(WATERLOGGED);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) return;
		JumboCandleBlock.addParticlesAndSound(level, pos.getCenter().add(0, 0.4375f, 0), random, this.flame);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!state.getValue(LIT)
			&& !player.isShiftKeyDown()
			&& !state.getValue(WATERLOGGED)) {

			if (stack.is(Items.FLINT_AND_STEEL)) {
				if (!level.isClientSide) {
					level.setBlock(pos, state.setValue(LIT, true), 3);
					stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
				}
				level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS);
				return ItemInteractionResult.sidedSuccess(level.isClientSide);
			} else if (stack.is(Items.FIRE_CHARGE)) {
				if (!level.isClientSide) {
					level.setBlock(pos, state.setValue(LIT, true), 3);
					if (!player.isCreative()) {
						stack.shrink(1);
					}
				}
				level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS);
				return ItemInteractionResult.sidedSuccess(level.isClientSide);
			}
		} else if (state.getValue(LIT) && stack.isEmpty()) {
			level.setBlock(pos, state.setValue(LIT, false), 3);
			level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
		return this.defaultBlockState().setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		if (state.getValue(WATERLOGGED)) {
			state = state.setValue(LIT, false);
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public boolean canStickTo(BlockState state, BlockState other) {
		return false;
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.DESTROY;
	}
}
