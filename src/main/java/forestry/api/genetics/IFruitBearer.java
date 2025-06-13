package forestry.api.genetics;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Can be implemented by tile entities which can bear fruit.
 */
public interface IFruitBearer {
	/**
	 * @return true if the actual tile can bear fruits.
	 */
	boolean hasFruit();

	/**
	 * Picks the fruits of this tile, resetting it to unripe fruits.
	 *
	 * @return Picked fruits. The returned list is immutable.
	 */
	List<ItemStack> pickFruit();

	/**
	 * @return float indicating the ripeness of the fruit with >= 1.0f indicating full ripeness.
	 */
	float getRipeness();

	/**
	 * Increases the ripeness of the fruit. Adding 1.0f will fully ripen the fruit.
	 *
	 * @param add Float to add to the ripeness. Will truncate to valid values.
	 */
	void addRipeness(float add);
}
