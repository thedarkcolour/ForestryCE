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
package forestry.factory.tiles;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.api.recipes.IFabricatorRecipe;
import forestry.api.recipes.IFabricatorSmeltingRecipe;
import forestry.core.fluids.FilteredTank;
import forestry.core.fluids.FluidRecipeFilter;
import forestry.core.fluids.TankManager;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.inventory.InventoryAdapter;
import forestry.core.inventory.InventoryAdapterTile;
import forestry.core.inventory.InventoryGhostCrafting;
import forestry.core.inventory.watchers.ISlotPickupWatcher;
import forestry.core.inventory.wrappers.InventoryMapper;
import forestry.core.items.definitions.ICraftingPlan;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.tiles.TilePowered;
import forestry.core.utils.InventoryUtil;
import forestry.core.utils.RecipeUtils;
import forestry.factory.features.FactoryTiles;
import forestry.factory.gui.ContainerFabricator;
import forestry.factory.inventory.InventoryFabricator;

public class TileFabricator extends TilePowered implements ISlotPickupWatcher, ILiquidTankTile, WorldlyContainer {
	private static final int MAX_HEAT = 5000;

	private final InventoryAdapterTile craftingInventory;
	private final TankManager tankManager;
	private final FilteredTank moltenTank;
	private int heat = 0;
	private int meltingPoint = 0;

	public TileFabricator(BlockPos pos, BlockState state) {
		super(FactoryTiles.FABRICATOR.tileType(), pos, state, 1100, 3300);
		setEnergyPerWorkCycle(200);
		craftingInventory = new InventoryGhostCrafting<>(this, InventoryGhostCrafting.SLOT_CRAFTING_COUNT);
		setInternalInventory(new InventoryFabricator(this));

		moltenTank = new FilteredTank(8 * FluidType.BUCKET_VOLUME, false, true).setFilter(FluidRecipeFilter.FABRICATOR_SMELTING_OUTPUT);

		tankManager = new TankManager(this, moltenTank);
	}

