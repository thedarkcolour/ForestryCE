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

public class FeatureJungle extends FeatureTreeVanilla {
	public FeatureJungle(ITreeSpecies tree) {
		super(tree);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		int height = this.height;
		float vinesChance = 0.0f;
		if (this.girth >= 2) {
			height *= 1.5f;
			vinesChance = 0.8f;
		}

		FeatureHelper.generateTreeTrunk(level, rand, wood, startPos, height, this.girth, 0, vinesChance, null, 0);

		Set<BlockPos> branchCoords = new HashSet<>();
		if (height > 10) {
			int branchSpawn = 6;
			while (branchSpawn < height - 2) {
				branchCoords.addAll(FeatureHelper.generateBranches(level, rand, wood, startPos.offset(0, branchSpawn, 0), this.girth, 0.5f, 0f, 2, 1, 0.25f));
				branchSpawn += rand.nextInt(4);
			}
		}

		return branchCoords;
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int height = this.height;
		if (this.girth >= 2) {
			height *= 1.5f;
		}

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(level, leaf, branchEnd, this.girth, 1, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		int leafSpawn = height + 1;
		float canopyRadiusMultiplier = height / 7.0f;

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 0.5f * canopyRadiusMultiplier + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 1.9f * canopyRadiusMultiplier + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn, 0), this.girth, 1.9f * canopyRadiusMultiplier + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
	}
}
