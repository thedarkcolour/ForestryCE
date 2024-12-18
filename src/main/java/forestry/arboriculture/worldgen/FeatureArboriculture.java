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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import forestry.arboriculture.blocks.BlockSapling;
import forestry.core.utils.VecUtil;
import forestry.core.worldgen.FeatureBase;

/**
 * Base logic for tree generation. Tree generation generally follows these steps:
 * <ol>
 *     <li>Calculate the girth and height based on species, genome, and random variation.</li>
 *     <li>Check if there are enough saplings to grow the tree and if the trunk has enough room to grow.</li>
 *     <li>Remove the saplings.</li>
 *     <li>Generate the trunk and branches. Keep track of the "branch end" positions for leaf placement.</li>
 *     <li>Generate leaves at the branch positions.</li>
 *     <li>If the tree has pod fruit, generate fruit pods as well.</li>
 *     <li>Update distance states for all leaves after generation.</li>
 *     <li>Calls updateShape on all leaf blocks, which handles decay and waterlogged behavior.</li>
 * </ol>
 */
public abstract class FeatureArboriculture extends FeatureBase {
	protected static final int minPodHeight = 3;

	protected final ITreeGenData tree;

	protected FeatureArboriculture(ITreeGenData tree) {
		this.tree = tree;
	}

	@Override
	public IGenome getDefaultGenome() {
		return this.tree.getDefaultGenome();
	}

	@Override
	public boolean place(IGenome genome, LevelAccessor level, RandomSource rand, BlockPos pos, boolean forced) {
		TreeBlockTypeLeaf leaf = new TreeBlockTypeLeaf(this.tree, genome);
		TreeBlockTypeLog wood = new TreeBlockTypeLog(this.tree, genome);

		// Calculate height and girth
		preGenerate(genome, level, rand, pos);

		// Determine valid growth position if any, or skip all checks if forced is true
		BlockPos genPos;
		if (forced) {
			genPos = pos;
		} else {
			// Default implementation is in TreeGrowthHelper.getGrowthPos, but can be overridden in TreeSpecies
			genPos = getValidGrowthPos(level, pos);
		}

		// If a valid growth position was found
		if (genPos != null) {
			// Remove all saplings
			clearSaplings(level, genPos);

			// Generate a trunk and a list of branch end positions. Store those branch ends in a contour
			ArrayList<BlockPos> branchEnds = new ArrayList<>(generateTrunk(level, rand, wood, genPos));
			branchEnds.sort(VecUtil.TOP_DOWN_COMPARATOR);
			TreeContour.Impl contour = new TreeContour.Impl(branchEnds);

			// Generate leaves and pods
			generateLeaves(level, rand, leaf, contour, genPos);
			generateExtras(level, rand, genPos);

			// Correctly update the leaf distance states on the leaf blocks
			DiscreteVoxelShape voxelshapepart = updateLeaves(level, contour);
			// Call updateShape method on all blocks on the edge of the tree's bounding box
			StructureTemplate.updateShapeAtEdge(level, 3, voxelshapepart, contour.boundingBox.minX(), contour.boundingBox.minY(), contour.boundingBox.minZ());
			return true;
		}

		return false;
	}

	/**
	 * Used by {@link FeatureTree} to set fields such as height and girth before generating anything in the world.
	 */
	public void preGenerate(IGenome genome, LevelAccessor level, RandomSource rand, BlockPos startPos) {
	}

