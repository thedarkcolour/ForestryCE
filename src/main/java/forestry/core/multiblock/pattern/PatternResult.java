package forestry.core.multiblock.pattern;

import java.util.List;

/**
 * The outcome of {@link MultiblockPattern#validate} (spec §5.2): either a {@link Match} describing a
 * fully-formed, maximal, fully-loaded structure, or a {@link Failure} listing the failing cells (the
 * first of which drives the player-facing chat message).
 *
 * <p>{@code net.minecraft}-free, like the rest of the pattern layer.
 */
public sealed interface PatternResult permits PatternResult.Match, PatternResult.Failure {

	/**
	 * A successful match.
	 *
	 * @param members    every member position of the structure, lexicographically sorted (lowest first)
	 * @param min        the bounding-box minimum corner (recomputed from {@code members}, spec §6.1)
	 * @param max        the bounding-box maximum corner
	 * @param holder     the payload holder / reference coord = lowest-{@code (x,y,z)} member (spec §6.1)
	 * @param components the member positions mapped to their component type id (for bucketing, spec §8)
	 */
	record Match(
			List<StructurePos> members,
			StructurePos min,
			StructurePos max,
			StructurePos holder,
			List<Component> components
	) implements PatternResult {
	}

	/**
	 * A failed match. {@code cells} is never empty; {@code cells.get(0)} is the first failing cell, whose
	 * {@link FailingCell#key()} is the parity translation key shown to the player.
	 */
	record Failure(List<FailingCell> cells) implements PatternResult {
		public String firstKey() {
			return this.cells.get(0).key();
		}
	}

	/** A single member position paired with its component type id. */
	record Component(StructurePos pos, String typeId) {
	}

	/** A failing cell: its position and the parity translation key explaining why it failed. */
	record FailingCell(StructurePos pos, String key) {
	}
}
