package forestry.apiculture;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

import forestry.api.apiculture.IActivityType;
import forestry.api.apiculture.LightPreference;
import forestry.api.core.ForestryError;
import forestry.api.core.IError;

import org.joml.Vector2i;

public class CathemeralActivityType implements IActivityType {
	private static final PerlinNoise NOISE = PerlinNoise.create(RandomSource.create(13L), List.of(5, 3, 6));

	// Active during [x,y)
	public static Vector2i getSleepPeriod(BlockPos pos) {
		int offset = (int) getOffset(pos);
		if (offset > 0) {
			return new Vector2i(24000 - offset, -12000 + (24000 - offset));
		} else {
			return new Vector2i(-offset, 12000 - offset);
		}
	}

	// Varies according to position, but y coordinate has lower impact on variation
	private static long getOffset(BlockPos pos) {
		return (long) (NOISE.getValue(pos.getX() / 40.0, pos.getY() / 1000.0, pos.getZ() / 40.0) * 24000L);
	}

	@Override
	public boolean isActive(long gameTime, long dayTime, BlockPos pos) {
		long adjustedTime = dayTime + getOffset(pos);

		return adjustedTime % 24000L < 12000L;
	}

	@Override
	public IError getInactiveError(long gameTime, long dayTime, BlockPos pos) {
		return ForestryError.SLEEPY;
	}

	@Override
	public LightPreference getLightPreference() {
		return LightPreference.ANY;
	}

	@Override
	public boolean isDominant() {
		return false;
	}
}
