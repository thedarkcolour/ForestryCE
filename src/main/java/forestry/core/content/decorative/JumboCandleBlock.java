package forestry.core.content.decorative;

import forestry.api.ForestryTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

/**
 * A candle one full block wide. Jumbo candles stack, and a stack reads as one candle: only the top block
 * carries a flame, and the blocks under it switch to a wickless model.
 */
public class JumboCandleBlock extends Block {
	public static final EnumProperty<CandleShape> SHAPE = EnumProperty.create("shape", CandleShape.class);
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	private final SimpleParticleType flame;

	public JumboCandleBlock(BlockBehaviour.Properties properties, JumboCandleBlockType type) {
		super(properties);
		this.registerDefaultState(this.getStateDefinition().any()
			.setValue(LIT, false)
			.setValue(SHAPE, CandleShape.SINGLE));
		this.flame = type == JumboCandleBlockType.REFRACTORY ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT);
		builder.add(SHAPE);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
		BlockState newState;

		// Reads the tag rather than the block, so candles of different colours stack
		boolean above = level.getBlockState(pos.above()).is(ForestryTags.Blocks.JUMBO_CANDLE);
		boolean below = level.getBlockState(pos.below()).is(ForestryTags.Blocks.JUMBO_CANDLE);

		// Update the shape of the candle first
		if (above && below) {
			newState = state.setValue(SHAPE, CandleShape.MIDDLE);
			newState = newState.setValue(LIT, false);
		} else if (!above && below) {
			newState = state.setValue(SHAPE, CandleShape.TOP);
		} else if (above && !below) {
			newState = state.setValue(SHAPE, CandleShape.BOTTOM);
			newState = newState.setValue(LIT, false);
		} else {
			newState = state.setValue(SHAPE, CandleShape.SINGLE);
		}

		// Whatever sits above lights or extinguishes the candle
		BlockState aboveState = level.getBlockState(pos.above());
		FluidState fluidState = aboveState.getFluidState();

		boolean hasFluid = !fluidState.isEmpty();
		boolean isLava = fluidState.is(FluidTags.LAVA);
		boolean isSolid = aboveState.isSolidRender(level, pos.above());

		if (state.getValue(LIT)) {
			if ((hasFluid && !isLava) || isSolid) {
				newState = newState.setValue(LIT, false);
				if (level instanceof ServerLevel serverLevel) {
					serverLevel.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
				}
			}
		} else {
			if ((hasFluid && isLava) || aboveState.is(BlockTags.FIRE)) {
				newState = newState.setValue(LIT, true);
			}
		}

		return newState;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) return;
		if (state.getValue(SHAPE) == CandleShape.BOTTOM || state.getValue(SHAPE) == CandleShape.MIDDLE) return;

		addParticlesAndSound(level, pos.getCenter().add(0, 0.6875f, 0), random, this.flame);
	}

	// Lifted from the vanilla candle code
	static void addParticlesAndSound(Level level, Vec3 offset, RandomSource random, SimpleParticleType flame) {
		float f = random.nextFloat();
		if (f < 0.3F) {
			level.addParticle(ParticleTypes.SMOKE, offset.x, offset.y, offset.z, 0.0, 0.0, 0.0);
			if (f < 0.17F) {
				level.playLocalSound(offset.x + 0.5, offset.y + 0.5, offset.z + 0.5, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
			}
		}
		level.addParticle(flame, offset.x, offset.y, offset.z, 0.0, 0.0, 0.0);
	}

	/**
	 * Deviation from 1.20.1: Block#use became useItemOn, which returns an ItemInteractionResult and runs
	 * for an empty hand too. Both branches stay here rather than moving the empty-handed one to
	 * useWithoutItem, which would also fire while the player holds an unrelated item.
	 */
	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!state.getValue(LIT)
			&& state.getValue(SHAPE) != CandleShape.BOTTOM
			&& state.getValue(SHAPE) != CandleShape.MIDDLE
			&& !player.isShiftKeyDown()
			&& !level.getBlockState(pos.above()).isSolidRender(level, pos.above())) {

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
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.DESTROY;
	}

	/**
	 * Where one jumbo candle sits in the stack it belongs to. Picks the model and decides whether the
	 * candle can carry a flame.
	 */
	public enum CandleShape implements StringRepresentable {
		SINGLE,
		TOP,
		MIDDLE,
		BOTTOM;

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}
}
