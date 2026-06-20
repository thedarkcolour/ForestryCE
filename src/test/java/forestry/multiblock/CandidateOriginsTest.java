package forestry.multiblock;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import forestry.core.multiblock.pattern.MultiblockPattern;
import forestry.core.multiblock.pattern.Predicates;
import forestry.core.multiblock.pattern.StructurePos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies {@link MultiblockPattern#candidateOrigins} = {@code { P − cellOffset }} over all size
 * variants, deduplicated (spec §5.3).
 */
class CandidateOriginsTest {

	/** Fixed 3x3x3 box pattern, no extra cells, so candidate offsets are exactly [0,3)^3. */
	private static MultiblockPattern fixed3x3x3() {
		return MultiblockPattern.builder()
				.componentTypePrefix("alveary_")
				.sizeX(3, 3).sizeY(3, 3).sizeZ(3, 3)
				.minBlocks(27)
				.boxCellPredicate((sx, sy, sz, dx, dy, dz) -> Predicates.anyComponent("alveary_", Predicates.KEY_INVALID_PART))
				.build();
	}

	/** Variable farm box X,Z in [3,5], Y=4, no extra cells. */
	private static MultiblockPattern variableFarm() {
		return MultiblockPattern.builder()
				.componentTypePrefix("farm_")
				.sizeX(3, 5).sizeY(4, 4).sizeZ(3, 5)
				.minBlocks(36)
				.boxCellPredicate((sx, sy, sz, dx, dy, dz) -> Predicates.anyComponent("farm_", Predicates.KEY_INVALID_PART))
				.build();
	}

	@Test
	void fixedPatternEnumeratesExactlyTheBoxOffsets() {
		MultiblockPattern pattern = fixed3x3x3();
		StructurePos p = new StructurePos(100, 64, 100);
		Set<StructurePos> origins = pattern.candidateOrigins(p);

		// expected = { p - (dx,dy,dz) : dx,dy,dz in [0,3) }
		Set<StructurePos> expected = new HashSet<>();
		for (int dx = 0; dx < 3; dx++) {
			for (int dy = 0; dy < 3; dy++) {
				for (int dz = 0; dz < 3; dz++) {
					expected.add(p.offset(-dx, -dy, -dz));
				}
			}
		}
		assertEquals(expected, new HashSet<>(origins));
		assertEquals(27, origins.size());
	}

	@Test
	void candidatesAlwaysIncludePItself() {
		// offset (0,0,0) is always a box cell, so P - 0 == P must be a candidate (P could be the origin).
		MultiblockPattern pattern = fixed3x3x3();
		StructurePos p = new StructurePos(-7, 3, 42);
		assertTrue(pattern.candidateOrigins(p).contains(p));
	}

	@Test
	void variableFarmDedupesAcrossSizeVariants() {
		MultiblockPattern pattern = variableFarm();
		StructurePos p = new StructurePos(0, 0, 0);
		Set<StructurePos> origins = pattern.candidateOrigins(p);

		// the union of all box offsets across X,Z in [3,5], Y=4 is [0,5) x [0,4) x [0,5):
		// each offset (dx,dy,dz) yields candidate p - (dx,dy,dz); the set is exactly that union, deduped.
		Set<StructurePos> expected = new HashSet<>();
		for (int dx = 0; dx < 5; dx++) {
			for (int dy = 0; dy < 4; dy++) {
				for (int dz = 0; dz < 5; dz++) {
					expected.add(p.offset(-dx, -dy, -dz));
				}
			}
		}
		assertEquals(expected, new HashSet<>(origins));
		assertEquals(5 * 4 * 5, origins.size());
	}
}
