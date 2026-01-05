package forestry.factory.tiles;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.api.recipes.ICarpenterRecipe;
import forestry.core.config.Constants;
import forestry.core.fluids.FilteredTank;
import forestry.core.fluids.FluidHelper;
import forestry.core.fluids.FluidRecipeFilter;
import forestry.core.fluids.TankManager;
import forestry.core.inventory.InventoryAdapterTile;
import forestry.core.inventory.InventoryGhostCrafting;
import forestry.core.inventory.wrappers.InventoryMapper;
import forestry.core.render.TankRenderInfo;
import forestry.core.tiles.IItemStackDisplay;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.tiles.TilePowered;
import forestry.core.utils.InventoryUtil;
import forestry.core.utils.RecipeUtils;
import forestry.factory.features.FactoryTiles;
import forestry.factory.gui.ContainerCarpenter;
import forestry.factory.inventory.InventoryCarpenter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class TileCarpenter extends TilePowered implements WorldlyContainer, ILiquidTankTile, IItemStackDisplay {
	private static final int TICKS_PER_RECIPE_TIME = 1;
	private static final int ENERGY_PER_WORK_CYCLE = 2040;
	private static final int ENERGY_PER_RECIPE_TIME = ENERGY_PER_WORK_CYCLE / 10;

	private final FilteredTank resourceTank;
	private final TankManager tankManager;
	private final InventoryAdapterTile craftingInventory;
	private final ResultContainer craftPreviewInventory;

	@Nullable
	private ICarpenterRecipe currentRecipe;

	private ItemStack getBoxStack() {
		return getInternalInventory().getItem(InventoryCarpenter.SLOT_BOX);
	}

	public TileCarpenter(BlockPos pos, BlockState state) {
		super(FactoryTiles.CARPENTER.tileType(), pos, state, 1100, Constants.MACHINE_MAX_ENERGY);
		setEnergyPerWorkCycle(ENERGY_PER_WORK_CYCLE);
        this.resourceTank = new FilteredTank(Constants.PROCESSOR_TANK_CAPACITY).setFilter(FluidRecipeFilter.CARPENTER_INPUT);

        this.craftingInventory = new InventoryGhostCrafting<>(this, 10);
        this.craftPreviewInventory = new ResultContainer();
		setInternalInventory(new InventoryCarpenter(this));

        this.tankManager = new TankManager(this, this.resourceTank);
	}

	/* LOADING & SAVING */

	@Override
	public void saveAdditional(CompoundTag compoundNBT) {
		super.saveAdditional(compoundNBT);

        this.tankManager.write(compoundNBT);
        this.craftingInventory.write(compoundNBT);
	}

	@Override
	public void load(CompoundTag compoundNBT) {
		super.load(compoundNBT);
        this.tankManager.read(compoundNBT);
        this.craftingInventory.read(compoundNBT);
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		super.writeData(data);
        this.tankManager.writeData(data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(FriendlyByteBuf data) {
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
				setTicksPerWorkCycle(recipeTime * TICKS_PER_RECIPE_TIME);
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
			FluidHelper.drainContainers(this.tankManager, this, InventoryCarpenter.SLOT_CAN_INPUT);
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
			InventoryUtil.tryAddStack(this, pendingProduct, InventoryCarpenter.SLOT_PRODUCT, InventoryCarpenter.SLOT_PRODUCT_COUNT, true);
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
			if (!fluid.isFluidStackIdentical(drained)) {
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
			ItemStack box = getItem(InventoryCarpenter.SLOT_BOX);
			if (box.isEmpty()) {
				return false;
			}
			if (doRemove) {
				removeItem(InventoryCarpenter.SLOT_BOX, 1);
			}
		}

		Container inventory = new InventoryMapper(getInternalInventory(), InventoryCarpenter.SLOT_INVENTORY_1, InventoryCarpenter.SLOT_INVENTORY_COUNT);
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
			canAdd = InventoryUtil.tryAddStack(this, pendingProduct, InventoryCarpenter.SLOT_PRODUCT, InventoryCarpenter.SLOT_PRODUCT_COUNT, true, false);
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


	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == ForgeCapabilities.FLUID_HANDLER) {
			return LazyOptional.of(() -> this.tankManager).cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerCarpenter(windowId, player.getInventory(), this);
	}
}
