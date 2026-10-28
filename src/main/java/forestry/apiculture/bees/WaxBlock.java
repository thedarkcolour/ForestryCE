package forestry.apiculture.bees;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class WaxBlock extends Block {
	private final boolean meltable;

	// Adding this for compatibility reasons. Other bee mods might want meltable wax blocks yknow.
	@Nullable
	private final Supplier<Fluid> meltingFluid;

	public WaxBlock(Properties properties, boolean melts, @Nullable Supplier<Fluid> meltingFluid) {
		super(properties);
		this.meltable = melts;
		this.meltingFluid = meltingFluid;
	}

	@Override
	public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		if (this.meltable) {
			return super.isFlammable(state, level, pos, direction);
		}
		return false;
	}

	@Override
	public boolean canStickTo(BlockState state, BlockState other) {
		return false;
	}

	@Override
	public boolean isStickyBlock(BlockState state) {
		return false;
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return this.meltable ? 45 : 0;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return this.meltable ? 45 : 0;
	}

	@Override
	public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction direction, @Nullable LivingEntity igniter) {
		super.onCaughtFire(state, level, pos, direction, igniter);
		if (this.meltable && this.meltingFluid != null) {
			level.setBlock(pos, this.meltingFluid.get().defaultFluidState().createLegacyBlock(), 3);
		}
	}
}
