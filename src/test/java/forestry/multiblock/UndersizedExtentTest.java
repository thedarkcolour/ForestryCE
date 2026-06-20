package forestry.multiblock;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import forestry.apiculture.multiblock.AlvearyPattern;
import forestry.core.multiblock.pattern.MultiblockPattern;
import forestry.core.multiblock.pattern.PatternResult;
import forestry.core.multiblock.pattern.Predicates;
import forestry.core.multiblock.pattern.StructurePos;
import forestry.farming.multiblock.FarmPattern;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Task A coverage for the {@code net.minecraft}-free pattern logic: the contiguous-extent measurement that
 * distinguishes an UNDERSIZED structure (whole layer missing -> {@code error.small}) from a full-extent
 * structure with a single interior/edge HOLE (-> {@code invalid.interior}), the message format args carried
 * on the failing cell, and the internal {@code KEY_NOT_MAXIMAL} "wrong candidate origin" signal.
 */
class UndersizedExtentTest {
	private static final StructurePos ORIGIN = new StructurePos(0, 0, 0);

	/** A complete, valid 3x3x3 alveary at origin: plain everywhere, slab cap above, clear air ring. */
	private static FakeStructureView.Builder validAlveary() {
		FakeStructureView.Builder b = FakeStructureView.builder();
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				for (int z = 0; z < 3; z++) {
					b.component(x, y, z, AlvearyPattern.PLAIN);
				}
			}
		}
		for (int x = 0; x < 3; x++) {
			for (int z = 0; z < 3; z++) {
				b.slab(x, 3, z);
			}
		}
		return b;
	}

	private static PatternResult validate(FakeStructureView.Builder b) {
		return AlvearyPattern.ALVEARY_PATTERN.validate(b.build(), ORIGIN);
	}

	// --- Undersized: an entire layer is missing -> error.small (the "must be 3x3x3" message) ---

	@Test
	void shortAlvearyMissingTopLayerFailsWithSmall() {
		// A 3x3x2 blob (no y=2 layer): every Y column is only 2 tall, and 3*3*2 = 18 < 27 blocks.
		FakeStructureView.Builder b = FakeStructureView.builder();
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 3; z++) {
					b.component(x, y, z, AlvearyPattern.PLAIN);
				}
			}
		}
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, validate(b));
		assertEquals(Predicates.KEY_SMALL, failure.firstKey());
		// error.small takes the three minimum dimensions (3,3,3) so the message reads "at least 3x3x3".
		assertArrayEquals(new int[]{3, 3, 3}, failure.first().args());
	}

	@Test
	void narrowAlvearyMissingXLayerFailsWithSmall() {
		// A 2x3x3 blob: full block count would be 18 < 27 -> caught by the aggregate small check first.
		FakeStructureView.Builder b = FakeStructureView.builder();
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 3; y++) {
				for (int z = 0; z < 3; z++) {
					b.component(x, y, z, AlvearyPattern.PLAIN);
				}
			}
		}
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, validate(b));
		assertEquals(Predicates.KEY_SMALL, failure.firstKey());
	}

	// --- Hole: full outer extent, one cell missing -> still invalid.interior (NOT error.small) ---

	@Test
	void singleEdgeHoleStillReportsInvalidInterior() {
		// Full extent in every axis, but the exterior-level-1 edge cell (0,1,0) is air. The parallel Y
		// columns are full (length 3), so the extent stays 3x3x3 and the size check passes; the per-cell
		// loop then reports the missing component as invalid.interior (parity), not error.small.
		FakeStructureView.Builder b = validAlveary();
		b.set(0, 1, 0, FakeStructureView.air());
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, validate(b));
		assertEquals(Predicates.KEY_INVALID_INTERIOR, failure.firstKey());
		assertNotEquals(Predicates.KEY_SMALL, failure.firstKey());
	}

	@Test
	void interiorHoleStillReportsInvalidInterior() {
		// The single interior cell (1,1,1) is air: outer shell is full, extent is 3x3x3 -> invalid.interior.
		FakeStructureView.Builder b = validAlveary();
		b.set(1, 1, 1, FakeStructureView.air());
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, validate(b));
		assertEquals(Predicates.KEY_INVALID_INTERIOR, failure.firstKey());
	}

	// --- The good cases the author verified still produce their content keys (no regression) ---

	@Test
	void completeButSlablessAlvearyStillNeedsSlabs() {
		FakeStructureView.Builder b = validAlveary();
		for (int x = 0; x < 3; x++) {
			for (int z = 0; z < 3; z++) {
				b.set(x, 3, z, FakeStructureView.air()); // remove the whole slab cap
			}
		}
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, validate(b));
		assertEquals(Predicates.KEY_NEED_SLABS, failure.firstKey());
	}

	// --- Farm small.x args ---

	@Test
	void farmTooNarrowCarriesMinXArg() {
		// 2x4x5 blob = 40 blocks (>= 36) but narrow in X -> error.small.x with the min X dimension (3).
		FakeStructureView.Builder b = FakeStructureView.builder();
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 0; z < 5; z++) {
					b.component(x, y, z, FarmPattern.PLAIN);
				}
			}
		}
		b.component(0, 0, 0, FarmPattern.GEARBOX);
		PatternResult result = FarmPattern.FARM_PATTERN.validate(b.build(), ORIGIN);
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_SMALL_X, failure.firstKey());
		assertArrayEquals(new int[]{3}, failure.first().args());
	}

	// --- Internal NOT_MAXIMAL signal: a non-min-corner candidate defers, never leaks invalid.part ---

	@Test
	void nonMinCornerCandidateReturnsNotMaximal() {
		// A complete 3x3x3 alveary, but validate from origin (1,0,0): a same-type component sits below in X,
		// so this candidate is not the true min corner -> the lower-face check returns the internal signal.
		MultiblockPattern pattern = AlvearyPattern.ALVEARY_PATTERN;
		PatternResult result = pattern.validate(validAlveary().build(), new StructurePos(1, 0, 0));
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_NOT_MAXIMAL, failure.firstKey());
		// And it is distinct from the player-facing content keys it used to be conflated with.
		assertNotEquals(Predicates.KEY_INVALID_PART, failure.firstKey());
		assertNotEquals(Predicates.KEY_INVALID_INTERIOR, failure.firstKey());
	}

	@Test
	void noArgsSentinelIsEmpty() {
		assertEquals(0, PatternResult.NO_ARGS.length);
		assertArrayEquals(new int[0], Arrays.copyOf(PatternResult.NO_ARGS, 0));
	}
}
