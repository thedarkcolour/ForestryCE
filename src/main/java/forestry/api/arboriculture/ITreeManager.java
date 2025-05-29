package forestry.api.arboriculture;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * @since 2.6.0
 */
public interface ITreeManager {
	/**
	 * @param block The block to query the refractory waxed form of, ex. Oak Planks
	 * @return The resulting block after refractory wax is used on it, ex. Oak Planks (Fireproof),
	 * or {@code null} if refractory wax cannot be applied to the block.
	 */
	@Nullable
	Block getRefractoryWaxed(Block block);

	/**
	 * @return Read-only view of all registered charcoal pit wall types.
	 */
	List<ICharcoalPileWall> getWalls();

	/**
	 * @return Information about the block in a charcoal pit wall, {@code null} if not a valid charcoal pit wall.
	 */
	@Nullable
	ICharcoalPileWall getWall(BlockState state);

	IWoodAccess getWoodAccess();
}
