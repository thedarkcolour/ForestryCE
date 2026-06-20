package forestry.apiculture.multiblock;

import java.util.LinkedHashMap;
import java.util.Map;

import forestry.core.multiblock.pattern.CellPredicate;
import forestry.core.multiblock.pattern.MultiblockPattern;
import forestry.core.multiblock.pattern.Predicates;
import forestry.core.multiblock.pattern.StructurePos;

/**
 * The declarative Alveary pattern (spec §5.1), modeling {@code AlvearyController.isMachineWhole} plus the
 * base cube loop verbatim:
 * <ul>
 *   <li>a fixed 3×3×3 box of {@code alveary_*} components (minBlocks 27),</li>
 *   <li>interior cell must be plain ({@code needPlainInterior}),</li>
 *   <li>top exterior layer (level 2) must be plain ({@code needPlainOnTop}),</li>
 *   <li>other exterior cells may be any alveary component,</li>
 *   <li>a 3×3 wooden-slab cap one block above ({@code needSlabs}),</li>
 *   <li>a non-solid entrance air ring around the top layer ({@code needSpace}).</li>
 * </ul>
 *
 * <p>Still {@code net.minecraft}-free: it references {@link Predicates} and pattern component type-id
 * strings only. The Phase-2 world adapter maps each in-world block entity to one of these type ids.
 *
 * <p>Parity ordering: the cube loop runs first, then the slab cap, then the air ring — so a missing-slab
 * error is reported before a blocked-ring error (the extra cells use a {@link LinkedHashMap} with the
 * slab cells inserted before the ring cells).
 */
public final class AlvearyPattern {
	/** Pattern component type ids (also used by the Phase-2 LevelStructureView to tag block entities). */
	public static final String PREFIX = "alveary_";
	public static final String PLAIN = "alveary_plain";

	public static final MultiblockPattern ALVEARY_PATTERN = MultiblockPattern.builder()
			.componentTypePrefix(PREFIX)
			.sizeX(3, 3).sizeY(3, 3).sizeZ(3, 3)
			.minBlocks(27)
			.boxCellPredicate(AlvearyPattern::cellPredicate)
			.extraCells(AlvearyPattern::extraCells)
			.build();

	private AlvearyPattern() {
	}

	private static CellPredicate cellPredicate(int sizeX, int sizeY, int sizeZ, int dx, int dy, int dz) {
		boolean interior = dx > 0 && dx < sizeX - 1
				&& dy > 0 && dy < sizeY - 1
				&& dz > 0 && dz < sizeZ - 1;
		if (interior) {
			// AlvearyController.isGoodForInterior: must be plain
			return Predicates.componentOfType(PLAIN, Predicates.KEY_ALVEARY_NEED_PLAIN_INTERIOR);
		}
		boolean top = dy == sizeY - 1;
		if (top) {
			// AlvearyController.isGoodForExteriorLevel(level == 2): must be plain
			return Predicates.componentOfType(PLAIN, Predicates.KEY_NEED_PLAIN_ON_TOP);
		}
		// any other exterior cell may be any alveary component (heater/fan/sieve/...). The base cube loop's
		// component-type guard maps a wrong-controller component to invalid.part.
		return Predicates.anyComponent(PREFIX, Predicates.KEY_INVALID_PART);
	}

	private static Map<StructurePos, CellPredicate> extraCells(int sizeX, int sizeY, int sizeZ) {
		// LinkedHashMap: slabs first, then ring, so slab failures win over space failures (parity order).
		Map<StructurePos, CellPredicate> extra = new LinkedHashMap<>();

		// Slab cap: the 3×3 footprint one block above the box (y = sizeY, i.e. maxY + 1).
		CellPredicate slab = Predicates.woodenSlab(Predicates.KEY_NEED_SLABS);
		int slabY = sizeY;
		for (int x = 0; x < sizeX; x++) {
			for (int z = 0; z < sizeZ; z++) {
				extra.put(new StructurePos(x, slabY, z), slab);
			}
		}

		// Entrance air ring: the perimeter at y = sizeY - 1 (= maxY), one cell out on each side. Cells that
		// lie inside the box footprint are skipped (they ARE the multiblock, mirroring isCoordInMultiblock).
		CellPredicate space = Predicates.nonSolidRender(Predicates.KEY_NEED_SPACE);
		int ringY = sizeY - 1;
		for (int x = -1; x <= sizeX; x++) {
			for (int z = -1; z <= sizeZ; z++) {
				boolean insideFootprint = x >= 0 && x < sizeX && z >= 0 && z < sizeZ;
				if (!insideFootprint) {
					extra.put(new StructurePos(x, ringY, z), space);
				}
			}
		}
		return extra;
	}
}
