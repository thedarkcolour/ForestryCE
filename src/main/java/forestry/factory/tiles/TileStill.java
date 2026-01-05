package forestry.factory.tiles;

import com.google.common.base.Preconditions;
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
import forestry.core.utils.RecipeUtils;
import forestry.factory.features.FactoryTiles;
import forestry.factory.gui.ContainerStill;
import forestry.factory.inventory.InventoryStill;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Objects;

public class TileStill extends TilePowered implements WorldlyContainer, ILiquidTankTile {
	private static final int ENERGY_PER_RECIPE_TIME = 200;

	private final FilteredTank resourceTank;
	private final FilteredTank productTank;
	private final TankManager tankManager;

	@Nullable
	private IStillRecipe currentRecipe = null;
	private FluidStack bufferedLiquid = FluidStack.EMPTY;

	public TileStill(BlockPos pos, BlockState state) {
		super(FactoryTiles.STILL.tileType(), pos, state, 1100, 80000);
		setInternalInventory(new InventoryStill(this));

		this.resourceTank = new FilteredTank(Constants.PROCESSOR_TANK_CAPACITY, true, true).setFilter(FluidRecipeFilter.STILL_INPUT);
		this.productTank = new FilteredTank(Constants.PROCESSOR_TANK_CAPACITY, false, true).setFilter(FluidRecipeFilter.STILL_OUTPUT);
		this.tankManager = new TankManager(this, this.resourceTank, this.productTank);
	}

	@Override
	public void saveAdditional(CompoundTag compoundNBT) {
		super.saveAdditional(compoundNBT);
        this.tankManager.write(compoundNBT);

		if (!this.bufferedLiquid.isEmpty()) {
			CompoundTag buffer = new CompoundTag();
            this.bufferedLiquid.writeToNBT(buffer);
			compoundNBT.put("Buffer", buffer);
		}
	}

	@Override
	public void load(CompoundTag compoundNBT) {
		super.load(compoundNBT);
        this.tankManager.read(compoundNBT);

		if (compoundNBT.contains("Buffer")) {
			CompoundTag buffer = compoundNBT.getCompound("Buffer");
            this.bufferedLiquid = FluidStack.loadFluidStackFromNBT(buffer);
		}
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		super.writeData(data);
        this.tankManager.writeData(data);
	}

	@Override
	public void readData(FriendlyByteBuf data) {
		super.readData(data);
        this.tankManager.readData(data);
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
		Preconditions.checkNotNull(this.currentRecipe);
		int cycles = this.currentRecipe.getCyclesPerUnit();
		FluidStack output = this.currentRecipe.getOutput();

		FluidStack product = new FluidStack(output, output.getAmount() * cycles);
        this.productTank.fillInternal(product, IFluidHandler.FluidAction.EXECUTE);

        this.bufferedLiquid = FluidStack.EMPTY;

		return true;
	}

	private void checkRecipe() {
		FluidStack recipeLiquid = !this.bufferedLiquid.isEmpty() ? this.bufferedLiquid : this.resourceTank.getFluid();

		if (this.currentRecipe == null || !this.currentRecipe.matches(recipeLiquid)) {
			Level level = Objects.requireNonNull(this.level);
			this.currentRecipe = RecipeUtils.getStillRecipe(level.getRecipeManager(), recipeLiquid);

			int recipeTime = this.currentRecipe == null ? 0 : this.currentRecipe.getCyclesPerUnit();
			setEnergyPerWorkCycle(ENERGY_PER_RECIPE_TIME * recipeTime);
			setTicksPerWorkCycle(recipeTime);
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
                    this.bufferedLiquid = new FluidStack(input, drainAmount);
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
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == ForgeCapabilities.FLUID_HANDLER) {
			return LazyOptional.of(() -> this.tankManager).cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerStill(windowId, player.getInventory(), this);
	}

}
