package forestry.api.apiculture.hives;

import forestry.api.apiculture.bee.IBee;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;

import java.util.List;

/**
 * Represents a drop from a wild bee hive.
 */
public interface IHiveDrop {
	IBee createIndividual(BlockGetter level, BlockPos pos);

	List<ItemStack> getExtraItems(BlockGetter level, BlockPos pos, int fortune);

	/**
	 * Chance to drop a bee or extra items. Default drops have 0.80 (= 80 %).
	 *
	 * @param level Minecraft world this is called for.
	 * @param pos   Coordinates of the broken hive.
	 * @return Chance for drop as a float of 0.0 - 1.0.
	 */
	double getChance(BlockGetter level, BlockPos pos, int fortune);

	/**
	 * Chance for the princess to be ignoble. Usually, it's around 0.4 to 0.7 (40% - 70%).
	 *
	 * @param level Minecraft world this is called for.
	 * @param pos   Coordinates of the broken hive.
	 * @return Chance for ignoble as a float of 0.0 - 1.0.
	 */
	double getIgnobleChance(BlockGetter level, BlockPos pos, int fortune);
}
