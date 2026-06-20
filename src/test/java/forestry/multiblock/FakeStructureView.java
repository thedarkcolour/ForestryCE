package forestry.multiblock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import forestry.core.multiblock.pattern.StructurePos;
import forestry.core.multiblock.pattern.StructureView;

/**
 * MC-free in-memory {@link StructureView} for unit tests.
 *
 * <p>Cells default to air (a non-component, non-slab, non-solid-render {@link CellSample}) unless
 * explicitly populated. Positions are loaded unless explicitly marked unloaded. Build with the inner
 * {@link Builder}.
 */
public final class FakeStructureView implements StructureView {
	/** Default sample for any position not explicitly set: air. */
	private static final CellSample AIR = new CellSample(false, null, false, false, null);

	private final Map<StructurePos, CellSample> cells;
	private final Set<StructurePos> unloaded;

	private FakeStructureView(Map<StructurePos, CellSample> cells, Set<StructurePos> unloaded) {
		this.cells = cells;
		this.unloaded = unloaded;
	}

	@Override
	public CellSample sample(StructurePos pos) {
		return this.cells.getOrDefault(pos, AIR);
	}

	@Override
	public boolean isLoaded(StructurePos pos) {
		return !this.unloaded.contains(pos);
	}

	public static Builder builder() {
		return new Builder();
	}

	/** Convenience factory for a Forestry component cell with the given type id. */
	public static CellSample component(String typeId) {
		return new CellSample(true, new Object(), false, false, typeId);
	}

	/** Convenience factory for a wooden slab cell. */
	public static CellSample slab() {
		return new CellSample(false, null, true, false, null);
	}

	/** Convenience factory for a solid-render, non-component cell (e.g. blocking the air ring). */
	public static CellSample solid() {
		return new CellSample(false, null, false, true, null);
	}

	/** Convenience factory for an air cell (non-component, non-slab, non-solid). */
	public static CellSample air() {
		return AIR;
	}

	public static final class Builder {
		private final Map<StructurePos, CellSample> cells = new HashMap<>();
		private final Set<StructurePos> unloaded = new HashSet<>();

		public Builder set(StructurePos pos, CellSample sample) {
			this.cells.put(pos, sample);
			return this;
		}

		public Builder set(int x, int y, int z, CellSample sample) {
			return set(new StructurePos(x, y, z), sample);
		}

		/** Place a Forestry component of the given type id at the position. */
		public Builder component(int x, int y, int z, String typeId) {
			return set(new StructurePos(x, y, z), FakeStructureView.component(typeId));
		}

		/** Place a wooden slab at the position. */
		public Builder slab(int x, int y, int z) {
			return set(new StructurePos(x, y, z), FakeStructureView.slab());
		}

		/** Place a solid-render block at the position. */
		public Builder solid(int x, int y, int z) {
			return set(new StructurePos(x, y, z), FakeStructureView.solid());
		}

		/** Mark a position's chunk as unloaded. */
		public Builder unloaded(int x, int y, int z) {
			this.unloaded.add(new StructurePos(x, y, z));
			return this;
		}

		public Builder unloaded(StructurePos pos) {
			this.unloaded.add(pos);
			return this;
		}

		public FakeStructureView build() {
			return new FakeStructureView(new HashMap<>(this.cells), new HashSet<>(this.unloaded));
		}
	}
}
