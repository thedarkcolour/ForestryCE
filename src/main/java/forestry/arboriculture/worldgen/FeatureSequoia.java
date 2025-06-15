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

import java.util.HashSet;
import java.util.Set;

public class FeatureSequoia extends FeatureTree {

	public FeatureSequoia(ITreeSpecies tree) {
		this(tree, 20, 5);
	}

	protected FeatureSequoia(ITreeSpecies tree, int baseHeight, int heightVariation) {
		super(tree, baseHeight, heightVariation);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, this.height, this.girth, 0, 0, null, 0);
		FeatureHelper.generateSupportStems(wood, level, rand, startPos, this.height, this.girth, 0.4f, 0.4f);

		int topHeight = this.height / 3 + rand.nextInt(this.height / 6);

		Set<BlockPos> branchCoords = new HashSet<>();
		for (int yBranch = topHeight; yBranch < this.height; yBranch++) {
			int branchLength = Math.round(this.height - yBranch) / 2;
			if (branchLength > 4) {
				branchLength = 4;
			}
			branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, yBranch, 0), this.girth, 0.05f, 0.25f, branchLength, 1, 0.5f));
		}
		return branchCoords;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, 1.0f + this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = this.height + 2;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1 + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1 + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		int topHeight = this.height / 3 + rand.nextInt(this.height / 6);
		while (leafSpawn > topHeight) {
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1 + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
	}
}
