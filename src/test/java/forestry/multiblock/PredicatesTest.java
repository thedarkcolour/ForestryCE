package forestry.multiblock;

import org.junit.jupiter.api.Test;

import forestry.core.multiblock.pattern.CellPredicate;
import forestry.core.multiblock.pattern.Predicates;
import forestry.core.multiblock.pattern.StructureView.CellSample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Verifies each cell predicate maps a failing cell to the exact parity translation key the old engine
 * produced (spec §5.1; keys cross-checked against AlvearyController / FarmController and en_us.json).
 */
class PredicatesTest {
	private static final CellSample AIR = new CellSample(false, null, false, false, null);

	private static CellSample component(String typeId) {
		return new CellSample(true, new Object(), false, false, typeId);
	}

	private static CellSample slab() {
		return new CellSample(false, null, true, false, null);
	}

	private static CellSample solid() {
		return new CellSample(false, null, false, true, null);
	}

	// --- componentOfType (mustBePlain variants) ---

	@Test
	void componentOfType_passesForExactType() {
		CellPredicate plain = Predicates.componentOfType("alveary_plain", "for.multiblock.alveary.error.needPlainInterior");
		assertNull(plain.test(component("alveary_plain")));
	}

	@Test
	void componentOfType_nonComponentCellIsInvalidInterior() {
		// A non-Forestry block inside the prism: base isBlockGoodFor* throws invalid.interior.
		CellPredicate plain = Predicates.componentOfType("alveary_plain", "for.multiblock.alveary.error.needPlainInterior");
		assertEquals("for.multiblock.error.invalid.interior", plain.test(AIR));
	}

	@Test
	void componentOfType_wrongComponentTypeReturnsFailKey() {
		// A component that exists but is the wrong (non-plain) type: needPlain* key.
		CellPredicate plain = Predicates.componentOfType("alveary_plain", "for.multiblock.alveary.error.needPlainInterior");
		assertEquals("for.multiblock.alveary.error.needPlainInterior", plain.test(component("alveary_heater")));
	}

	@Test
	void componentOfType_farmBandKey() {
		CellPredicate band = Predicates.componentOfType("farm_plain", "for.multiblock.farm.error.needPlainBand");
		assertNull(band.test(component("farm_plain")));
		assertEquals("for.multiblock.farm.error.needPlainBand", band.test(component("farm_gearbox")));
		assertEquals("for.multiblock.error.invalid.interior", band.test(AIR));
	}

	// --- anyComponent (anyAlvearyBlock / any farm block) ---

	@Test
	void anyComponent_passesForAnyMatchingPrefix() {
		CellPredicate any = Predicates.anyComponent("alveary_", "for.multiblock.error.invalid.part");
		assertNull(any.test(component("alveary_plain")));
		assertNull(any.test(component("alveary_heater")));
		assertNull(any.test(component("alveary_swarmer")));
	}

	@Test
	void anyComponent_nonComponentIsInvalidInterior() {
		CellPredicate any = Predicates.anyComponent("alveary_", "for.multiblock.error.invalid.part");
		assertEquals("for.multiblock.error.invalid.interior", any.test(AIR));
	}

	@Test
	void anyComponent_wrongControllerTypeIsInvalidPart() {
		// A component of a different machine type inside this prism -> invalid.part.
		CellPredicate any = Predicates.anyComponent("alveary_", "for.multiblock.error.invalid.part");
		assertEquals("for.multiblock.error.invalid.part", any.test(component("farm_plain")));
	}

	// --- woodenSlab (needSlabs) ---

	@Test
	void woodenSlab_passesForSlab() {
		CellPredicate slabPred = Predicates.woodenSlab("for.multiblock.alveary.error.needSlabs");
		assertNull(slabPred.test(slab()));
	}

	@Test
	void woodenSlab_failsForNonSlab() {
		CellPredicate slabPred = Predicates.woodenSlab("for.multiblock.alveary.error.needSlabs");
		assertEquals("for.multiblock.alveary.error.needSlabs", slabPred.test(AIR));
		assertEquals("for.multiblock.alveary.error.needSlabs", slabPred.test(component("alveary_plain")));
	}

	// --- nonSolidRender (needSpace) ---

	@Test
	void nonSolidRender_passesForAir() {
		CellPredicate space = Predicates.nonSolidRender("for.multiblock.alveary.error.needSpace");
		assertNull(space.test(AIR));
	}

	@Test
	void nonSolidRender_failsForSolid() {
		CellPredicate space = Predicates.nonSolidRender("for.multiblock.alveary.error.needSpace");
		assertEquals("for.multiblock.alveary.error.needSpace", space.test(solid()));
	}

	@Test
	void nonSolidRender_componentCellIsSkipped() {
		// The old air-ring loop skips cells that are part of the multiblock (isCoordInMultiblock continue).
		CellPredicate space = Predicates.nonSolidRender("for.multiblock.alveary.error.needSpace");
		assertNull(space.test(component("alveary_plain")));
	}

	// --- parity key constants pinned centrally ---

	@Test
	void parityKeyConstantsAreExact() {
		assertEquals("for.multiblock.alveary.error.needSlabs", Predicates.KEY_NEED_SLABS);
		assertEquals("for.multiblock.alveary.error.needSpace", Predicates.KEY_NEED_SPACE);
		assertEquals("for.multiblock.alveary.error.needPlainOnTop", Predicates.KEY_NEED_PLAIN_ON_TOP);
		assertEquals("for.multiblock.alveary.error.needPlainInterior", Predicates.KEY_ALVEARY_NEED_PLAIN_INTERIOR);
		assertEquals("for.multiblock.farm.error.needGearbox", Predicates.KEY_NEED_GEARBOX);
		assertEquals("for.multiblock.farm.error.needPlainBand", Predicates.KEY_NEED_PLAIN_BAND);
		assertEquals("for.multiblock.farm.error.needPlainInterior", Predicates.KEY_FARM_NEED_PLAIN_INTERIOR);
		assertEquals("for.multiblock.error.invalid.interior", Predicates.KEY_INVALID_INTERIOR);
		assertEquals("for.multiblock.error.invalid.part", Predicates.KEY_INVALID_PART);
		assertEquals("for.multiblock.error.small", Predicates.KEY_SMALL);
		assertEquals("for.multiblock.error.small.x", Predicates.KEY_SMALL_X);
		assertEquals("for.multiblock.error.small.y", Predicates.KEY_SMALL_Y);
		assertEquals("for.multiblock.error.small.z", Predicates.KEY_SMALL_Z);
		assertEquals("for.multiblock.error.large.x", Predicates.KEY_LARGE_X);
		assertEquals("for.multiblock.error.large.y", Predicates.KEY_LARGE_Y);
		assertEquals("for.multiblock.error.large.z", Predicates.KEY_LARGE_Z);
	}
}
