package forestry.core.multiblock.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import forestry.core.multiblock.pattern.PatternResult.Component;
import forestry.core.multiblock.pattern.PatternResult.FailingCell;
import forestry.core.multiblock.pattern.StructureView.CellSample;

/**
 * A declarative, {@code net.minecraft}-free multiblock pattern (spec §5). Given a {@link StructureView}
 * and a candidate origin (the would-be lowest member / min corner, spec §6.1), {@link #validate}
 * performs a single stateless query and returns a {@link PatternResult}.
 *
 * <p>The structure is a solid box of same-type Forestry components (spec §5.1) whose extent is measured
 * from the origin, plus optional "extra" cells outside the box (the alveary's slab cap and entrance air
 * ring). The box may be fixed-size (alveary) or fall within per-axis ranges (farm). Validation enforces
 * the size range, every cell predicate, an optional whole-structure post-check (e.g. gearbox-present),
 * and the maximality + loaded-shell rule that fixes audit D1 (spec §5.2).
 */
public final class MultiblockPattern {
	/** Predicate for a single box cell, parameterised by box size and the cell's offset within it. */
	@FunctionalInterface
	public interface BoxCellPredicate {
		CellPredicate predicateFor(int sizeX, int sizeY, int sizeZ, int dx, int dy, int dz);
	}

	/** Produces the non-box "extra" cells (slab cap, air ring) as offsets relative to the origin. */
	@FunctionalInterface
	public interface ExtraCellsFactory {
		Map<StructurePos, CellPredicate> extraCells(int sizeX, int sizeY, int sizeZ);
	}

	/** A whole-structure check over the matched components; returns {@code null} or a failure key. */
	@FunctionalInterface
	public interface PostCheck {
		String check(List<Component> components);
	}

	private final String componentTypePrefix;
	private final int minSizeX;
	private final int maxSizeX;
	private final int minSizeY;
	private final int maxSizeY;
	private final int minSizeZ;
	private final int maxSizeZ;
	private final int minBlocks;
	private final BoxCellPredicate boxCellPredicate;
	private final ExtraCellsFactory extraCellsFactory;
	private final List<PostCheck> postChecks;

	private MultiblockPattern(Builder b) {
		this.componentTypePrefix = Objects.requireNonNull(b.componentTypePrefix, "componentTypePrefix");
		this.minSizeX = b.minSizeX;
		this.maxSizeX = b.maxSizeX;
		this.minSizeY = b.minSizeY;
		this.maxSizeY = b.maxSizeY;
		this.minSizeZ = b.minSizeZ;
		this.maxSizeZ = b.maxSizeZ;
		this.minBlocks = b.minBlocks;
		this.boxCellPredicate = Objects.requireNonNull(b.boxCellPredicate, "boxCellPredicate");
		this.extraCellsFactory = b.extraCellsFactory;
		this.postChecks = List.copyOf(b.postChecks);
	}

	/** Whether a sampled cell is a same-type component of this machine (used for measurement/maximality). */
	private boolean isSameTypeComponent(CellSample sample) {
		return sample.isComponent()
				&& sample.componentTypeId() != null
				&& sample.componentTypeId().startsWith(this.componentTypePrefix);
	}

