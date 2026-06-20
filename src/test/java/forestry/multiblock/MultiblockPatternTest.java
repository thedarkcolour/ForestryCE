package forestry.multiblock;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import forestry.core.multiblock.pattern.CellPredicate;
import forestry.core.multiblock.pattern.MultiblockPattern;
import forestry.core.multiblock.pattern.PatternResult;
import forestry.core.multiblock.pattern.Predicates;
import forestry.core.multiblock.pattern.StructurePos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises {@link MultiblockPattern#validate}: full assembly, per-cell failure keys, the variable-size
 * Farm, and the D1 maximality + loaded-shell rule (spec §5.1, §5.2, §6.1).
 *
 * <p>Uses small self-contained alveary-like / farm-like patterns so this task does not depend on the
 * concrete {@code AlvearyPattern}/{@code FarmPattern} (those get their own parity tests in Task 1.5).
 */
class MultiblockPatternTest {

	// --- A minimal alveary-like pattern: fixed 3x3x3 of "alveary_*" components,
	//     plain interior + plain top, wooden-slab cap, non-solid air ring. ---
	private static MultiblockPattern alvearyLike() {
		return MultiblockPattern.builder()
				.componentTypePrefix("alveary_")
				.sizeX(3, 3).sizeY(3, 3).sizeZ(3, 3)
				.minBlocks(27)
				.boxCellPredicate((sx, sy, sz, dx, dy, dz) -> {
					boolean top = dy == sy - 1;
					boolean interior = dx > 0 && dx < sx - 1 && dy > 0 && dy < sy - 1 && dz > 0 && dz < sz - 1;
					if (interior) {
						return Predicates.componentOfType("alveary_plain", Predicates.KEY_ALVEARY_NEED_PLAIN_INTERIOR);
					}
					if (top) {
						return Predicates.componentOfType("alveary_plain", Predicates.KEY_NEED_PLAIN_ON_TOP);
					}
					return Predicates.anyComponent("alveary_", Predicates.KEY_INVALID_PART);
				})
				.extraCells((sx, sy, sz) -> {
					Map<StructurePos, CellPredicate> extra = new HashMap<>();
					int topY = sy; // one above the box (box occupies y in [0, sy-1])
					CellPredicate slab = Predicates.woodenSlab(Predicates.KEY_NEED_SLABS);
					for (int x = 0; x < sx; x++) {
						for (int z = 0; z < sz; z++) {
							extra.put(new StructurePos(x, topY, z), slab);
						}
					}
					// air ring at y = sy-1 around the perimeter (excluding the box footprint)
					int ringY = sy - 1;
					CellPredicate space = Predicates.nonSolidRender(Predicates.KEY_NEED_SPACE);
					for (int x = -1; x <= sx; x++) {
						for (int z = -1; z <= sz; z++) {
							boolean insideBox = x >= 0 && x < sx && z >= 0 && z < sz;
							if (!insideBox) {
								extra.put(new StructurePos(x, ringY, z), space);
							}
						}
					}
					return extra;
				})
				.build();
	}

	// --- A minimal farm-like pattern: variable X,Z in [3,5], Y=4, plain interior + plain band (level 2),
	//     at least one gearbox component. ---
	private static MultiblockPattern farmLike() {
		return MultiblockPattern.builder()
				.componentTypePrefix("farm_")
				.sizeX(3, 5).sizeY(4, 4).sizeZ(3, 5)
				.minBlocks(3 * 3 * 4)
				.boxCellPredicate((sx, sy, sz, dx, dy, dz) -> {
					boolean band = dy == 2;
					boolean interior = dx > 0 && dx < sx - 1 && dy > 0 && dy < sy - 1 && dz > 0 && dz < sz - 1;
					if (interior) {
						return Predicates.componentOfType("farm_plain", Predicates.KEY_FARM_NEED_PLAIN_INTERIOR);
					}
					if (band) {
						return Predicates.componentOfType("farm_plain", Predicates.KEY_NEED_PLAIN_BAND);
					}
					return Predicates.anyComponent("farm_", Predicates.KEY_INVALID_PART);
				})
				.postCheck(components -> {
					for (PatternResult.Component c : components) {
						if (c.typeId().equals("farm_gearbox")) {
							return null;
						}
					}
					return Predicates.KEY_NEED_GEARBOX;
				})
				.build();
	}

	// --- builders for in-world layouts ---

	/** A complete 3x3x3 alveary at origin (0,0,0): plain everywhere, slab cap, clear air ring. */
	private static FakeStructureView.Builder fullAlveary() {
		FakeStructureView.Builder b = FakeStructureView.builder();
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				for (int z = 0; z < 3; z++) {
					b.component(x, y, z, "alveary_plain");
				}
			}
		}
		for (int x = 0; x < 3; x++) {
			for (int z = 0; z < 3; z++) {
				b.slab(x, 3, z);
			}
		}
		// air ring stays air (default), nothing to set
		return b;
	}

	/** A complete farm box of the given size with a gearbox at the bottom corner. */
	private static FakeStructureView.Builder fullFarm(int sx, int sy, int sz) {
		FakeStructureView.Builder b = FakeStructureView.builder();
		for (int x = 0; x < sx; x++) {
			for (int y = 0; y < sy; y++) {
				for (int z = 0; z < sz; z++) {
					b.component(x, y, z, "farm_plain");
				}
			}
		}
		// one gearbox somewhere on the non-band, non-interior shell (bottom corner is exterior level 0)
		b.component(0, 0, 0, "farm_gearbox");
		return b;
	}

	// --- Alveary tests ---

	@Test
	void completeAlvearyMatches() {
		MultiblockPattern pattern = alvearyLike();
		PatternResult result = pattern.validate(fullAlveary().build(), new StructurePos(0, 0, 0));
		PatternResult.Match match = assertInstanceOf(PatternResult.Match.class, result);
		assertEquals(27, match.members().size());
		assertEquals(new StructurePos(0, 0, 0), match.holder());
		assertEquals(new StructurePos(0, 0, 0), match.min());
		assertEquals(new StructurePos(2, 2, 2), match.max());
		// holder is the lowest member
		assertEquals(match.members().get(0), match.holder());
	}

	@Test
	void alvearyHolderIsLowestMemberWhenOriginNonZero() {
		MultiblockPattern pattern = alvearyLike();
		// shift everything to origin (10, 64, -5)
		FakeStructureView.Builder b = FakeStructureView.builder();
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				for (int z = 0; z < 3; z++) {
					b.component(10 + x, 64 + y, -5 + z, "alveary_plain");
				}
			}
		}
		for (int x = 0; x < 3; x++) {
			for (int z = 0; z < 3; z++) {
				b.slab(10 + x, 67, -5 + z);
			}
		}
		PatternResult result = pattern.validate(b.build(), new StructurePos(10, 64, -5));
		PatternResult.Match match = assertInstanceOf(PatternResult.Match.class, result);
		assertEquals(new StructurePos(10, 64, -5), match.holder());
		assertEquals(new StructurePos(12, 66, -3), match.max());
	}

	@Test
	void missingSlabFailsWithNeedSlabs() {
		MultiblockPattern pattern = alvearyLike();
		FakeStructureView.Builder b = fullAlveary();
		// remove one slab (leave it air)
		b.set(1, 3, 1, FakeStructureView.air());
		PatternResult result = pattern.validate(b.build(), new StructurePos(0, 0, 0));
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_NEED_SLABS, failure.firstKey());
	}

	@Test
	void nonComponentInteriorFails() {
		MultiblockPattern pattern = alvearyLike();
		FakeStructureView.Builder b = fullAlveary();
		// the single interior cell (1,1,1) becomes air -> not a component
		b.set(1, 1, 1, FakeStructureView.air());
		PatternResult result = pattern.validate(b.build(), new StructurePos(0, 0, 0));
		assertInstanceOf(PatternResult.Failure.class, result);
	}

	@Test
	void blockedAirRingFailsWithNeedSpace() {
		MultiblockPattern pattern = alvearyLike();
		FakeStructureView.Builder b = fullAlveary();
		// place a solid block in the entrance ring at y=2 (sy-1)
		b.solid(-1, 2, 1);
		PatternResult result = pattern.validate(b.build(), new StructurePos(0, 0, 0));
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_NEED_SPACE, failure.firstKey());
	}

	// --- Farm tests ---

	@Test
	void farm3x4x3Matches() {
		MultiblockPattern pattern = farmLike();
		PatternResult result = pattern.validate(fullFarm(3, 4, 3).build(), new StructurePos(0, 0, 0));
		PatternResult.Match match = assertInstanceOf(PatternResult.Match.class, result);
		assertEquals(3 * 4 * 3, match.members().size());
		assertEquals(new StructurePos(0, 0, 0), match.holder());
		assertEquals(new StructurePos(2, 3, 2), match.max());
	}

	@Test
	void farm5x4x5Matches() {
		MultiblockPattern pattern = farmLike();
		PatternResult result = pattern.validate(fullFarm(5, 4, 5).build(), new StructurePos(0, 0, 0));
		PatternResult.Match match = assertInstanceOf(PatternResult.Match.class, result);
		assertEquals(5 * 4 * 5, match.members().size());
		assertEquals(new StructurePos(4, 3, 4), match.max());
	}

	@Test
	void farmTwoWideFailsOnSizeX() {
		MultiblockPattern pattern = farmLike();
		// a 2x4x5 box of components (=40 blocks, above minBlocks=36) -> too narrow in X reaches small.x
		// (a 2x4x3 = 24-block box would fail the prior block-count check with error.small, matching the
		//  old RectangularMultiblockControllerBase ordering.)
		FakeStructureView.Builder b = FakeStructureView.builder();
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 0; z < 5; z++) {
					b.component(x, y, z, "farm_plain");
				}
			}
		}
		b.component(0, 0, 0, "farm_gearbox");
		PatternResult result = pattern.validate(b.build(), new StructurePos(0, 0, 0));
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_SMALL_X, failure.firstKey());
	}

	@Test
	void farmTooFewBlocksFailsWithSmall() {
		MultiblockPattern pattern = farmLike();
		// a 2x4x3 = 24-block box (< minBlocks 36) -> aggregate error.small (count check first, parity)
		FakeStructureView.Builder b = FakeStructureView.builder();
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 0; z < 3; z++) {
					b.component(x, y, z, "farm_plain");
				}
			}
		}
		b.component(0, 0, 0, "farm_gearbox");
		PatternResult result = pattern.validate(b.build(), new StructurePos(0, 0, 0));
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_SMALL, failure.firstKey());
	}

	@Test
	void farmNoGearboxFailsWithNeedGearbox() {
		MultiblockPattern pattern = farmLike();
		FakeStructureView.Builder b = FakeStructureView.builder();
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 0; z < 3; z++) {
					b.component(x, y, z, "farm_plain");
				}
			}
		}
		// no gearbox
		PatternResult result = pattern.validate(b.build(), new StructurePos(0, 0, 0));
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_NEED_GEARBOX, failure.firstKey());
	}

	// --- D1: maximality + loaded-shell ---

	@Test
	void d1_subPrismDoesNotMatchWhenShellHasSameTypeComponent() {
		MultiblockPattern pattern = farmLike();
		// build a full 5x4x3 farm, then validate the 3x4x3 SUB-region at origin (0,0,0).
		// The +X shell of the 3-wide box (x=3) contains farm components -> maximality non-match.
		FakeStructureView.Builder b = fullFarm(5, 4, 3);
		PatternResult result = pattern.validate(b.build(), new StructurePos(0, 0, 0));
		// at origin (0,0,0) the maximal extent is 5 wide, which is a valid 5x4x3 farm -> Match (5-wide),
		// NOT a 3-wide sub-machine. Assert it is the full 5-wide structure, never a shrunken 3x4x3.
		PatternResult.Match match = assertInstanceOf(PatternResult.Match.class, result);
		assertEquals(5 * 4 * 3, match.members().size());
		assertEquals(new StructurePos(4, 3, 2), match.max());
	}

	@Test
	void d1_subPrismOriginInsideLargerFarmIsNonMatch() {
		MultiblockPattern pattern = farmLike();
		// A 5x4x3 farm. Validate from an origin that is NOT the true min corner of a 3-wide sub-box,
		// i.e. origin (1,0,0): there is a same-type component at x=0 (below origin in X) -> non-maximal.
		FakeStructureView.Builder b = fullFarm(5, 4, 3);
		PatternResult result = pattern.validate(b.build(), new StructurePos(1, 0, 0));
		assertInstanceOf(PatternResult.Failure.class, result);
	}

	@Test
	void loadedShell_unloadedMemberCellIsNonMatch() {
		MultiblockPattern pattern = farmLike();
		FakeStructureView.Builder b = fullFarm(3, 4, 3);
		b.unloaded(2, 2, 2); // a member cell in an unloaded chunk
		PatternResult result = pattern.validate(b.build(), new StructurePos(0, 0, 0));
		assertInstanceOf(PatternResult.Failure.class, result);
	}

	@Test
	void loadedShell_unloadedShellCellIsNonMatch() {
		MultiblockPattern pattern = farmLike();
		FakeStructureView.Builder b = fullFarm(3, 4, 3);
		// the +X confirming shell at x=3 is unloaded -> cannot confirm maximality -> non-match
		b.unloaded(3, 0, 0);
		PatternResult result = pattern.validate(b.build(), new StructurePos(0, 0, 0));
		assertInstanceOf(PatternResult.Failure.class, result);
	}

	@Test
	void loadedFarmOnceFullyLoadedMatches() {
		// sanity: same farm with everything loaded matches (control for the unloaded cases)
		MultiblockPattern pattern = farmLike();
		PatternResult result = pattern.validate(fullFarm(3, 4, 3).build(), new StructurePos(0, 0, 0));
		assertTrue(result instanceof PatternResult.Match);
	}
}