	/**
	 * Copied vanilla logic from TreeFeature#updateLeaves
	 */
	private DiscreteVoxelShape updateLeaves(LevelAccessor level, TreeContour.Impl contour) {
		BoundingBox boundingBox = contour.boundingBox;
		List<Set<BlockPos>> list = Lists.newArrayList();
		DiscreteVoxelShape voxelshapepart = new BitSetDiscreteVoxelShape(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());
		int i = 6;

		for (int j = 0; j < 6; ++j) {
			list.add(Sets.newHashSet());
		}

		BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

		for (BlockPos blockpos1 : Lists.newArrayList(contour.leavePositions)) {
			if (boundingBox.isInside(blockpos1)) {
				voxelshapepart.fill(blockpos1.getX() - boundingBox.minX(), blockpos1.getY() - boundingBox.minY(), blockpos1.getZ() - boundingBox.minZ());
			}

			for (Direction direction : Direction.values()) {
				blockpos$mutable.setWithOffset(blockpos1, direction);
				if (!contour.leavePositions.contains(blockpos$mutable)) {
					BlockState blockstate = level.getBlockState(blockpos$mutable);
					if (blockstate.hasProperty(BlockStateProperties.DISTANCE)) {
						list.get(0).add(blockpos$mutable.immutable());
						if (boundingBox.isInside(blockpos$mutable)) {
							voxelshapepart.fill(blockpos$mutable.getX() - boundingBox.minX(), blockpos$mutable.getY() - boundingBox.minY(), blockpos$mutable.getZ() - boundingBox.minZ());
						}
					}
				}
			}
		}

		for (int l = 1; l < 6; ++l) {
			Set<BlockPos> set = list.get(l - 1);
			Set<BlockPos> set1 = list.get(l);

			for (BlockPos blockpos2 : set) {
				if (boundingBox.isInside(blockpos2)) {
					voxelshapepart.fill(blockpos2.getX() - boundingBox.minX(), blockpos2.getY() - boundingBox.minY(), blockpos2.getZ() - boundingBox.minZ());
				}

				for (Direction direction1 : Direction.values()) {
					blockpos$mutable.setWithOffset(blockpos2, direction1);
					if (!set.contains(blockpos$mutable) && !set1.contains(blockpos$mutable)) {
						BlockState blockstate1 = level.getBlockState(blockpos$mutable);
						if (blockstate1.hasProperty(BlockStateProperties.DISTANCE)) {
							int k = blockstate1.getValue(BlockStateProperties.DISTANCE);
							if (k > l + 1) {
								BlockState blockstate2 = blockstate1.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(l + 1));
								setBlockKnownShape(level, blockpos$mutable, blockstate2);
								if (boundingBox.isInside(blockpos$mutable)) {
									voxelshapepart.fill(blockpos$mutable.getX() - boundingBox.minX(), blockpos$mutable.getY() - boundingBox.minY(), blockpos$mutable.getZ() - boundingBox.minZ());
								}

								set1.add(blockpos$mutable.immutable());
							}
						}
					}
				}
			}
		}

		return voxelshapepart;
	}

	private static void setBlockKnownShape(LevelWriter level, BlockPos pos, BlockState state) {
		level.setBlock(pos, state, BlockSapling.UPDATE_NEIGHBORS | BlockSapling.UPDATE_CLIENTS | BlockSapling.UPDATE_KNOWN_SHAPE);
	}

	/**
	 * Generate the tree's trunk. Returns a list of positions of branch ends for leaves to generate at.
	 */
	protected abstract Set<BlockPos> generateTrunk(LevelAccessor level, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos);

	protected abstract void generateLeaves(LevelAccessor level, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos);

	protected abstract void generateExtras(LevelAccessor level, RandomSource rand, BlockPos startPos);

	@Nullable
	public abstract BlockPos getValidGrowthPos(LevelAccessor level, BlockPos pos);

	/**
	 * Removes all saplings before generating the trunk.
	 */
	public void clearSaplings(LevelAccessor level, BlockPos genPos) {
		int treeGirth = this.tree.getGirth(this.tree.getDefaultGenome());
		for (int x = 0; x < treeGirth; x++) {
			for (int z = 0; z < treeGirth; z++) {
				BlockPos saplingPos = genPos.offset(x, 0, z);
				if (level.getBlockState(saplingPos).getBlock() instanceof BlockSapling) {
					level.setBlock(saplingPos, Blocks.AIR.defaultBlockState(), 18);
				}
			}
		}
	}

	public boolean hasPods() {
		return this.tree.allowsFruitBlocks(this.tree.getDefaultGenome());
	}
}
