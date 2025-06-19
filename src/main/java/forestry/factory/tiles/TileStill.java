package forestry.factory.tiles;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.api.recipes.IStillRecipe;
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
import forestry.factory.gui.StillMenu;
import forestry.factory.inventory.InventoryStill;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Objects;

public class TileStill extends TilePowered implements WorldlyContainer, ILiquidTankTile {
	private static final int ENERGY_PER_RECIPE_TIME = 200;

	private final FilteredTank resourceTank;
	private final FilteredTank productTank;
	private final TankManager tankManager;

	@Nullable
	private RecipeHolder<IStillRecipe> currentRecipe = null;
	private FluidStack bufferedLiquid = FluidStack.EMPTY;

	public TileStill(BlockPos pos, BlockState state) {
		super(FactoryTiles.STILL.tileType(), pos, state, 1100, 80000);
		setInternalInventory(new InventoryStill(this));

		this.resourceTank = new FilteredTank(Constants.PROCESSOR_TANK_CAPACITY, true, true).setFilter(FluidRecipeFilter.STILL_INPUT);
		this.productTank = new FilteredTank(Constants.PROCESSOR_TANK_CAPACITY, false, true).setFilter(FluidRecipeFilter.STILL_OUTPUT);
		this.tankManager = new TankManager(this, this.resourceTank, this.productTank);
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.tankManager.write(nbt, registries);

		if (!this.bufferedLiquid.isEmpty()) {
			CompoundTag buffer = new CompoundTag();
			this.bufferedLiquid.writeToNBT(buffer);
			nbt.put("Buffer", buffer);
		}
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.tankManager.read(nbt, registries);

		if (nbt.contains("Buffer")) {
			CompoundTag buffer = nbt.getCompound("Buffer");
			this.bufferedLiquid = FluidStack.loadFluidStackFromNBT(buffer);
		}
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf buffer) {
		super.writeData(buffer);
		this.tankManager.writeData(buffer);
	}

	@Override
	public void readData(RegistryFriendlyByteBuf buffer) {
		super.readData(buffer);
		this.tankManager.readData(buffer);
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		if (updateOnInterval(20)) {
			FluidHelper.drainContainers(this.tankManager, this, InventoryStill.SLOT_CAN);

			FluidStack fluidStack = this.productTank.getFluid();
			if (!fluidStack.isEmpty()) {
				FluidHelper.fillContainers(this.tankManager, this, InventoryStill.SLOT_RESOURCE, InventoryStill.SLOT_PRODUCT, fluidStack.getFluid(), true);
			}
		}
	}

	@Override
	public boolean workCycle() {
		int cycles = this.currentRecipe.getCyclesPerUnit();
		FluidStack output = this.currentRecipe.getOutput();

		FluidStack product = new FluidStack(output, output.getAmount() * cycles);
		this.productTank.fillInternal(product, IFluidHandler.FluidAction.EXECUTE);

		this.bufferedLiquid = FluidStack.EMPTY;

		return true;
	}

	private void checkRecipe() {
		FluidStack recipeLiquid = !this.bufferedLiquid.isEmpty() ? this.bufferedLiquid : this.resourceTank.getFluid();

		if (this.currentRecipe == null || !this.currentRecipe.value().matches(recipeLiquid)) {
			Level level = Objects.requireNonNull(this.level);
			this.currentRecipe = RecipeUtil.getStillRecipe(level.getRecipeManager(), recipeLiquid);

			int recipeTime = this.currentRecipe == null ? 0 : this.currentRecipe.value().getCyclesPerUnit();
			setEnergyPerWorkCycle(ENERGY_PER_RECIPE_TIME * recipeTime);
			setStepsPerWorkCycle(recipeTime);
		}
	}

	@Override
	public boolean hasWork() {
		checkRecipe();

		boolean hasRecipe = this.currentRecipe != null;
		boolean hasTankSpace = true;
		boolean hasLiquidResource = true;

		if (hasRecipe) {
			FluidStack fluidStack = this.currentRecipe.getOutput();
			hasTankSpace = this.productTank.fillInternal(fluidStack, IFluidHandler.FluidAction.SIMULATE) == fluidStack.getAmount();
			if (this.bufferedLiquid.isEmpty()) {
				int cycles = this.currentRecipe.getCyclesPerUnit();
				FluidStack input = this.currentRecipe.getInput();
				int drainAmount = cycles * input.getAmount();
				FluidStack drained = this.resourceTank.drain(drainAmount, IFluidHandler.FluidAction.SIMULATE);
				hasLiquidResource = !drained.isEmpty() && drained.getAmount() == drainAmount;
				if (hasLiquidResource) {
					this.bufferedLiquid = input.copyWithAmount(drainAmount);
					this.resourceTank.drain(drainAmount, IFluidHandler.FluidAction.EXECUTE);
				}
			}
		}

		IErrorLogic errorLogic = getErrorLogic();
		errorLogic.setCondition(!hasRecipe, ForestryError.NO_RECIPE);
		errorLogic.setCondition(!hasTankSpace, ForestryError.NO_SPACE_TANK);
		errorLogic.setCondition(!hasLiquidResource, ForestryError.NO_RESOURCE_LIQUID);

		return hasRecipe && hasLiquidResource && hasTankSpace;
	}

	@Override
	public TankRenderInfo getResourceTankInfo() {
		return new TankRenderInfo(this.resourceTank);
	}

	@Override
	public TankRenderInfo getProductTankInfo() {
		return new TankRenderInfo(this.productTank);
	}


	@Override
	public TankManager getTankManager() {
		return this.tankManager;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new StillMenu(windowId, player.getInventory(), this);
	}
}
