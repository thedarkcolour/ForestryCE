package forestry.multiblock;

import org.junit.jupiter.api.Test;

import forestry.core.multiblock.pattern.PatternResult;
import forestry.core.multiblock.pattern.Predicates;
import forestry.core.multiblock.pattern.StructurePos;
import forestry.farming.multiblock.FarmPattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Parity tests for {@link FarmPattern#FARM_PATTERN}, modeling {@code FarmController.isMachineWhole}
 * + the base cube loop: size range X,Z in [3,5] Y=4, plain interior, plain band (level 2), >=1 gearbox.
 */
class FarmPatternTest {
	private static final StructurePos ORIGIN = new StructurePos(0, 0, 0);

	/** A complete, valid farm of size sx*sy*sz, all plain, with a gearbox at a non-band exterior cell. */
	private static FakeStructureView.Builder validFarm(int sx, int sy, int sz) {
		FakeStructureView.Builder b = FakeStructureView.builder();
		for (int x = 0; x < sx; x++) {
			for (int y = 0; y < sy; y++) {
				for (int z = 0; z < sz; z++) {
					b.component(x, y, z, FarmPattern.PLAIN);
				}
			}
		}
		// gearbox at the bottom corner (exterior level 0, not the band)
		b.component(0, 0, 0, FarmPattern.GEARBOX);
		return b;
	}

	private static PatternResult validate(FakeStructureView.Builder b) {
		return FarmPattern.FARM_PATTERN.validate(b.build(), ORIGIN);
	}

	@Test
	void minFarm3x4x3Matches() {
		PatternResult result = validate(validFarm(3, 4, 3));
		PatternResult.Match match = assertInstanceOf(PatternResult.Match.class, result);
		assertEquals(3 * 4 * 3, match.members().size());
		assertEquals(ORIGIN, match.holder());
		assertEquals(new StructurePos(2, 3, 2), match.max());
	}

	@Test
	void maxFarm5x4x5Matches() {
		PatternResult result = validate(validFarm(5, 4, 5));
		PatternResult.Match match = assertInstanceOf(PatternResult.Match.class, result);
		assertEquals(5 * 4 * 5, match.members().size());
		assertEquals(new StructurePos(4, 3, 4), match.max());
	}

	@Test
	void asymmetricFarm5x4x3Matches() {
		PatternResult result = validate(validFarm(5, 4, 3));
		PatternResult.Match match = assertInstanceOf(PatternResult.Match.class, result);
		assertEquals(5 * 4 * 3, match.members().size());
	}

	@Test
	void noGearboxFailsWithNeedGearbox() {
		FakeStructureView.Builder b = FakeStructureView.builder();
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 0; z < 3; z++) {
					b.component(x, y, z, FarmPattern.PLAIN);
				}
			}
		}
		PatternResult result = validate(b);
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_NEED_GEARBOX, failure.firstKey());
	}

	@Test
	void nonPlainBandFailsWithNeedPlainBand() {
		// level-2 band exterior cell must be plain; place a hatch there (a farm component, wrong type)
		FakeStructureView.Builder b = validFarm(3, 4, 3);
		b.component(0, 2, 1, "farm_hatch"); // (x=0 exterior, y=2 band) -> needPlainBand
		PatternResult result = validate(b);
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_NEED_PLAIN_BAND, failure.firstKey());
	}

	@Test
	void nonPlainInteriorFailsWithNeedPlainInterior() {
		// interior cell of a 3x4x3: (1,1,1) and (1,2,1) are interior. Use (1,1,1).
		FakeStructureView.Builder b = validFarm(3, 4, 3);
		b.component(1, 1, 1, "farm_hatch");
		PatternResult result = validate(b);
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_FARM_NEED_PLAIN_INTERIOR, failure.firstKey());
	}

	@Test
	void tooSmallFailsWithSmall() {
		// a 2x4x3 = 24 block farm (< minBlocks 36) -> aggregate error.small (count check first)
		FakeStructureView.Builder b = FakeStructureView.builder();
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 0; z < 3; z++) {
					b.component(x, y, z, FarmPattern.PLAIN);
				}
			}
		}
		b.component(0, 0, 0, FarmPattern.GEARBOX);
		PatternResult result = validate(b);
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_SMALL, failure.firstKey());
	}

	@Test
	void d1_subPrismNeverFormsSmallerMachine() {
		// a full 5x4x3 farm validated at origin (0,0,0) forms the full 5-wide machine, never a 3x4x3.
		PatternResult result = validate(validFarm(5, 4, 3));
		PatternResult.Match match = assertInstanceOf(PatternResult.Match.class, result);
		assertEquals(5 * 4 * 3, match.members().size());
	}

	@Test
	void loadedShellUnloadedCellDefers() {
		FakeStructureView.Builder b = validFarm(3, 4, 3);
		b.unloaded(2, 3, 2); // a member cell in an unloaded chunk
		PatternResult result = validate(b);
		assertInstanceOf(PatternResult.Failure.class, result);
	}
}
