package forestry.core.content.machines.tiles;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.api.core.machines.ICarpenterRecipe;
import forestry.core.platform.config.Constants;
import forestry.core.platform.fluids.FilteredTank;
import forestry.core.platform.fluids.FluidHelper;
import forestry.core.platform.fluids.FluidRecipeFilter;
import forestry.core.platform.fluids.TankManager;
import forestry.core.platform.inventory.BlockEntityInventoryAdapter;
import forestry.core.platform.inventory.GhostCraftingInventory;
import forestry.core.platform.inventory.wrappers.InventoryMapper;
import forestry.core.platform.render.TankRenderInfo;
import forestry.core.platform.tile.IItemStackDisplay;
import forestry.core.platform.tile.ILiquidTankTile;
import forestry.core.platform.tile.PoweredBlockEntity;
import forestry.core.platform.util.InventoryUtil;
import forestry.core.platform.util.RecipeUtils;
import forestry.core.content.machines.blocks.FactoryBlock;
import forestry.core.content.machines.features.FactoryBlockEntities;
import forestry.core.content.machines.gui.CarpenterMenu;
import forestry.core.content.machines.inventory.CarpenterInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class CarpenterBlockEntity extends PoweredBlockEntity implements WorldlyContainer, ILiquidTankTile, IItemStackDisplay {
	private static final int STEPS_PER_RECIPE_TIME = 1;
	private static final int ENERGY_PER_WORK_CYCLE = 2040;
	private static final int ENERGY_PER_RECIPE_TIME = ENERGY_PER_WORK_CYCLE / 10;

	private final FilteredTank resourceTank;
	private final TankManager tankManager;
	private final BlockEntityInventoryAdapter craftingInventory;
	private final ResultContainer craftPreviewInventory;

	@Nullable
	private ICarpenterRecipe currentRecipe;

	private ItemStack getBoxStack() {
		return getInternalInventory().getItem(CarpenterInventory.SLOT_BOX);
	}

	public CarpenterBlockEntity(BlockPos pos, BlockState state) {
		super(FactoryBlockEntities.CARPENTER.tileType(), pos, state, 1100, Constants.MACHINE_MAX_ENERGY);
		setEnergyPerWorkCycle(ENERGY_PER_WORK_CYCLE);
        this.resourceTank = new FilteredTank(Constants.PROCESSOR_TANK_CAPACITY).setFilter(FluidRecipeFilter.CARPENTER_INPUT);

        this.craftingInventory = new GhostCraftingInventory<>(this, 10);
        this.craftPreviewInventory = new ResultContainer();
		setInternalInventory(new CarpenterInventory(this));

        this.tankManager = new TankManager(this, this.resourceTank);
	}

	/* LOADING & SAVING */

	@Override
	public void saveAdditional(CompoundTag compoundNBT, HolderLookup.Provider registries) {
		super.saveAdditional(compoundNBT, registries);

        this.tankManager.write(compoundNBT, registries);
        this.craftingInventory.write(compoundNBT, registries);
	}

	@Override
	public void loadAdditional(CompoundTag compoundNBT, HolderLookup.Provider registries) {
		super.loadAdditional(compoundNBT, registries);
        this.tankManager.read(compoundNBT, registries);
        this.craftingInventory.read(compoundNBT, registries);
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf data) {
		super.writeData(data);
        this.tankManager.writeData(data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(RegistryFriendlyByteBuf data) {
		super.readData(data);
        this.tankManager.readData(data);
	}

	public void checkRecipe(RegistryAccess registryAccess) {
		if (this.level.isClientSide) {
			return;
		}

		if (this.currentRecipe == null || !this.currentRecipe.matches(this.resourceTank.getFluid(), getBoxStack(), this.craftingInventory, this.level)) {
			ICarpenterRecipe recipe = RecipeUtils.getCarpenterRecipe(this.level.getRecipeManager(), this.resourceTank.getFluid(), getBoxStack(), this.craftingInventory, this.level);
			this.currentRecipe = recipe;

			if (recipe != null) {
				int recipeTime = this.currentRecipe.getPackagingTime();
				setStepsPerWorkCycle(recipeTime * STEPS_PER_RECIPE_TIME);
				setEnergyPerWorkCycle(recipeTime * ENERGY_PER_RECIPE_TIME);

				ItemStack craftingResult = this.currentRecipe.getResultItem(registryAccess);
                this.craftPreviewInventory.setItem(0, craftingResult);
			} else {
                this.craftPreviewInventory.setItem(0, ItemStack.EMPTY);
			}
		}
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		if (updateOnInterval(20)) {
			FluidHelper.drainContainers(this.tankManager, this, CarpenterInventory.SLOT_CAN_INPUT);

			TankRenderInfo resourceTankInfo = this.getResourceTankInfo();

			int newLevel = resourceTankInfo.getLevel().getLevelScaled(4);
			boolean update = false;

			if (state.hasProperty(FactoryBlock.TANK_RESOURCE_LEVEL) &&
				state.getValue(FactoryBlock.TANK_RESOURCE_LEVEL) != newLevel) {
				state = state.setValue(FactoryBlock.TANK_RESOURCE_LEVEL, newLevel);
				update = true;
			}

			if (update) {
				level.setBlock(pos, state, FactoryBlock.UPDATE_CLIENTS);
			}
		}
	}

	@Override
	public boolean workCycle() {
		if (!removeLiquidResources(true)) {
			return false;
		}
		if (!removeItemResources(true)) {
			return false;
		}

		if (this.currentRecipe != null) {
			ItemStack pendingProduct = this.currentRecipe.getResultItem(this.level.registryAccess());
			InventoryUtil.tryAddStack(this, pendingProduct, CarpenterInventory.SLOT_PRODUCT, CarpenterInventory.SLOT_PRODUCT_COUNT, true);
		}
		return true;
	}

	private boolean removeLiquidResources(boolean doRemove) {
		if (this.currentRecipe == null) {
			return true;
		}

		FluidStack fluid = this.currentRecipe.getInputFluid();
		if (!fluid.isEmpty()) {
			FluidStack drained = this.resourceTank.drainInternal(fluid, IFluidHandler.FluidAction.SIMULATE);
			if (!FluidStack.matches(fluid, drained)) {
				return false;
			}
			if (doRemove) {
                this.resourceTank.drainInternal(fluid, IFluidHandler.FluidAction.EXECUTE);
			}
		}

		return true;
	}

	private boolean removeItemResources(boolean doRemove) {
		if (this.currentRecipe == null) {
			return true;
		}

		if (!this.currentRecipe.getBox().isEmpty()) {
			ItemStack box = getItem(CarpenterInventory.SLOT_BOX);
			if (box.isEmpty()) {
				return false;
			}
			if (doRemove) {
				removeItem(CarpenterInventory.SLOT_BOX, 1);
			}
		}

		Container inventory = new InventoryMapper(getInternalInventory(), CarpenterInventory.SLOT_INVENTORY_1, CarpenterInventory.SLOT_INVENTORY_COUNT);
		return InventoryUtil.consumeIngredients(inventory, this.currentRecipe.getCraftingGridRecipe().getIngredients(), null, true, false, doRemove);
	}

	/* STATE INFORMATION */
	@Override
	public boolean hasWork() {
		if (updateOnInterval(20)) {
			checkRecipe(this.level.registryAccess());
		}

		boolean hasRecipe = this.currentRecipe != null;
		boolean hasLiquidResources = true;
		boolean hasItemResources = true;
		boolean canAdd = true;

		if (hasRecipe) {
			hasLiquidResources = removeLiquidResources(false);
			hasItemResources = removeItemResources(false);

			ItemStack pendingProduct = this.currentRecipe.getResultItem(this.level.registryAccess());
			canAdd = InventoryUtil.tryAddStack(this, pendingProduct, CarpenterInventory.SLOT_PRODUCT, CarpenterInventory.SLOT_PRODUCT_COUNT, true, false);
		}

		IErrorLogic errorLogic = getErrorLogic();
		errorLogic.setCondition(!hasRecipe, ForestryError.NO_RECIPE);
		errorLogic.setCondition(!hasLiquidResources, ForestryError.NO_RESOURCE_LIQUID);
		errorLogic.setCondition(!hasItemResources, ForestryError.NO_RESOURCE_INVENTORY);
		errorLogic.setCondition(!canAdd, ForestryError.NO_SPACE_INVENTORY);

		return hasRecipe && hasItemResources && hasLiquidResources && canAdd;
	}

	@Override
	public TankRenderInfo getResourceTankInfo() {
		return new TankRenderInfo(this.resourceTank);
	}

	/**
	 * @return Inaccessible crafting inventory for the craft grid.
	 */
	public Container getCraftingInventory() {
		return this.craftingInventory;
	}

	public Container getCraftPreviewInventory() {
		return this.craftPreviewInventory;
	}

	@Override
	public void handleItemStackForDisplay(ItemStack itemStack) {
        this.craftPreviewInventory.setItem(0, itemStack);
	}


	@Override
	public TankManager getTankManager() {
		return this.tankManager;
	}

	public IFluidHandler getFluidHandler(@Nullable Direction facing) {
		return this.tankManager;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new CarpenterMenu(windowId, player.getInventory(), this);
	}
}
