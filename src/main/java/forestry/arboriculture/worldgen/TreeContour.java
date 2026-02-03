package forestry.arboriculture.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A tree contour holds branch end positions and leaf positions.
 */
public sealed interface TreeContour {
	TreeContour EMPTY = new Empty();

	void addLeaf(BlockPos pos);

	List<BlockPos> getBranchEnds();
	List<BlockPos> getTrunkOrigins();

	boolean hasLeaf(BlockPos pos);

	final class Impl implements TreeContour {
		public final Set<BlockPos> leavePositions;
		public final List<BlockPos> branchEnds;
		public final List<BlockPos> trunkOrigins;

		@Nullable
		public BoundingBox boundingBox;

		public Impl(List<BlockPos> ends, List<BlockPos> origins) {
			this.leavePositions = new HashSet<>();
			this.branchEnds = ends;
			this.trunkOrigins = origins;
			this.boundingBox = null;
		}

		@Override
		public void addLeaf(BlockPos pos) {
			this.leavePositions.add(pos.immutable());

			if (this.boundingBox == null) {
				this.boundingBox = new BoundingBox(pos);
			} else {
				this.boundingBox.encapsulate(pos);
			}
		}

		@Override
		public boolean hasLeaf(BlockPos pos) {
			return this.leavePositions.contains(pos);
		}

		@Override
		public List<BlockPos> getBranchEnds() {
			return this.branchEnds;
		}

		@Override
		public List<BlockPos> getTrunkOrigins() {
			return this.trunkOrigins;
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
		@Override
		public List<BlockPos> getTrunkOrigins() {
			return List.of();
		}
	}
}


