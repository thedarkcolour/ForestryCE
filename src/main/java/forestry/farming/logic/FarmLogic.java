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
package forestry.farming.logic;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmLogic;
import forestry.api.farming.IFarmType;
import forestry.api.farming.IFarmable;
import forestry.api.farming.Soil;
import forestry.core.utils.VecUtil;

public abstract class FarmLogic implements IFarmLogic {
	private final EntitySelectorFarm entitySelectorFarm;
	protected final IFarmType type;
	protected final boolean isManual;

	public FarmLogic(IFarmType type, boolean isManual) {
		this.type = type;
		this.isManual = isManual;
		this.entitySelectorFarm = new EntitySelectorFarm(type);
	}

	protected Collection<IFarmable> getFarmables() {
		return this.type.getFarmables();
	}

	protected Collection<Soil> getSoils() {
		return this.type.getSoils();
	}

	@Override
	public IFarmType getType() {
		return this.type;
	}

	@Override
	public boolean isManual() {
		return isManual;
	}

	@Override
	public Collection<ICrop> harvest(Level level, IFarmHousing housing, Direction direction, int extent, BlockPos pos) {
		ArrayDeque<ICrop> crops = new ArrayDeque<>();
		for (int i = 0; i < extent; i++) {
			BlockPos position = translateWithOffset(pos.above(), direction, i);
			ICrop crop = getCrop(level, position);
			if (crop != null) {
				crops.addFirst(crop);
			}
		}
		return crops;
	}

	@Nullable
	protected ICrop getCrop(Level world, BlockPos position) {
		if (!world.hasChunkAt(position) || world.isEmptyBlock(position)) {
			return null;
		}
		BlockState blockState = world.getBlockState(position);
		for (IFarmable seed : getFarmables()) {
			ICrop crop = seed.getCropAt(world, position, blockState);
			if (crop != null) {
				return crop;
			}
		}
		return null;
	}

	protected final boolean isWaterSourceBlock(Level world, BlockPos position) {
		if (!world.hasChunkAt(position)) {
			return false;
		}
		BlockState blockState = world.getBlockState(position);
		Block block = blockState.getBlock();
		return block == Blocks.WATER;
	}

	protected final boolean isIceBlock(Level world, BlockPos position) {
		if (!world.hasChunkAt(position)) {
			return false;
		}
		BlockState blockState = world.getBlockState(position);
		Block block = blockState.getBlock();
		return block == Blocks.ICE;
	}

	protected final BlockPos translateWithOffset(BlockPos pos, Direction farmDirection, int step) {
		return VecUtil.scale(farmDirection.getNormal(), step).offset(pos);
	}

	private static AABB getHarvestBox(Level world, IFarmHousing farmHousing, boolean toWorldHeight) {
		BlockPos coords = farmHousing.getCoords();
		Vec3i area = farmHousing.getArea();
		Vec3i offset = farmHousing.getOffset();

		BlockPos min = coords.offset(offset);
		BlockPos max = min.offset(area);

		int maxY = max.getY();
		if (toWorldHeight) {
			maxY = world.getMaxBuildHeight();
		}

		return new AABB(min.getX(), min.getY(), min.getZ(), max.getX(), maxY, max.getZ());
	}

	protected List<ItemStack> collectEntityItems(Level world, IFarmHousing farmHousing, boolean toWorldHeight) {
		AABB harvestBox = getHarvestBox(world, farmHousing, toWorldHeight);

		List<ItemEntity> entityItems = world.getEntitiesOfClass(ItemEntity.class, harvestBox, entitySelectorFarm);
		ArrayList<ItemStack> stacks = new ArrayList<>();
		for (ItemEntity entity : entityItems) {
			ItemStack contained = entity.getItem();
			stacks.add(contained.copy());
			entity.remove(Entity.RemovalReason.DISCARDED);
		}
		return stacks;
	}

	// for debugging
	@Override
	public String toString() {
		return type.getTranslationKey();
	}

	private static class EntitySelectorFarm implements Predicate<ItemEntity> {
		// From immersiveengineering.api.Lib.MAGNET_PREVENT_NBT
		private static final String MAGNET_PREVENT_NBT = "PreventRemoteMovement";

		private final IFarmType properties;

		public EntitySelectorFarm(IFarmType properties) {
			this.properties = properties;
		}

		@Override
		public boolean apply(@Nullable ItemEntity entity) {
			if (entity == null || !entity.isAlive()) {
				return false;
			}

			// Fixes grabbing items from Immersive Engineering belts.
			if (entity.getPersistentData().getBoolean(MAGNET_PREVENT_NBT)) {
				return false;
			}

			ItemStack contained = entity.getItem();
			return this.properties.isAcceptedSeedling(contained) || this.properties.isAcceptedWindfall(contained);
		}
	}
}
