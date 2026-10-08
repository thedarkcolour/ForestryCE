package forestry.api.apiculture;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface IJubilanceFactory {
	/**
	 * The default Jubilance Provider is satisfied when the humidity and temperature are ideal for the bee.
	 */
	IBeeJubilance getDefault();

	/**
	 * The jubilance provider for bees who prefer not to live near other creatures (other bees are fine).
	 */
	IBeeJubilance getHermit();

	/**
	 * The Requires Resource Jubilance Provider is satisfied when a specific block is under the hive.
	 */
	IBeeJubilance getRequiresResource(BlockState... acceptedBlockStates);

	/**
	 * The Requires Resource Jubilance Provider is satisfied when a block in the tag is under the hive.
	 */
	IBeeJubilance getRequiresResource(TagKey<Block> acceptedBlocks);
}
