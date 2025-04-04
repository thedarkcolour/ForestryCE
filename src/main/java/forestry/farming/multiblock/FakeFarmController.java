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
package forestry.farming.multiblock;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraftforge.fluids.FluidStack;

import forestry.api.farming.IFarmLogic;
import forestry.api.farming.IFarmable;
import forestry.core.fluids.FakeTankManager;
import forestry.core.fluids.ITankManager;
import forestry.core.inventory.FakeInventoryAdapter;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.multiblock.FakeMultiblockController;
import forestry.farming.FarmTarget;
import forestry.farming.gui.IFarmLedgerDelegate;

public enum FakeFarmController implements FakeMultiblockController, IFarmControllerInternal {
	INSTANCE;

	@Override
	public BlockPos getCoords() {
		return BlockPos.ZERO;
	}

	@Override
	public Vec3i getArea() {
		return Vec3i.ZERO;
	}

	@Override
	public Vec3i getOffset() {
		return Vec3i.ZERO;
	}

	@Override
	public boolean doWork() {
		return false;
	}

	@Override
	public boolean hasLiquid(FluidStack liquid) {
		return false;
	}

	@Override
	public void removeLiquid(FluidStack liquid) {
	}

	@Override
	public boolean plantGermling(IFarmable farmable, Level world, BlockPos pos, Direction direction) {
		return false;
	}

	@Override
	public IFarmInventoryInternal getFarmInventory() {
		return FakeFarmInventory.INSTANCE;
	}

	@Override
	public void setUpFarmlandTargets(Map<Direction, List<FarmTarget>> targets) {
	}

	@Override
	public BlockPos getTopCoord() {
		return BlockPos.ZERO;
	}

	@Override
	public BlockPos getCoordinates() {
		return BlockPos.ZERO;
	}

	@Override
	public void addPendingProduct(ItemStack stack) {
	}

	@Override
	public void setFarmLogic(Direction direction, IFarmLogic logic) {
	}

	@Override
	public IFarmLogic getFarmLogic(Direction direction) {
		throw new IllegalStateException();
	}

	@Override
	public Collection<IFarmLogic> getFarmLogics() {
		return List.of();
	}

	@Override
	public void resetFarmLogic(Direction direction) {
	}

	@Override
	public int getStoredFertilizerScaled(int scale) {
		return 0;
	}

	@Override
	public BlockPos getFarmCorner(Direction direction) {
		return null;
	}

	@Override
	public int getSocketCount() {
		return 0;
	}

	@Override
	public ItemStack getSocket(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setSocket(int slot, ItemStack stack) {
	}

	@Override
	public ResourceLocation getSocketType() {
		return null;
	}

	@Override
	public IFarmLedgerDelegate getFarmLedgerDelegate() {
		throw new IllegalStateException("Invalid farm");
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		return FakeInventoryAdapter.INSTANCE;
	}

	@Override
	public ITankManager getTankManager() {
		return FakeTankManager.instance;
	}

	@Nullable
	@Override
	public BlockPos getDestroyedCoord() {
		return null;
	}

	@Override
	public String getUnlocalizedType() {
		return "for.multiblock.farm.type";
	}

	@Override
	public boolean isValidPlatform(Level world, BlockPos pos) {
		return false;
	}

	@Override
	public int getExtents(Direction direction, BlockPos pos) {
		return 0;
	}

	@Override
	public void setExtents(Direction direction, BlockPos pos, int extend) {
	}

	@Override
	public void cleanExtents(Direction direction) {
	}

	private enum FakeFarmInventory implements IFarmInventoryInternal {
		INSTANCE;

		@Override
		public boolean hasResources(List<ItemStack> resources) {
			return false;
		}

		@Override
		public void removeResources(List<ItemStack> resources) {
		}

		@Override
		public boolean acceptsAsSeedling(ItemStack stack) {
			return false;
		}

		@Override
		public boolean acceptsAsResource(ItemStack stack) {
			return false;
		}

		@Override
		public boolean acceptsAsFertilizer(ItemStack stack) {
			return false;
		}

		@Override
		public Container getProductInventory() {
			return FakeInventoryAdapter.INSTANCE;
		}

		@Override
		public Container getGermlingsInventory() {
			return FakeInventoryAdapter.INSTANCE;
		}

		@Override
		public Container getResourcesInventory() {
			return FakeInventoryAdapter.INSTANCE;
		}

		@Override
		public Container getFertilizerInventory() {
			return FakeInventoryAdapter.INSTANCE;
		}

		@Override
		public int getFertilizerValue() {
			return 0;
		}

		@Override
		public boolean useFertilizer() {
			return false;
		}

		@Override
		public void stowProducts(Iterable<ItemStack> harvested, ArrayDeque<ItemStack> pendingProduce) {
		}

		@Override
		public boolean tryAddPendingProduce(ArrayDeque<ItemStack> pendingProduce) {
			return false;
		}
	}
}
