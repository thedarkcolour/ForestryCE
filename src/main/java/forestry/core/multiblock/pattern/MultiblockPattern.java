package forestry.core.multiblock.pattern;

import java.util.ArrayList;
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
		// those confirming cells to be loaded (loaded-shell). Both branches mean "this candidate is not
		// rooted at the structure's true min corner", a discovery artefact of probing many permissive
		// candidate origins (spec §5.3) — NOT a real content/size error. They return the internal
		// KEY_NOT_MAXIMAL so findValidationHint ranks them below the meaningful failure produced by the
		// candidate that IS rooted at the true min corner (e.g. error.small / invalid.interior), instead
		// of leaking the misleading "incompatible part (%s)" message for an incomplete machine. ---
		for (StructurePos below : new StructurePos[]{
				origin.offset(-1, 0, 0), origin.offset(0, -1, 0), origin.offset(0, 0, -1)}) {
			if (!view.isLoaded(below)) {
				return failure(below, Predicates.KEY_NOT_MAXIMAL);
			}
			if (isSameTypeComponent(view.sample(below))) {
				return failure(below, Predicates.KEY_NOT_MAXIMAL);
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

		// --- Size-range check (parity small/large keys). For a fixed-size axis measureBox does NOT measure
		// the real extent (it returns the fixed value so an interior hole is caught later as
		// invalid.interior, exact parity). That hides an UNDERSIZED structure (e.g. a 3x3x2 alveary): the
		// per-cell loop would report the missing top layer as invalid.interior, when the old engine showed
		// "must be 3x3x3" (error.small). So we measure the true contiguous same-type extent from origin and
		// hand it to checkSize, which compares it against the configured minimums and surfaces error.small /
		// error.small.{x,y,z}. A hole that does NOT shrink the outer extent (full shell, missing interior)
		// leaves the extent at full size, so it still falls through to the per-cell invalid.interior path. ---
		Measure extent = measureExtent(view, origin);
		PatternResult.Failure sizeFailure = checkSize(origin, sizeX, sizeY, sizeZ, extent);
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

		// --- Match. The box is built in ascending (x,y,z) order from origin, so origin is by construction
		// the lowest member = min = holder (spec §6.1); max is the opposite corner of the solid box. ---
		StructurePos max = origin.offset(sizeX - 1, sizeY - 1, sizeZ - 1);
		return new PatternResult.Match(members, origin, max, origin, components);
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
	 * Measures the true contiguous same-type extent of the box from {@code origin} along +X/+Y/+Z, used
	 * <em>only</em> for the undersized-structure ({@code error.small}) check — it is NOT used to build the
	 * member set. Unlike {@link #measureAxis} it measures every axis (including fixed-size ones, whose
	 * {@code measureAxis} short-circuits to the fixed value), so a genuinely undersized fixed-size machine
	 * (a 3x3x2 alveary) reports its real extent.
	 *
	 * <p>To avoid mistaking a single missing <em>edge</em> cell (an interior/edge HOLE in an otherwise
	 * full-extent box) for an undersized structure, the reach along an axis is the <b>maximum</b> run over
	 * all column start cells in the {@code minA x minB} base patch of the perpendicular plane through
	 * origin: a one-cell hole on one column leaves a parallel column full, so the max stays at full size and
	 * the failure falls through to the per-cell {@code invalid.interior} path (parity). Only when an
	 * <em>entire</em> layer is absent (every parallel column is short) does the max drop below the minimum
	 * and surface {@code error.small}. The patch is bounded by the per-axis minimums (3 for the alveary), so
	 * this is a handful of cheap reads. If origin is not itself a same-type component the extent is 0.
	 */
	private Measure measureExtent(StructureView view, StructurePos origin) {
		if (!view.isLoaded(origin) || !isSameTypeComponent(view.sample(origin))) {
			return new Measure(0, 0, 0);
		}
		// Unit vectors: X=(1,0,0) Y=(0,1,0) Z=(0,0,1). For each measured axis, pass the two perpendiculars
		// and their per-axis minimums so the base patch spans the expected footprint of the perpendicular plane.
		StructurePos x = new StructurePos(1, 0, 0);
		StructurePos y = new StructurePos(0, 1, 0);
		StructurePos z = new StructurePos(0, 0, 1);
		return new Measure(
				measureAxisExtent(view, origin, x, y, this.minSizeY, z, this.minSizeZ),
				measureAxisExtent(view, origin, y, x, this.minSizeX, z, this.minSizeZ),
				measureAxisExtent(view, origin, z, x, this.minSizeX, y, this.minSizeY));
	}

	/**
	 * The maximum contiguous same-type run along {@code u}, taken over every column whose start cell lies in
	 * the {@code minA x minB} base patch spanned by the two perpendicular unit vectors {@code pa}/{@code pb}
	 * through origin. Taking the max makes a single-cell hole on one column irrelevant (a parallel column is
	 * still full-length), so only a wholly-missing layer shrinks the measured extent below the minimum.
	 */
	private int measureAxisExtent(StructureView view, StructurePos origin, StructurePos u,
			StructurePos pa, int minA, StructurePos pb, int minB) {
		int best = 0;
		for (int a = 0; a < minA; a++) {
			for (int b = 0; b < minB; b++) {
				StructurePos start = origin.offset(pa.x() * a + pb.x() * b, pa.y() * a + pb.y() * b, pa.z() * a + pb.z() * b);
				if (!view.isLoaded(start) || !isSameTypeComponent(view.sample(start))) {
					continue;
				}
				int run = 1;
				while (true) {
					StructurePos next = start.offset(u.x() * run, u.y() * run, u.z() * run);
					if (!view.isLoaded(next) || !isSameTypeComponent(view.sample(next))) {
						break;
					}
					run++;
				}
				if (run > best) {
					best = run;
				}
			}
		}
		return best;
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

	/**
	 * Parity size checks against the configured ranges (matches RectangularMultiblockControllerBase) with
	 * the message format args filled in (spec Task A.3). {@code box} is the box size measureBox resolved
	 * (fixed value on fixed axes); {@code extent} is the true contiguous same-type extent from origin
	 * ({@link #measureExtent}). The aggregate count and the large checks use the box size (parity); the
	 * per-axis small checks use the real extent so an UNDERSIZED fixed-size machine (a 3x3x2 alveary) is
	 * reported as error.small / error.small.{x,y,z} rather than falling through to an invalid.interior on
	 * its missing top layer. The args are: error.small → (minX,minY,minZ); error.small.{x,y,z} → that
	 * minimum dimension; error.large.{x,y,z} → that maximum dimension (mirrors the old engine's args).
	 */
	private PatternResult.Failure checkSize(StructurePos origin, int boxX, int boxY, int boxZ, Measure extent) {
		// Aggregate block-count too small -> error.small (minX, minY, minZ). Use the real extent's volume so
		// a short blob (3x3x2 = 18 < 27) is caught here; a full-extent box with an interior hole keeps full
		// volume and falls through to the per-cell invalid.interior path (exact parity).
		int blocks = extent.sizeX * extent.sizeY * extent.sizeZ;
		if (blocks < this.minBlocks) {
			return failure(origin, Predicates.KEY_SMALL, this.minSizeX, this.minSizeY, this.minSizeZ);
		}
		if (this.maxSizeX > 0 && boxX > this.maxSizeX) {
			return failure(origin, Predicates.KEY_LARGE_X, this.maxSizeX);
		}
		if (this.maxSizeY > 0 && boxY > this.maxSizeY) {
			return failure(origin, Predicates.KEY_LARGE_Y, this.maxSizeY);
		}
		if (this.maxSizeZ > 0 && boxZ > this.maxSizeZ) {
			return failure(origin, Predicates.KEY_LARGE_Z, this.maxSizeZ);
		}
		if (extent.sizeX < this.minSizeX) {
			return failure(origin, Predicates.KEY_SMALL_X, this.minSizeX);
		}
		if (extent.sizeY < this.minSizeY) {
			return failure(origin, Predicates.KEY_SMALL_Y, this.minSizeY);
		}
		if (extent.sizeZ < this.minSizeZ) {
			return failure(origin, Predicates.KEY_SMALL_Z, this.minSizeZ);
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

	/** Failure carrying integer message format args (size keys, spec Task A.3). */
	private static PatternResult.Failure failure(StructurePos pos, String key, int... args) {
		return new PatternResult.Failure(List.of(new FailingCell(pos, key, args)));
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
