package forestry.core.multiblock.pattern;

/**
 * A pure, Minecraft-free 3D integer position used by the declarative pattern engine.
 *
 * <p>This deliberately does NOT use {@code net.minecraft.core.BlockPos}: keeping the whole pattern
 * layer free of {@code net.minecraft} imports lets the JUnit unit tests run without a Minecraft
 * classpath. The world adapter ({@code LevelStructureView}, Phase 2) is responsible for translating
 * between {@code BlockPos} and {@code StructurePos}.
 *
 * <p>Ordering is lexicographic by {@code (x, y, z)} so the "lowest member" (the holder / reference
 * coord, spec §6.1) is simply the minimum of the member set.
 */
public record StructurePos(int x, int y, int z) implements Comparable<StructurePos> {
	/**
	 * Returns a new position translated by the given deltas.
	 */
	public StructurePos offset(int dx, int dy, int dz) {
		return new StructurePos(this.x + dx, this.y + dy, this.z + dz);
	}

	@Override
	public int compareTo(StructurePos other) {
		int cmp = Integer.compare(this.x, other.x);
		if (cmp != 0) {
			return cmp;
		}
		cmp = Integer.compare(this.y, other.y);
		if (cmp != 0) {
			return cmp;
		}
		return Integer.compare(this.z, other.z);
	}
}
