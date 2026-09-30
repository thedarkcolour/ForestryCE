package forestry.core.platform.multiblock.pattern;

import forestry.core.platform.multiblock.pattern.StructureView.CellSample;

/**
 * Factory methods for the cell predicates that drive multiblock validation, plus the inventory of
 * parity translation keys (spec 5.1). Every key here was cross-checked against
 * {@code AlvearyController.isMachineWhole} and {@code FarmController.isMachineWhole}, the base
 * {@code RectangularMultiblockControllerBase} and {@code MultiblockControllerBase}, and
 * {@code assets/forestry/lang/en_us.json}, so the new engine surfaces identical error messages.
 *
 * <p>This class is deliberately {@code net.minecraft}-free.
 */
public final class Predicates {
	// --- Parity translation keys, pinned centrally so patterns and the validator agree ---

	/** Alveary slab cap missing ({@code AlvearyController.isMachineWhole}). */
	public static final String KEY_NEED_SLABS = "for.multiblock.alveary.error.needSlabs";
	/** Alveary entrance air-ring blocked ({@code AlvearyController.isMachineWhole}). */
	public static final String KEY_NEED_SPACE = "for.multiblock.alveary.error.needSpace";
	/** Alveary top exterior layer not plain ({@code AlvearyController.isGoodForExteriorLevel}). */
	public static final String KEY_NEED_PLAIN_ON_TOP = "for.multiblock.alveary.error.needPlainOnTop";
	/** Alveary interior not plain ({@code AlvearyController.isGoodForInterior}). */
	public static final String KEY_ALVEARY_NEED_PLAIN_INTERIOR = "for.multiblock.alveary.error.needPlainInterior";
	/** Farm has no gearbox ({@code FarmController.isMachineWhole}). */
	public static final String KEY_NEED_GEARBOX = "for.multiblock.farm.error.needGearbox";
	/** Farm level-2 band not plain ({@code FarmController.isGoodForExteriorLevel}). */
	public static final String KEY_NEED_PLAIN_BAND = "for.multiblock.farm.error.needPlainBand";
	/** Farm interior not plain ({@code FarmController.isGoodForInterior}). */
	public static final String KEY_FARM_NEED_PLAIN_INTERIOR = "for.multiblock.farm.error.needPlainInterior";

	/**
	 * A cell inside the prism that is NOT a Forestry component. The {@code part == null} branch in
	 * {@code RectangularMultiblockControllerBase} falls through to the base
	 * {@code isBlockGoodForInterior} and {@code isBlockGoodForExteriorLevel}, which throw this.
	 */
	public static final String KEY_INVALID_INTERIOR = "for.multiblock.error.invalid.interior";
	/**
	 * A cell that IS a component but of the wrong controller type. This is the
	 * {@code myClass.equals(...controller...)} guard in {@code RectangularMultiblockControllerBase}.
	 */
	public static final String KEY_INVALID_PART = "for.multiblock.error.invalid.part";

	/**
	 * <b>Internal "wrong candidate origin" signal, never shown to the player.</b> Returned by the
	 * <em>lower-face</em> maximality pre-check in {@link MultiblockPattern#validate} when a same-type
	 * component sits just below the candidate origin. The origin is therefore NOT the structure's lowest
	 * member, so this candidate must defer to the one rooted at the true min corner. It is a discovery
	 * artefact of probing many permissive candidate origins (spec 5.3), not a real content or size error.
	 *
	 * <p>{@link forestry.core.platform.multiblock.MultiblockValidation#findValidationHint} ranks it <em>below</em>
	 * every real content or size key so the meaningful failure from the true-min-corner candidate wins,
	 * such as {@code error.small} for an undersized alveary or {@code invalid.interior} for an interior
	 * hole. Otherwise this generic deferral leaks the misleading "incompatible part (%s)" message. It is
	 * not a parity key, because the old engine had no such code path, and it has no lang entry. It is
	 * consumed internally.
	 */
	public static final String KEY_NOT_MAXIMAL = "for.multiblock.error.internal.notMaximal";

