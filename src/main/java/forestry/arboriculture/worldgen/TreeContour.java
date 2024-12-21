package forestry.arboriculture.worldgen;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * A tree contour holds branch end positions and leaf positions.
 */
public sealed interface TreeContour {
	TreeContour EMPTY = new Empty();

	void addLeaf(BlockPos pos);

	List<BlockPos> getBranchEnds();

	boolean hasLeaf(BlockPos pos);

	final class Impl implements TreeContour {
		public final Set<BlockPos> leavePositions;
		public final List<BlockPos> branchEnds;

		@Nullable
		public BoundingBox boundingBox;

		public Impl(List<BlockPos> branchEnds) {
			this.leavePositions = new HashSet<>();
			this.branchEnds = branchEnds;
			this.boundingBox = null;
		}

		@Override
		public void addLeaf(BlockPos pos) {
			leavePositions.add(pos.immutable());

			if (boundingBox == null) {
				boundingBox = new BoundingBox(pos);
			} else {
				boundingBox.encapsulate(pos);
			}
		}

		@Override
		public boolean hasLeaf(BlockPos pos) {
			return this.leavePositions.contains(pos);
		}

		@Override
		public List<BlockPos> getBranchEnds() {
			return branchEnds;
		}
	}

	final class Empty implements TreeContour {
		private Empty() {
		}

		@Override
		public void addLeaf(BlockPos pos) {
		}

		@Override
		public boolean hasLeaf(BlockPos pos) {
			return false;
		}

		@Override
		public List<BlockPos> getBranchEnds() {
			return List.of();
		}
	}
}