	/* SAVING & LOADING */

	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);

		compound.putInt("Heat", heat);
		tankManager.write(compound);
		craftingInventory.write(compound);
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);

		heat = compound.getInt("Heat");
		tankManager.read(compound);
		craftingInventory.read(compound);
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		tankManager.writeData(data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(FriendlyByteBuf data) {
		tankManager.readData(data);
	}

	/* UPDATING */
	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		if (!moltenTank.isFull()) {
			trySmelting();
		}

		if (!moltenTank.isEmpty()) {
			// Remove smelt if we have gone below melting point
			if (heat < getMeltingPoint() - 100) {
				moltenTank.drain(5, IFluidHandler.FluidAction.EXECUTE);
			}
		}

		if (heat > 2500) {
			this.heat -= 2;
		} else if (heat > 0) {
			this.heat--;
		}
	}

	private void trySmelting() {
		IInventoryAdapter inventory = getInternalInventory();

		ItemStack smeltResource = inventory.getItem(InventoryFabricator.SLOT_METAL);
		if (smeltResource.isEmpty()) {
			return;
		}

		IFabricatorSmeltingRecipe smelt = RecipeUtils.getFabricatorMeltingRecipe(this.level.getRecipeManager(), smeltResource);
		if (smelt == null || smelt.getMeltingPoint() > heat) {
			return;
		}

		FluidStack smeltFluid = smelt.getResultFluid();
		if (moltenTank.fillInternal(smeltFluid, IFluidHandler.FluidAction.SIMULATE) == smeltFluid.getAmount()) {
			this.removeItem(InventoryFabricator.SLOT_METAL, 1);
			moltenTank.fillInternal(smeltFluid, IFluidHandler.FluidAction.EXECUTE);
			meltingPoint = smelt.getMeltingPoint();
		}
	}

	@Override
	public boolean workCycle() {
		this.heat += 100;
		if (this.heat > MAX_HEAT) {
			this.heat = MAX_HEAT;
		}

		craftResult();

		return true;
	}

	@Nullable
	private IFabricatorRecipe getRecipe() {
		IInventoryAdapter inventory = getInternalInventory();
		ItemStack plan = inventory.getItem(InventoryFabricator.SLOT_PLAN);
		FluidStack liquid = moltenTank.getFluid();
		IFabricatorRecipe recipe = RecipeUtils.getFabricatorRecipe(level.getRecipeManager(), level, liquid, plan, craftingInventory);
		if (!liquid.isEmpty() && recipe != null && !liquid.containsFluid(recipe.getResultFluid())) {
			return null;
		}
		return recipe;
	}

	public ItemStack getResult(@Nullable IFabricatorRecipe myRecipe) {
		if (myRecipe == null) {
			return ItemStack.EMPTY;
		}

		return myRecipe.getCraftingGridRecipe().getResultItem(this.level.registryAccess()).copy();
	}

	/* ISlotPickupWatcher */
	@Override
	public void onTake(int slotIndex, Player player) {
		if (slotIndex == InventoryFabricator.SLOT_RESULT) {
			removeItem(InventoryFabricator.SLOT_RESULT, 1);
		}
	}

	private void craftResult() {
		IFabricatorRecipe myRecipe = getRecipe();
		ItemStack craftResult = getResult(myRecipe);

		if (myRecipe != null && !craftResult.isEmpty() && getItem(InventoryFabricator.SLOT_RESULT).isEmpty()) {
			FluidStack liquid = myRecipe.getResultFluid();

			// Remove resources
			if (removeFromInventory(myRecipe, false)) {
				FluidStack drained = moltenTank.drainInternal(liquid, IFluidHandler.FluidAction.SIMULATE);
				if (!drained.isEmpty() && drained.isFluidStackIdentical(liquid)) {
					removeFromInventory(myRecipe, true);
					moltenTank.drain(liquid.getAmount(), IFluidHandler.FluidAction.EXECUTE);

					// Damage plan
					if (!getItem(InventoryFabricator.SLOT_PLAN).isEmpty()) {
						Item planItem = getItem(InventoryFabricator.SLOT_PLAN).getItem();
						if (planItem instanceof ICraftingPlan) {
							ItemStack planUsed = ((ICraftingPlan) planItem).planUsed(getItem(InventoryFabricator.SLOT_PLAN), craftResult);
							setItem(InventoryFabricator.SLOT_PLAN, planUsed);
						}
					}

					setItem(InventoryFabricator.SLOT_RESULT, craftResult);
				}
			}
		}
	}

	private boolean removeFromInventory(IFabricatorRecipe recipe, boolean doRemove) {
		Container inventory = new InventoryMapper(this, InventoryFabricator.SLOT_INVENTORY_1, InventoryFabricator.SLOT_INVENTORY_COUNT);
		return InventoryUtil.consumeIngredients(inventory, recipe.getCraftingGridRecipe().getIngredients(), null, true, false, doRemove);
	}

	@Override
	public boolean hasWork() {
		boolean hasRecipe = true;
		boolean hasLiquidResources = true;
		boolean hasResources = true;

		ItemStack plan = getItem(InventoryFabricator.SLOT_PLAN);
		IFabricatorRecipe recipe = RecipeUtils.getFabricatorRecipe(level.getRecipeManager(), level, moltenTank.getFluid(), plan, craftingInventory);
		if (recipe != null) {
			hasResources = removeFromInventory(recipe, false);
			FluidStack toDrain = recipe.getResultFluid();
			FluidStack drained = moltenTank.drainInternal(toDrain, IFluidHandler.FluidAction.SIMULATE);
			hasLiquidResources = !drained.isEmpty() && drained.isFluidStackIdentical(toDrain);
		} else {
			hasRecipe = RecipeUtils.getFabricatorMeltingRecipe(level.getRecipeManager(), getItem(InventoryFabricator.SLOT_METAL)) != null;
		}

		IErrorLogic errorLogic = getErrorLogic();
		errorLogic.setCondition(!hasRecipe, ForestryError.NO_RECIPE);
		errorLogic.setCondition(!hasLiquidResources, ForestryError.NO_RESOURCE_LIQUID);
		errorLogic.setCondition(!hasResources, ForestryError.NO_RESOURCE_INVENTORY);

		return hasRecipe;
	}

	public int getHeatScaled(int i) {
		return heat * i / MAX_HEAT;
	}

	private int getMeltingPoint() {
		if (!this.getItem(InventoryFabricator.SLOT_METAL).isEmpty()) {
			IFabricatorSmeltingRecipe meltingRecipe = RecipeUtils.getFabricatorMeltingRecipe(getLevel().getRecipeManager(), this.getItem(InventoryFabricator.SLOT_METAL));
			return meltingRecipe == null ? 0 : meltingRecipe.getMeltingPoint();
		} else if (moltenTank.getFluidAmount() > 0) {
			return meltingPoint;
		}

		return 0;
	}

	public int getMeltingPointScaled(int i) {
		int meltingPoint = getMeltingPoint();

		if (meltingPoint <= 0) {
			return 0;
		} else {
			return meltingPoint * i / MAX_HEAT;
		}
	}

	/* SMP */
	public void getGUINetworkData(int i, int j) {
		if (i == 0) {
			heat = j;
		} else if (i == 1) {
			meltingPoint = j;
		}
	}

	public void sendGUINetworkData(AbstractContainerMenu container, ContainerListener iCrafting) {
		iCrafting.dataChanged(container, 0, heat);
		iCrafting.dataChanged(container, 1, getMeltingPoint());
	}

	/**
	 * @return Inaccessible crafting inventory for the craft grid.
	 */
	public InventoryAdapter getCraftingInventory() {
		return craftingInventory;
	}

	@Override
	public TankManager getTankManager() {
		return tankManager;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == ForgeCapabilities.FLUID_HANDLER) {
			return LazyOptional.of(() -> tankManager).cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerFabricator(windowId, player.getInventory(), this);
	}
}