	/**
	 * Runs the stateless validation query (spec §5.2) for {@code origin} treated as the structure's
	 * lowest member / min corner. Returns a {@link PatternResult.Match} only for the true, maximal,
	 * fully-loaded structure; otherwise a {@link PatternResult.Failure}.
	 */
	public PatternResult validate(StructureView view, StructurePos origin) {
		// --- Maximality (lower faces): origin must actually be the lowest member. If a same-type
		// component sits just below origin on any axis, this origin is non-maximal -> defer. We require
		// those confirming cells to be loaded (loaded-shell). ---
		for (StructurePos below : new StructurePos[]{
				origin.offset(-1, 0, 0), origin.offset(0, -1, 0), origin.offset(0, 0, -1)}) {
			if (!view.isLoaded(below)) {
				return failure(below, Predicates.KEY_INVALID_INTERIOR);
			}
			if (isSameTypeComponent(view.sample(below))) {
				return failure(below, Predicates.KEY_INVALID_PART);
			}
		}

		// --- Measure the maximal contiguous same-type component box growing from origin in +X/+Y/+Z.
		// Each grown layer must be fully loaded (loaded-shell) and fully same-type-component. ---
		Measure measure = measureBox(view, origin);
		if (measure == null) {
			// a required cell was unloaded while measuring -> cannot confirm extent -> defer
			return failure(origin, Predicates.KEY_INVALID_INTERIOR);
		}
		int sizeX = measure.sizeX;
		int sizeY = measure.sizeY;
		int sizeZ = measure.sizeZ;

		// --- Size-range check (parity small/large keys). ---
		PatternResult.Failure sizeFailure = checkSize(origin, sizeX, sizeY, sizeZ);
		if (sizeFailure != null) {
			return sizeFailure;
		}

		// --- Loaded check for the whole box + extra cells before running predicates. ---
		// (box cells are already confirmed loaded by measureBox; check extra cells here)
		Map<StructurePos, CellPredicate> extra = this.extraCellsFactory == null
				? Map.of()
				: this.extraCellsFactory.extraCells(sizeX, sizeY, sizeZ);
		for (StructurePos rel : extra.keySet()) {
			StructurePos worldPos = origin.offset(rel.x(), rel.y(), rel.z());
			if (!view.isLoaded(worldPos)) {
				return failure(worldPos, Predicates.KEY_INVALID_INTERIOR);
			}
		}

		// --- Maximality (upper faces): the layer just beyond each grown face must NOT be same-type
		// components and must be loaded. measureBox stopped growing because those layers were not all
		// same-type; but for ranges below max we still must confirm the next layer is loaded & clear. ---
		PatternResult.Failure maximalityFailure = checkUpperMaximality(view, origin, sizeX, sizeY, sizeZ);
		if (maximalityFailure != null) {
			return maximalityFailure;
		}

		// --- Run the box cell predicates. Every box cell must also be loaded (loaded-shell rule, spec
		// §5.2): an unloaded member cell means we cannot confirm the structure, so we defer (non-match)
		// rather than assembling on a partial footprint. ---
		List<Component> components = new ArrayList<>(sizeX * sizeY * sizeZ);
		List<StructurePos> members = new ArrayList<>(sizeX * sizeY * sizeZ);
		for (int dx = 0; dx < sizeX; dx++) {
			for (int dy = 0; dy < sizeY; dy++) {
				for (int dz = 0; dz < sizeZ; dz++) {
					StructurePos worldPos = origin.offset(dx, dy, dz);
					if (!view.isLoaded(worldPos)) {
						return failure(worldPos, Predicates.KEY_INVALID_INTERIOR);
					}
					CellSample sample = view.sample(worldPos);
					CellPredicate predicate = this.boxCellPredicate.predicateFor(sizeX, sizeY, sizeZ, dx, dy, dz);
					String fail = predicate.test(sample);
					if (fail != null) {
						return failure(worldPos, fail);
					}
					members.add(worldPos);
					components.add(new Component(worldPos, sample.componentTypeId()));
				}
			}
		}

		// --- Run the extra-cell predicates (slab cap, air ring). ---
		for (Map.Entry<StructurePos, CellPredicate> entry : extra.entrySet()) {
			StructurePos rel = entry.getKey();
			StructurePos worldPos = origin.offset(rel.x(), rel.y(), rel.z());
			String fail = entry.getValue().test(view.sample(worldPos));
			if (fail != null) {
				return failure(worldPos, fail);
			}
		}

		// --- Whole-structure post-checks (e.g. gearbox-present). ---
		for (PostCheck postCheck : this.postChecks) {
			String fail = postCheck.check(components);
			if (fail != null) {
				return failure(origin, fail);
			}
		}

		// --- Match. members are produced in (x,y,z) order, so members.get(0) is the lowest = holder. ---
		Collections.sort(members);
		StructurePos min = members.get(0);
		StructurePos holder = min;
		StructurePos max = origin.offset(sizeX - 1, sizeY - 1, sizeZ - 1);
		return new PatternResult.Match(members, min, max, holder, components);
	}

