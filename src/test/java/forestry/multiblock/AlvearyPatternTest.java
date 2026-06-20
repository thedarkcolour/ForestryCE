package forestry.multiblock;

import org.junit.jupiter.api.Test;

import forestry.apiculture.multiblock.AlvearyPattern;
import forestry.core.multiblock.pattern.PatternResult;
import forestry.core.multiblock.pattern.Predicates;
import forestry.core.multiblock.pattern.StructurePos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Parity tests for {@link AlvearyPattern#ALVEARY_PATTERN}, modeling {@code AlvearyController.isMachineWhole}
 * + the base cube loop (each error path -> its exact key; valid -> Match).
 */
class AlvearyPatternTest {
	private static final StructurePos ORIGIN = new StructurePos(0, 0, 0);

	/** A complete, valid 3x3x3 alveary: plain everywhere, slab cap above, clear air ring. */
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

	@Test
	void validAlvearyMatches() {
		PatternResult result = validate(validAlveary());
		PatternResult.Match match = assertInstanceOf(PatternResult.Match.class, result);
		assertEquals(27, match.members().size());
		assertEquals(ORIGIN, match.holder());
		assertEquals(new StructurePos(2, 2, 2), match.max());
	}

	@Test
	void validAlvearyWithUpgradeBlocksMatches() {
		// bottom-corner heater + a fan + stabiliser on exterior levels 0/1 are allowed (any alveary block)
		FakeStructureView.Builder b = validAlveary();
		b.component(0, 0, 0, "alveary_heater");
		b.component(2, 0, 2, "alveary_fan");
		b.component(0, 1, 0, "alveary_stabiliser");
		PatternResult result = validate(b);
		assertInstanceOf(PatternResult.Match.class, result);
	}

	@Test
	void nonPlainInteriorFailsWithNeedPlainInterior() {
		FakeStructureView.Builder b = validAlveary();
		b.component(1, 1, 1, "alveary_heater"); // interior must be plain
		PatternResult result = validate(b);
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_ALVEARY_NEED_PLAIN_INTERIOR, failure.firstKey());
	}

	@Test
	void nonPlainTopFailsWithNeedPlainOnTop() {
		FakeStructureView.Builder b = validAlveary();
		b.component(1, 2, 1, "alveary_heater"); // top-center, exterior level 2 -> must be plain
		PatternResult result = validate(b);
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_NEED_PLAIN_ON_TOP, failure.firstKey());
	}

	@Test
	void missingSlabFailsWithNeedSlabs() {
		FakeStructureView.Builder b = validAlveary();
		b.set(1, 3, 1, FakeStructureView.air());
		PatternResult result = validate(b);
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_NEED_SLABS, failure.firstKey());
	}

	@Test
	void blockedAirRingFailsWithNeedSpace() {
		FakeStructureView.Builder b = validAlveary();
		b.solid(-1, 2, 1); // a solid block in the entrance ring (y = maxY = 2)
		PatternResult result = validate(b);
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_NEED_SPACE, failure.firstKey());
	}

	@Test
	void slabErrorReportedBeforeSpaceError() {
		// parity: super cube -> slab -> air ring; slab failure must win over a concurrent space failure
		FakeStructureView.Builder b = validAlveary();
		b.set(1, 3, 1, FakeStructureView.air()); // missing slab
		b.solid(-1, 2, 1);                       // and a blocked ring
		PatternResult result = validate(b);
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		assertEquals(Predicates.KEY_NEED_SLABS, failure.firstKey());
	}

	@Test
	void nonComponentInsidePrismFailsWithInvalidInterior() {
		FakeStructureView.Builder b = validAlveary();
		b.set(0, 1, 0, FakeStructureView.air()); // exterior-level-1 cell becomes a non-component (air)
		PatternResult result = validate(b);
		PatternResult.Failure failure = assertInstanceOf(PatternResult.Failure.class, result);
		// air inside the prism breaks the contiguous box measurement -> deferred/invalid (no false match)
		assertEquals(Predicates.KEY_INVALID_INTERIOR, failure.firstKey());
	}
}
