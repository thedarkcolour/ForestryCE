package forestry.api.arboriculture.genetics;

import forestry.api.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Use this interface if your fruit is a pod, like Cocoa, Dates, or Papaya.
 */
public interface IPodFruit extends IFruit {
	/**
	 * @return Whether this pod fruit can hang off of the given block, ex. Cocoa can only hang off of Jungle logs
	 */
	boolean canSurviveOn(BlockState state);

	/**
	 * Attempts to place the pod fruit into the world.
	 *
	 * @param level  The world to place the pod fruit in.
	 * @param pos    The position to place the pod fruit at.
	 * @param genome The genome of the tree whose fruit is being placed.
	 * @return {@code true} if the placement was successful, {@code false} if the position was not valid for the pod.
	 */
    boolean tryPlace(LevelAccessor level, BlockPos pos, IGenome genome);
}