	/**
	 * Determines the box size at {@code origin}.
	 *
	 * <p>For a <b>fixed axis</b> ({@code min == max}) the size is that fixed value with no measurement —
	 * so an interior hole is caught later by the per-cell predicates as {@code invalid.interior} (exact
	 * parity with the old bounding-box-then-validate-each-cell behaviour). For a <b>ranged axis</b> the
	 * size is found by growing along the min-corner edge while the next edge cell is a same-type
	 * component, clamped to {@code max}; this resolves which size variant a variable-size machine (the
	 * farm) is, and remaining interior cells are still validated by predicates. Returns {@code null} if a
	 * consulted cell is unloaded (defer; loaded-shell rule).
	 */
	private Measure measureBox(StructureView view, StructurePos origin) {
		if (!view.isLoaded(origin)) {
			return null;
		}
		// If origin is not even a same-type component, report 1x1x1 so size/predicate checks emit a key.
		boolean originIsComponent = isSameTypeComponent(view.sample(origin));

		int sizeX = measureAxis(view, origin, 1, 0, 0, this.minSizeX, this.maxSizeX, originIsComponent);
		if (sizeX == UNLOADED) {
			return null;
		}
		int sizeY = measureAxis(view, origin, 0, 1, 0, this.minSizeY, this.maxSizeY, originIsComponent);
		if (sizeY == UNLOADED) {
			return null;
		}
		int sizeZ = measureAxis(view, origin, 0, 0, 1, this.minSizeZ, this.maxSizeZ, originIsComponent);
		if (sizeZ == UNLOADED) {
			return null;
		}
		return new Measure(sizeX, sizeY, sizeZ);
	}

	private static final int UNLOADED = -1;

	/**
	 * Measures one axis. Fixed axes ({@code min == max}) return {@code max} immediately. Ranged axes grow
	 * along the unit-vector edge from origin while the next edge cell is a same-type component, clamped to
	 * {@code max}. Returns {@link #UNLOADED} if a consulted edge cell is unloaded.
	 */
	private int measureAxis(StructureView view, StructurePos origin, int ux, int uy, int uz, int min, int max, boolean originIsComponent) {
		if (min == max) {
			return max; // fixed-size axis: no measurement, predicates validate every cell
		}
		if (!originIsComponent) {
			return 1; // degenerate; size check will fail with a small key
		}
		int size = 1;
		while (size < max) {
			StructurePos next = origin.offset(ux * size, uy * size, uz * size);
			if (!view.isLoaded(next)) {
				return UNLOADED;
			}
			if (!isSameTypeComponent(view.sample(next))) {
				break;
			}
			size++;
		}
		return size;
	}

	/**
	 * Confirms the layer just beyond each grown face (+X/+Y/+Z) is loaded and contains no same-type
	 * component (maximality). measureBox stopped at these layers, but only after checking each layer was
	 * loaded; this re-walks the full face to ensure NO cell of it is a same-type component (a partial
	 * same-type face would mean a larger irregular structure). Returns null on success.
	 */
	private PatternResult.Failure checkUpperMaximality(StructureView view, StructurePos origin, int sizeX, int sizeY, int sizeZ) {
		// +X face at x = sizeX
		for (int dy = 0; dy < sizeY; dy++) {
			for (int dz = 0; dz < sizeZ; dz++) {
				PatternResult.Failure f = confirmShellClear(view, origin.offset(sizeX, dy, dz));
				if (f != null) {
					return f;
				}
			}
		}
		// +Y face at y = sizeY
		for (int dx = 0; dx < sizeX; dx++) {
			for (int dz = 0; dz < sizeZ; dz++) {
				PatternResult.Failure f = confirmShellClear(view, origin.offset(dx, sizeY, dz));
				if (f != null) {
					return f;
				}
			}
		}
		// +Z face at z = sizeZ
		for (int dx = 0; dx < sizeX; dx++) {
			for (int dy = 0; dy < sizeY; dy++) {
				PatternResult.Failure f = confirmShellClear(view, origin.offset(dx, dy, sizeZ));
				if (f != null) {
					return f;
				}
			}
		}
		return null;
	}

	private PatternResult.Failure confirmShellClear(StructureView view, StructurePos pos) {
		if (!view.isLoaded(pos)) {
			return new PatternResult.Failure(List.of(new FailingCell(pos, Predicates.KEY_INVALID_INTERIOR)));
		}
		if (isSameTypeComponent(view.sample(pos))) {
			// the real structure is larger -> this candidate is a non-maximal sub-region
			return new PatternResult.Failure(List.of(new FailingCell(pos, Predicates.KEY_INVALID_PART)));
		}
		return null;
	}