	// internal like KEY_NOT_MAXIMAL: a cell the query needs is in an unloaded chunk, so the structure is undecided and
	// validation is retried once that chunk loads
	public static final String KEY_NOT_LOADED = "for.multiblock.error.internal.notLoaded";

	/** Too few blocks for the minimum machine size ({@code error.small}, 3 dimension args). */
	public static final String KEY_SMALL = "for.multiblock.error.small";
	public static final String KEY_SMALL_X = "for.multiblock.error.small.x";
	public static final String KEY_SMALL_Y = "for.multiblock.error.small.y";
	public static final String KEY_SMALL_Z = "for.multiblock.error.small.z";
	public static final String KEY_LARGE_X = "for.multiblock.error.large.x";
	public static final String KEY_LARGE_Y = "for.multiblock.error.large.y";
	public static final String KEY_LARGE_Z = "for.multiblock.error.large.z";

	private Predicates() {
	}

	// --- Predicate factories ---

	/**
	 * Builds a predicate requiring the cell to be a Forestry component of exactly the required type id.
	 * Ex. {@code "alveary_plain"}, {@code "farm_plain"}
	 *
	 * <p>Mirrors the controllers' {@code isGoodForInterior} and {@code isGoodForExteriorLevel} "must be plain"
	 * branches layered on the base cube loop:
	 * <ul>
	 *   <li>not a component at all -> {@link #KEY_INVALID_INTERIOR} (base {@code isBlockGoodFor*})</li>
	 *   <li>a component of the wrong type -> the wrong-component fail key (ex. {@code needPlainInterior})</li>
	 *   <li>otherwise ok</li>
	 * </ul>
	 */
	public static CellPredicate componentOfType(String requiredTypeId, String failKeyIfWrongComponent) {
		return sample -> {
			if (!sample.isComponent()) {
				return KEY_INVALID_INTERIOR;
			}
			if (!requiredTypeId.equals(sample.componentTypeId())) {
				return failKeyIfWrongComponent;
			}
			return null;
		};
	}

	/**
	 * Builds a predicate requiring the cell to be ANY Forestry component of this machine, meaning its type id
	 * starts with the given prefix. Ex. {@code "alveary_"}, {@code "farm_"}
	 *
	 * <p>Mirrors the base cube loop's component-type guard:
	 * <ul>
	 *   <li>not a component at all -> {@link #KEY_INVALID_INTERIOR}</li>
	 *   <li>a component of a different machine type -> the wrong-type key, usually
	 *       {@link #KEY_INVALID_PART}</li>
	 *   <li>otherwise ok</li>
	 * </ul>
	 */
	public static CellPredicate anyComponent(String typePrefix, String wrongTypeKey) {
		return sample -> {
			if (!sample.isComponent()) {
				return KEY_INVALID_INTERIOR;
			}
			String typeId = sample.componentTypeId();
			if (typeId == null || !typeId.startsWith(typePrefix)) {
				return wrongTypeKey;
			}
			return null;
		};
	}

	/**
	 * Builds a predicate requiring the cell to be a wooden slab, the alveary cap, meaning
	 * {@code BlockTags.WOODEN_SLABS}. Anything else, such as air, a component or a solid block, returns the
	 * fail key, usually {@link #KEY_NEED_SLABS}.
	 */
	public static CellPredicate woodenSlab(String failKey) {
		return sample -> sample.isWoodenSlab() ? null : failKey;
	}

	/**
	 * Builds a predicate requiring the cell NOT to render solid, which is the alveary entrance air ring. A
	 * cell that is part of the multiblock, meaning a component, is skipped and passes. That matches the old
	 * loop's {@code isCoordInMultiblock} continue. A solid-render non-component block returns the fail key,
	 * usually {@link #KEY_NEED_SPACE}.
	 */
	public static CellPredicate nonSolidRender(String failKey) {
		return sample -> {
			if (sample.isComponent()) {
				return null;
			}
			return sample.isSolidRender() ? failKey : null;
		};
	}
}
