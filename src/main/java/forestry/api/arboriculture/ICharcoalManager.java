package forestry.api.arboriculture;

import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Provides functions that are related to the forestry charcoal pile.
 */
@Deprecated
public interface ICharcoalManager {
	@Nullable
	ICharcoalPileWall getWall(BlockState state);

	/**
	 * @return A collection with all registered charcoal pile walls.
	 */
	List<ICharcoalPileWall> getWalls();
}