	/** Parity size checks against the configured ranges (matches RectangularMultiblockControllerBase). */
	private PatternResult.Failure checkSize(StructurePos origin, int sizeX, int sizeY, int sizeZ) {
		int blocks = sizeX * sizeY * sizeZ;
		if (blocks < this.minBlocks) {
			return new PatternResult.Failure(List.of(new FailingCell(origin, Predicates.KEY_SMALL)));
		}
		if (this.maxSizeX > 0 && sizeX > this.maxSizeX) {
			return new PatternResult.Failure(List.of(new FailingCell(origin, Predicates.KEY_LARGE_X)));
		}
		if (this.maxSizeY > 0 && sizeY > this.maxSizeY) {
			return new PatternResult.Failure(List.of(new FailingCell(origin, Predicates.KEY_LARGE_Y)));
		}
		if (this.maxSizeZ > 0 && sizeZ > this.maxSizeZ) {
			return new PatternResult.Failure(List.of(new FailingCell(origin, Predicates.KEY_LARGE_Z)));
		}
		if (sizeX < this.minSizeX) {
			return new PatternResult.Failure(List.of(new FailingCell(origin, Predicates.KEY_SMALL_X)));
		}
		if (sizeY < this.minSizeY) {
			return new PatternResult.Failure(List.of(new FailingCell(origin, Predicates.KEY_SMALL_Y)));
		}
		if (sizeZ < this.minSizeZ) {
			return new PatternResult.Failure(List.of(new FailingCell(origin, Predicates.KEY_SMALL_Z)));
		}
		return null;
	}

	/**
	 * The discovery candidate origins for a Forestry block changed/loaded at {@code pos} (spec §5.3):
	 * {@code { pos − cellOffset }} over every cell of every size variant (box cells + extra cells),
	 * deduplicated. Each is then fed to {@link #validate}.
	 */
	public Set<StructurePos> candidateOrigins(StructurePos pos) {
		Set<StructurePos> origins = new LinkedHashSet<>();
		for (int sx = this.minSizeX; sx <= this.maxSizeX; sx++) {
			for (int sy = this.minSizeY; sy <= this.maxSizeY; sy++) {
				for (int sz = this.minSizeZ; sz <= this.maxSizeZ; sz++) {
					// box cell offsets
					for (int dx = 0; dx < sx; dx++) {
						for (int dy = 0; dy < sy; dy++) {
							for (int dz = 0; dz < sz; dz++) {
								origins.add(pos.offset(-dx, -dy, -dz));
							}
						}
					}
					// extra cell offsets
					if (this.extraCellsFactory != null) {
						for (StructurePos rel : this.extraCellsFactory.extraCells(sx, sy, sz).keySet()) {
							origins.add(pos.offset(-rel.x(), -rel.y(), -rel.z()));
						}
					}
				}
			}
		}
		return origins;
	}

	private static PatternResult.Failure failure(StructurePos pos, String key) {
		return new PatternResult.Failure(List.of(new FailingCell(pos, key)));
	}

	private record Measure(int sizeX, int sizeY, int sizeZ) {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String componentTypePrefix;
		private int minSizeX = 1;
		private int maxSizeX = 1;
		private int minSizeY = 1;
		private int maxSizeY = 1;
		private int minSizeZ = 1;
		private int maxSizeZ = 1;
		private int minBlocks = 1;
		private BoxCellPredicate boxCellPredicate;
		private ExtraCellsFactory extraCellsFactory;
		private final List<PostCheck> postChecks = new ArrayList<>();

		public Builder componentTypePrefix(String prefix) {
			this.componentTypePrefix = prefix;
			return this;
		}

		public Builder sizeX(int min, int max) {
			this.minSizeX = min;
			this.maxSizeX = max;
			return this;
		}

		public Builder sizeY(int min, int max) {
			this.minSizeY = min;
			this.maxSizeY = max;
			return this;
		}

		public Builder sizeZ(int min, int max) {
			this.minSizeZ = min;
			this.maxSizeZ = max;
			return this;
		}

		public Builder minBlocks(int minBlocks) {
			this.minBlocks = minBlocks;
			return this;
		}

		public Builder boxCellPredicate(BoxCellPredicate boxCellPredicate) {
			this.boxCellPredicate = boxCellPredicate;
			return this;
		}

		public Builder extraCells(ExtraCellsFactory extraCellsFactory) {
			this.extraCellsFactory = extraCellsFactory;
			return this;
		}

		public Builder postCheck(PostCheck postCheck) {
			this.postChecks.add(postCheck);
			return this;
		}

		public MultiblockPattern build() {
			return new MultiblockPattern(this);
		}
	}
}
