package forestry.core.tiles;

import net.minecraft.core.BlockPos;

/**
 * Networked tile entities that have a client side "active" flag
 */
public interface IActivatable {
	/**
	 * Position of the tile entity.
	 *
	 * @return The position of the tile entity
	 */
	BlockPos getCoordinates();

	/**
	 * Retrieves the current state of the tile entity.
	 *
	 * @return True if the tile is currently active, false otherwise
	 */
	boolean isActive();

	/**
	 * Changes the state of this tile entity.
	 *
	 * @param active True if the tile should be activated, false otherwise
	 */
	void setActive(boolean active);
}
