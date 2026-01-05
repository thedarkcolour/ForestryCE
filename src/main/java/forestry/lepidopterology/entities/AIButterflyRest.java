package forestry.lepidopterology.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;

import java.util.EnumSet;

public class AIButterflyRest extends AIButterflyBase {
	public AIButterflyRest(EntityButterfly entity) {
		super(entity);
		setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (this.entity.getExhaustion() < EntityButterfly.EXHAUSTION_REST
			&& this.entity.canFly()) {
			return false;
		}

		Vec3 entityPos = this.entity.position();
		int x = (int) entityPos.x;
		int y = (int) Math.floor(entityPos.y);
		int z = (int) entityPos.z;
		BlockPos pos = new BlockPos(x, y, z);

		if (!canLand(pos)) {
			return false;
		}

		Level level = this.entity.level();
		pos = pos.relative(Direction.DOWN);
		if (level.isEmptyBlock(pos)) {
			return false;
		}
		BlockState blockState = level.getBlockState(pos);
		if (blockState.liquid()) {
			return false;
		}
		if (!this.entity.getButterfly().isAcceptedEnvironment(level, x, pos.getY(), z)) {
			return false;
		}

        this.entity.setDestination(null);
        this.entity.setState(EnumButterflyState.RESTING);
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		if (this.entity.getExhaustion() <= 0 && this.entity.canFly()) {
			return false;
		}
		return !this.entity.isInWater();
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public void tick() {
        this.entity.changeExhaustion(-1);
	}

	private boolean canLand(BlockPos pos) {
		Level level = this.entity.level();
		if (!level.hasChunkAt(pos)) {
			return false;
		}
		BlockState blockState = level.getBlockState(pos);
		if (!blockState.isAir()) {
			return false;
		}
		if (isPlant(blockState)) {
			return true;
		}

		BlockState belowState = level.getBlockState(pos.below());
		return isRest(belowState) || belowState.is(BlockTags.LEAVES);
	}

	private static boolean isRest(BlockState state) {
		return state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS);
	}

	private static boolean isPlant(BlockState state) {
		Block block = state.getBlock();
		if (state.is(BlockTags.FLOWERS)) {
			return true;
		} else if (block instanceof IPlantable) {
			return true;
		} else if (block instanceof BonemealableBlock) {
			return true;
		} else {
			return state.is(BlockTags.LEAVES);
		}
	}
}
