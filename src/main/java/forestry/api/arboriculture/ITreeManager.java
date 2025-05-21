package forestry.api.arboriculture;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.Block;

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
	 * This getter will be replaced by separate methods in 1.21, as ICharcoalManager will be removed
	 */
	ICharcoalManager getCharcoalManager();

	IWoodAccess getWoodAccess();
}
