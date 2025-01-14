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
package forestry.core.utils;

import com.google.common.collect.AbstractIterator;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Iterator;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public final class VecUtil {
	/**
	 * Sorts positions by height in descending order
	 */
	public static final Comparator<BlockPos> TOP_DOWN_COMPARATOR = (a, b) -> Integer.compare(b.getY(), a.getY());

	public static BlockPos getRandomPositionInArea(RandomSource random, Vec3i area) {
		int x = random.nextInt(area.getX() + 1);
		int y = random.nextInt(area.getY() + 1);
		int z = random.nextInt(area.getZ() + 1);
		return new BlockPos(x, y, z);
	}

	public static BlockPos sum(Vec3i... vectors) {
		int x = 0;
		int y = 0;
		int z = 0;
		for (Vec3i vec : vectors) {
			x += vec.getX();
			y += vec.getY();
			z += vec.getZ();
		}
		return new BlockPos(x, y, z);
	}

	// todo look into usages and replace with adjusted territory respecting IBeeModifiers
	public static BlockPos scale(Vec3i vec, float factor) {
		return BlockPos.containing(vec.getX() * factor, vec.getY() * factor, vec.getZ() * factor);
	}

	public static Direction direction(Vec3i a, Vec3i b) {
		int x = Math.abs(a.getX() - b.getX());
		int y = Math.abs(a.getY() - b.getY());
		int z = Math.abs(a.getZ() - b.getZ());
		int max = Math.max(x, Math.max(y, z));
		if (max == x) {
			return Direction.EAST;
		} else if (max == z) {
			return Direction.SOUTH;
		} else {
			return Direction.UP;
		}
	}

	public static Iterator<BlockPos.MutableBlockPos> getAllInBoxFromCenterMutable(Level level, final BlockPos from, final BlockPos center, final BlockPos to) {
		int minX = Math.min(from.getX(), to.getX());
		int minY = Math.min(from.getY(), to.getY());
		int minZ = Math.min(from.getZ(), to.getZ());
		int maxX = Math.max(from.getX(), to.getX());
		int maxY = Math.max(from.getY(), to.getY());
		int maxZ = Math.max(from.getZ(), to.getZ());

		return new MutableBlockPosSpiralIterator(level, center, minX, minY, minZ, maxX, maxY, maxZ);
	}

	private static class MutableBlockPosSpiralIterator extends AbstractIterator<BlockPos.MutableBlockPos> {
		private final Level level;
		private final BlockPos center;
		private final int minX;
		private final int minY;
		private final int minZ;
		private final int maxX;
		private final int maxY;
		private final int maxZ;
		private int spiralLayer;
		private final int maxSpiralLayers;
		private int direction;
		private final int minWorldHeight;

		@Nullable
		private BlockPos.MutableBlockPos theBlockPos;

		public MutableBlockPosSpiralIterator(Level level, BlockPos center, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
			this.level = level;
			this.center = center;
			this.minX = minX;
			this.minY = minY;
			this.minZ = minZ;
			this.maxX = maxX;
			this.maxY = maxY;
			this.maxZ = maxZ;

			int xDiameter = maxX - minX;
			int zDiameter = maxZ - minZ;
			this.maxSpiralLayers = Math.max(xDiameter, zDiameter) / 2;
			this.spiralLayer = 1;
			this.minWorldHeight = level.getMinBuildHeight();
		}

		@Override
		@Nullable
		protected BlockPos.MutableBlockPos computeNext() {
			BlockPos.MutableBlockPos pos;

			do {
				pos = nextPos();
			}
			while (pos != null && (pos.getX() > this.maxX || pos.getY() > this.maxY || pos.getZ() > this.maxZ || pos.getX() < this.minX || pos.getY() < this.minY || pos.getZ() < this.minZ));

			return pos;
		}

		@Nullable
		protected BlockPos.MutableBlockPos nextPos() {
			if (this.theBlockPos == null) {
				this.theBlockPos = new BlockPos.MutableBlockPos(this.center.getX(), this.maxY, this.center.getZ());
				// in 1.12, this returned heightmap value. in 1.13+ it returns heightmap value + 1, so we have to subtract 1.
				int y = Math.min(this.maxY, this.level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.theBlockPos.getX(), this.theBlockPos.getZ()) - 1);
				this.theBlockPos.setY(y);
				return this.theBlockPos;
			} else if (spiralLayer > maxSpiralLayers) {
				return this.endOfData();
			} else {
				int x = this.theBlockPos.getX();
				int y = this.theBlockPos.getY();
				int z = this.theBlockPos.getZ();

				if (y > minY && y > this.minWorldHeight) {
					y--;
				} else {
					switch (direction) {
						case 0 -> {
							++x;
							if (x == center.getX() + spiralLayer) {
								++direction;
							}
						}
						case 1 -> {
							++z;
							if (z == center.getZ() + spiralLayer) {
								++direction;
							}
						}
						case 2 -> {
							--x;
							if (x == center.getX() - spiralLayer) {
								++direction;
							}
						}
						case 3 -> {
							--z;
							if (z == center.getZ() - spiralLayer) {
								direction = 0;
								++spiralLayer;
							}
						}
					}

					this.theBlockPos.set(x, y, z);
					// 1.13 adds a +1 to the heightmap value, so we have to counter it with a -1.
					y = Math.min(this.maxY, this.level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1);
				}

				return this.theBlockPos.set(x, y, z);
			}
		}
	}
}
