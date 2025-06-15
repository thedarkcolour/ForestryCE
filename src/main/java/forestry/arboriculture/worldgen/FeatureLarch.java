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

public class FeatureLarch extends FeatureTree {

	public FeatureLarch(ITreeSpecies tree) {
		super(tree, 6, 5);
	}

	@Override
	protected void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = this.height + 1;

		float sizeMultiplier = Math.max(this.height / 8, 1.0f);

		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, sizeMultiplier + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		while (leafSpawn > 2) {
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, 2 * sizeMultiplier + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
			FeatureHelper.generateCylinderFromTreeStartPos(level, leaf, startPos.offset(0, leafSpawn--, 0), this.girth, sizeMultiplier + this.girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		}
	}
}
