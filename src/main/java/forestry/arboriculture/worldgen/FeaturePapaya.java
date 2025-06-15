/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.genetics.ITreeSpecies;
import forestry.core.worldgen.FeatureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import java.util.Set;

public class FeaturePapaya extends FeatureTree {

	public FeaturePapaya(ITreeSpecies tree) {
		super(tree, 7, 2);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);
		return FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, this.height - 1, 0), this.girth, 0.15f, 0.25f, this.height / 4, 1, 0.25f);
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 1 + this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int yCenter = this.height - this.girth;
		yCenter = yCenter > 3 ? yCenter : 4;
		FeatureHelper.generateSphereFromTreeStartPos(level, startPos.offset(0, yCenter, 0), this.girth, Math.round((2 + rand.nextInt(this.girth)) * (this.height / 8.0f)), leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
	}
}
