package forestry.api.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Interface for things, that have a location.
 */
public interface ILocationProvider {
	BlockPos getBlockPos();

	@Nullable
	Level getLevel();
}
