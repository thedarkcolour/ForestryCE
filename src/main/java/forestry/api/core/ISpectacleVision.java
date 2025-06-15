package forestry.api.core;

import forestry.api.ForestryCapabilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Naturalist armor allows players to see pollinated tree leaves.
 *
 * @see ForestryCapabilities#SPECTACLE_VISION
 */
public interface ISpectacleVision {

	/**
	 * Called to see if this naturalist's armor allows for seeing pollinated tree leaves/flowers.
	 *
	 * @param player Player doing the viewing
	 * @param armor  Armor item
	 * @param doSee  Whether or not to actually do the side effects of viewing
	 * @return true if the armor actually allows the player to see pollination.
	 */
	boolean canSeePollination(Player player, ItemStack armor, boolean doSee);
}
