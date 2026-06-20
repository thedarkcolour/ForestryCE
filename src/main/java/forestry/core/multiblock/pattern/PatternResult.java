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

	/** A {@link FailingCell} carrying no message format args. */
	int[] NO_ARGS = new int[0];

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

		/** The first failing cell (the one that drives the player-facing chat message). */
		public FailingCell first() {
			return this.cells.get(0);
		}
	}

	/** A single member position paired with its component type id. */
	record Component(StructurePos pos, String typeId) {
	}

	/**
	 * A failing cell: its position, the parity translation key explaining why it failed, and any integer
	 * format args the key's lang string consumes ({@code error.small} → {@code minX,minY,minZ};
	 * {@code error.small.x|y|z} / {@code error.large.x|y|z} → a single dimension). Content keys whose lang
	 * string takes a block/type <em>name</em> ({@code invalid.interior} / {@code invalid.part}) carry no
	 * integer args here — the {@code net.minecraft}-aware world layer resolves the name from {@link #pos()}.
	 */
	record FailingCell(StructurePos pos, String key, int[] args) {
		public FailingCell(StructurePos pos, String key) {
			this(pos, key, NO_ARGS);
		}
	}
}
