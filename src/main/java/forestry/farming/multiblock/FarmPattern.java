package forestry.farming.multiblock;

import forestry.core.multiblock.pattern.CellPredicate;
import forestry.core.multiblock.pattern.MultiblockPattern;
import forestry.core.multiblock.pattern.PatternResult;
import forestry.core.multiblock.pattern.Predicates;

/**
 * The declarative Farm pattern (spec §5.1), modeling {@code FarmController.isMachineWhole} plus the base
 * cube loop verbatim:
 * <ul>
 *   <li>a variable box of {@code farm_*} components: X,Z in [3,5], Y = 4 (minBlocks 36),</li>
 *   <li>interior cell must be plain ({@code needPlainInterior}),</li>
 *   <li>level-2 exterior band must be plain ({@code needPlainBand}),</li>
 *   <li>other exterior cells may be any farm component,</li>
 *   <li>at least one gearbox among the members ({@code needGearbox}).</li>
 * </ul>
 *
 * <p>The Farm has no slab/air-ring (no extra cells). Still {@code net.minecraft}-free.
 */
public final class FarmPattern {
	/** Pattern component type ids (also used by the Phase-2 LevelStructureView to tag block entities). */
	public static final String PREFIX = "farm_";
	public static final String PLAIN = "farm_plain";
	public static final String GEARBOX = "farm_gearbox";

	public static final MultiblockPattern FARM_PATTERN = MultiblockPattern.builder()
			.componentTypePrefix(PREFIX)
			.sizeX(3, 5).sizeY(4, 4).sizeZ(3, 5)
			.minBlocks(3 * 3 * 4)
			.boxCellPredicate(FarmPattern::cellPredicate)
			.postCheck(FarmPattern::hasGearbox)
			.build();

	private FarmPattern() {
	}

	private static CellPredicate cellPredicate(int sizeX, int sizeY, int sizeZ, int dx, int dy, int dz) {
		boolean interior = dx > 0 && dx < sizeX - 1
				&& dy > 0 && dy < sizeY - 1
				&& dz > 0 && dz < sizeZ - 1;
		if (interior) {
			// FarmController.isGoodForInterior: must be plain
			return Predicates.componentOfType(PLAIN, Predicates.KEY_FARM_NEED_PLAIN_INTERIOR);
		}
		// FarmController.isGoodForExteriorLevel(level == 2): the band must be plain. exteriorLevel == dy
		// (= y - minY). Only the exterior (non-interior) cells reach this branch, matching the base loop.
		boolean band = dy == 2;
		if (band) {
			return Predicates.componentOfType(PLAIN, Predicates.KEY_NEED_PLAIN_BAND);
		}
		// any other exterior farm component is fine; wrong-controller component -> invalid.part.
		return Predicates.anyComponent(PREFIX, Predicates.KEY_INVALID_PART);
	}

	/** FarmController.isMachineWhole: there must be at least one gearbox among the parts. */
	private static String hasGearbox(java.util.List<PatternResult.Component> components) {
		for (PatternResult.Component c : components) {
			if (GEARBOX.equals(c.typeId())) {
				return null;
			}
		}
		return Predicates.KEY_NEED_GEARBOX;
	}
}
