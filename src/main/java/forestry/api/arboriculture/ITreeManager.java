package forestry.api.arboriculture;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.Block;

/**
 * @since 2.6.0
 */
public interface ITreeManager {
	@Nullable
	Block getRefractoryWaxed(Block block);

	/**
	 * This getter will be replaced by separate methods in 1.21, as ICharcoalManager will be removed
	 */
	ICharcoalManager getCharcoalManager();

	IWoodAccess getWoodAccess();
}
