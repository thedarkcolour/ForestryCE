package forestry.api.core;

import net.minecraft.world.entity.player.Player;

/**
 * Implement this interface on your block entities so that players wearing Spectacles can see them more easily.
 */
public interface ISpectacleBlock {
	/**
	 * Determines whether a highlight should be drawn around this block.
	 *
	 * @param player The player wearing spectacles.
	 * @return {@code true} if a highlighted bounding box should be drawn around this block, {@code false} otherwise.
	 */
	default boolean isHighlighted(Player player) {
		return true;
	}

	/**
	 * Selects which highlight style the renderer draws for this block. The default is a steady rainbow outline
	 * (pollinated leaves, assembled multiblock anchors); returning {@code true} draws a pulsing white outline
	 * instead, used to flag work-in-progress markers such as the parts of an unformed multiblock.
	 *
	 * @param player The player wearing spectacles.
	 * @return {@code true} for the flashing-white outline, {@code false} for the steady rainbow outline.
	 */
	default boolean usesFlashingHighlight(Player player) {
		return false;
	}
}
