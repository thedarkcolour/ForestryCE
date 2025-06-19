package forestry.factory.tiles;

import forestry.api.ForestryDataMaps;
import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.api.fuels.FermenterFuel;
import forestry.api.recipes.IFermenterRecipe;
import forestry.api.recipes.IVariableFermentable;
import forestry.core.config.Constants;
import forestry.core.fluids.FilteredTank;
import forestry.core.fluids.FluidHelper;
import forestry.core.fluids.FluidRecipeFilter;
import forestry.core.fluids.TankManager;
import forestry.core.render.TankRenderInfo;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.tiles.TilePowered;
import forestry.core.utils.RecipeUtil;
import forestry.factory.features.FactoryTiles;
import forestry.factory.gui.FermenterMenu;
import forestry.factory.inventory.InventoryFermenter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class TileFermenter extends TilePowered implements WorldlyContainer, ILiquidTankTile {
	private final FilteredTank resourceTank;
	private final FilteredTank productTank;
	private final TankManager tankManager;

	@Nullable
	private IFermenterRecipe currentRecipe;
	private float currentResourceModifier;
	private int fermentationTime = 0;
	private int fermentationTotalTime = 0;
	private int fuelBurnTime = 0;
	private int fuelTotalTime = 0;
	private int fuelCurrentFerment = 0;

	public TileFermenter(BlockPos pos, BlockState state) {
		super(FactoryTiles.FERMENTER.tileType(), pos, state, 2000, 80000);
		setEnergyPerWorkCycle(4200);
		setInternalInventory(new InventoryFermenter(this));

		this.resourceTank = new FilteredTank(Constants.PROCESSOR_TANK_CAPACITY, true, true).setFilter(FluidRecipeFilter.FERMENTER_INPUT);
		this.productTank = new FilteredTank(Constants.PROCESSOR_TANK_CAPACITY, false, true).setFilter(FluidRecipeFilter.FERMENTER_OUTPUT);
		this.tankManager = new TankManager(this, this.resourceTank, this.productTank);
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		nbt.putInt("FermentationTime", this.fermentationTime);
		nbt.putInt("FermentationTotalTime", this.fermentationTotalTime);
		nbt.putInt("FuelBurnTime", this.fuelBurnTime);
		nbt.putInt("FuelTotalTime", this.fuelTotalTime);
		nbt.putInt("FuelCurrentFerment", this.fuelCurrentFerment);

		this.tankManager.write(nbt, registries);
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		this.fermentationTime = nbt.getInt("FermentationTime");
		this.fermentationTotalTime = nbt.getInt("FermentationTotalTime");
		this.fuelBurnTime = nbt.getInt("FuelBurnTime");
		this.fuelTotalTime = nbt.getInt("FuelTotalTime");
		this.fuelCurrentFerment = nbt.getInt("FuelCurrentFerment");

		this.tankManager.read(nbt, registries);
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf buffer) {
		super.writeData(buffer);
		this.tankManager.writeData(buffer);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(RegistryFriendlyByteBuf buffer) {
		super.readData(buffer);
		this.tankManager.readData(buffer);
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		if (updateOnInterval(20)) {
			FluidHelper.drainContainers(this.tankManager, this, InventoryFermenter.SLOT_INPUT);

			FluidStack fluidStack = this.productTank.getFluid();
			if (fluidStack != null) {
				FluidHelper.fillContainers(this.tankManager, this, InventoryFermenter.SLOT_CAN_INPUT, InventoryFermenter.SLOT_CAN_OUTPUT, fluidStack.getFluid(), true);
			}
		}
	}

	@Override
	public boolean workCycle() {
		if (this.currentRecipe == null) {
			return false;
		}

		int fermented = Math.min(this.fermentationTime, this.fuelCurrentFerment);
		int productAmount = Math.round(fermented * this.currentRecipe.getModifier() * this.currentResourceModifier);
		this.productTank.fillInternal(new FluidStack(this.currentRecipe.getOutputFluid(), productAmount), IFluidHandler.FluidAction.EXECUTE);

		this.fuelBurnTime--;
		this.resourceTank.drain(fermented, IFluidHandler.FluidAction.EXECUTE);
		this.fermentationTime -= fermented;

		// Not done yet
		if (this.fermentationTime > 0) {
			return false;
		}

		this.currentRecipe = null;
		return true;
	}

	private void checkRecipe() {
		if (this.currentRecipe != null) {
			return;
		}

		ItemStack resource = getItem(InventoryFermenter.SLOT_RESOURCE);
		FluidStack fluid = this.resourceTank.getFluid();

		if (!fluid.isEmpty()) {
			this.currentRecipe = RecipeUtil.unwrap(RecipeUtil.getFermenterRecipe(this.level.getRecipeManager(), resource, fluid));
		}

		this.fermentationTotalTime = this.fermentationTime = this.currentRecipe == null ? 0 : this.currentRecipe.getFermentationValue();

		if (this.currentRecipe != null) {
			this.currentResourceModifier = determineResourceMod(resource);
			removeItem(InventoryFermenter.SLOT_RESOURCE, 1);
		}
	}

	private void checkFuel() {
		if (this.fuelBurnTime <= 0) {
			ItemStack fuel = getItem(InventoryFermenter.SLOT_FUEL);
			if (!fuel.isEmpty()) {
				FermenterFuel fermenterFuel = fuel.getItemHolder().getData(ForestryDataMaps.FERMENTER_FUELS);
				if (fermenterFuel != null) {
					this.fuelBurnTime = this.fuelTotalTime = fermenterFuel.burnDuration();
					this.fuelCurrentFerment = fermenterFuel.fermentPerCycle();

					removeItem(InventoryFermenter.SLOT_FUEL, 1);
				}
			}
		}
	}

	private static float determineResourceMod(ItemStack stack) {
		if (stack.getItem() instanceof IVariableFermentable fermentable) {
			return fermentable.getFermentationModifier(stack);
		}
		return 1.0f;
	}

	@Override
	public boolean hasWork() {
		checkRecipe();
		checkFuel();

		int fermented = Math.min(this.fermentationTime, this.fuelCurrentFerment);

		boolean hasRecipe = this.currentRecipe != null;
		boolean hasFuel = this.fuelBurnTime > 0;
		boolean hasResource = this.fermentationTime > 0 || !getItem(InventoryFermenter.SLOT_RESOURCE).isEmpty();
		FluidStack drained = this.resourceTank.drain(fermented, IFluidHandler.FluidAction.SIMULATE);
		boolean hasFluidResource = !drained.isEmpty() && drained.getAmount() == fermented;
		boolean hasFluidSpace = true;

		if (hasRecipe) {
			int productAmount = Math.round(fermented * this.currentRecipe.getModifier() * this.currentResourceModifier);
			Fluid output = this.currentRecipe.getOutputFluid();
			FluidStack fluidStack = new FluidStack(output, productAmount);
			hasFluidSpace = this.productTank.fillInternal(fluidStack, IFluidHandler.FluidAction.SIMULATE) == fluidStack.getAmount();
		}

		IErrorLogic errorLogic = getErrorLogic();
		errorLogic.setCondition(!hasRecipe, ForestryError.NO_RECIPE);
		errorLogic.setCondition(!hasFuel, ForestryError.NO_FUEL);
		errorLogic.setCondition(!hasResource, ForestryError.NO_RESOURCE);
		errorLogic.setCondition(!hasFluidResource, ForestryError.NO_RESOURCE_LIQUID);
		errorLogic.setCondition(!hasFluidSpace, ForestryError.NO_SPACE_TANK);

		return hasRecipe && hasFuel && hasResource && hasFluidResource && hasFluidSpace;
	}

	public int getBurnTimeRemainingScaled(int i) {
		if (this.fuelTotalTime == 0) {
			return 0;
		}

		return this.fuelBurnTime * i / this.fuelTotalTime;
	}

	public int getFermentationProgressScaled(int i) {
		if (this.fermentationTotalTime == 0) {
			return 0;
		}

		return this.fermentationTime * i / this.fermentationTotalTime;
	}

	@Override
	public TankRenderInfo getResourceTankInfo() {
		return new TankRenderInfo(this.resourceTank);
	}

	@Override
	public TankRenderInfo getProductTankInfo() {
		return new TankRenderInfo(this.productTank);
	}

	/* SMP GUI */
	public void getGUINetworkData(int i, int j) {
		switch (i) {
			case 0 -> this.fuelBurnTime = j;
			case 1 -> this.fuelTotalTime = j;
			case 2 -> this.fermentationTime = j;
			case 3 -> this.fermentationTotalTime = j;
		}
	}

	public void sendGUINetworkData(AbstractContainerMenu container, ContainerListener iCrafting) {
		iCrafting.dataChanged(container, 0, this.fuelBurnTime);
		iCrafting.dataChanged(container, 1, this.fuelTotalTime);
		iCrafting.dataChanged(container, 2, this.fermentationTime);
		iCrafting.dataChanged(container, 3, this.fermentationTotalTime);
	}


	@Override
	public TankManager getTankManager() {
		return this.tankManager;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new FermenterMenu(windowId, inv, this);
	}
}
