package forestry.factory.tiles;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.core.config.Constants;
import forestry.core.fluids.FluidHelper;
import forestry.core.fluids.FluidHelper.FillStatus;
import forestry.core.fluids.StandardTank;
import forestry.core.fluids.TankManager;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.inventory.watchers.ISlotPickupWatcher;
import forestry.core.render.TankRenderInfo;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.tiles.TilePowered;
import forestry.factory.features.FactoryTiles;
import forestry.factory.gui.ContainerBottler;
import forestry.factory.inventory.InventoryBottler;
import forestry.factory.recipes.BottlerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class TileBottler extends TilePowered implements WorldlyContainer, ILiquidTankTile, ISlotPickupWatcher {
	private static final int TICKS_PER_RECIPE_TIME = 5;
	private static final int ENERGY_PER_RECIPE_TIME = 1000;

	private final StandardTank resourceTank;
	private final TankManager tankManager;

	private final EnumMap<Direction, Boolean> canDump;
	private boolean dumpingFluid = false;
	@Nullable
	private BottlerRecipe currentRecipe;
	@OnlyIn(Dist.CLIENT)
	public boolean isFillRecipe;

	public TileBottler(BlockPos pos, BlockState state) {
		super(FactoryTiles.BOTTLER.tileType(), pos, state, 1100, 40000);

		setInternalInventory(new InventoryBottler(this));

        this.resourceTank = new StandardTank(Constants.PROCESSOR_TANK_CAPACITY);
        this.tankManager = new TankManager(this, this.resourceTank);

        this.canDump = new EnumMap<>(Direction.class);
	}

	/* SAVING & LOADING */

	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
        this.tankManager.write(compound);
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
        this.tankManager.read(compound);
		checkEmptyRecipe();
		checkFillRecipe();
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

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		if (updateOnInterval(20)) {
			ItemStack leftProcessingStack = getItem(InventoryBottler.SLOT_EMPTYING_PROCESSING);
			ItemStack rightProcessingStack = getItem(InventoryBottler.SLOT_FILLING_PROCESSING);
			if (leftProcessingStack.isEmpty()) {
				ItemStack inputStack = getItem(InventoryBottler.SLOT_INPUT_FULL_CONTAINER);
				if (!inputStack.isEmpty()) {
					leftProcessingStack = removeItem(InventoryBottler.SLOT_INPUT_FULL_CONTAINER, 1);
					setItem(InventoryBottler.SLOT_EMPTYING_PROCESSING, leftProcessingStack);
				}
			}
			if (rightProcessingStack.isEmpty()) {
				ItemStack inputStack = getItem(InventoryBottler.SLOT_INPUT_EMPTY_CONTAINER);
				if (!inputStack.isEmpty()) {
					rightProcessingStack = removeItem(InventoryBottler.SLOT_INPUT_EMPTY_CONTAINER, 1);
					setItem(InventoryBottler.SLOT_FILLING_PROCESSING, rightProcessingStack);
				}
			}
		}

		if (canDump()) {
			if (this.dumpingFluid || updateOnInterval(20)) {
                this.dumpingFluid = dumpFluid();
			}
		}
	}

	private boolean canDump() {
		FluidStack fluid = this.tankManager.getFluid(0);
		if (fluid != null) {
			if (this.canDump.isEmpty()) {
				for (Direction facing : Direction.VALUES) {
                    this.canDump.put(facing, FluidHelper.canAcceptFluid(this.level, this.worldPosition.relative(facing), facing.getOpposite(), fluid));
				}
			}

			for (Direction facing : Direction.VALUES) {
				if (this.canDump.get(facing)) {
					return true;
				}
			}
		}
		return false;
	}

	//TODO - a bit ugly atm. Are the new checks worth the perf with the new interface? Can this be written better?
	//Is there a race condition here?
	private boolean dumpFluid() {
		if (!this.resourceTank.isEmpty()) {
			for (Direction facing : Direction.VALUES) {
				if (this.canDump.get(facing)) {
					LazyOptional<IFluidHandler> fluidDestination = FluidUtil.getFluidHandler(this.level, this.worldPosition.relative(facing), facing.getOpposite());

					if (fluidDestination.isPresent()) {
						fluidDestination.ifPresent(f -> FluidUtil.tryFluidTransfer(f, this.tankManager, FluidType.BUCKET_VOLUME / 20, true));
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean workCycle() {
		FluidHelper.FillStatus status;
		if (this.currentRecipe != null) {
			if (this.currentRecipe.fillRecipe) {
				status = FluidHelper.fillContainers(this.tankManager, this, InventoryBottler.SLOT_FILLING_PROCESSING, InventoryBottler.SLOT_OUTPUT_FULL_CONTAINER, this.currentRecipe.fluid.getFluid(), true);
			} else {
				status = FluidHelper.drainContainers(this.tankManager, this, InventoryBottler.SLOT_EMPTYING_PROCESSING, InventoryBottler.SLOT_OUTPUT_EMPTY_CONTAINER, true);
			}
		} else {
			return true;
		}

		if (status == FluidHelper.FillStatus.SUCCESS) {
            this.currentRecipe = null;
			return true;
		}
		return false;
	}

	@Override
	public void onNeighborTileChange(Level world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborTileChange(world, pos, neighbor);

        this.canDump.clear();
	}

	private void checkFillRecipe() {
		ItemStack emptyCan = getItem(InventoryBottler.SLOT_FILLING_PROCESSING);
		if (!emptyCan.isEmpty()) {
			FluidStack resource = this.resourceTank.getFluid();
			if (resource.isEmpty()) {
				return;
			}
			//Fill Container
			if (this.currentRecipe == null || !this.currentRecipe.matchEmpty(emptyCan, resource)) {
                this.currentRecipe = BottlerRecipe.createFillingRecipe(resource.getFluid(), emptyCan);
				if (this.currentRecipe != null) {
					float viscosityMultiplier = resource.getFluid().getFluidType().getViscosity(resource) / 1000.0f;
					viscosityMultiplier = (viscosityMultiplier - 1f) / 20f + 1f; // scale down the effect

					int fillAmount = Math.min(this.currentRecipe.fluid.getAmount(), resource.getAmount());
					float fillTime = fillAmount / (float) FluidType.BUCKET_VOLUME;
					fillTime *= viscosityMultiplier;

					setTicksPerWorkCycle(Math.round(fillTime * TICKS_PER_RECIPE_TIME));
					setEnergyPerWorkCycle(Math.round(fillTime * ENERGY_PER_RECIPE_TIME));
				}
			}
		}
	}

	private void checkEmptyRecipe() {
		ItemStack filledCan = getItem(InventoryBottler.SLOT_EMPTYING_PROCESSING);
		if (!filledCan.isEmpty()) {
			//Empty Container
			if (this.currentRecipe == null || !this.currentRecipe.matchFilled(filledCan) && !this.currentRecipe.fillRecipe) {
                this.currentRecipe = BottlerRecipe.createEmptyingRecipe(filledCan);
				if (this.currentRecipe != null) {
					FluidStack resource = this.currentRecipe.fluid;
					float viscosityMultiplier = resource.getFluid().getFluidType().getViscosity(resource) / 1000.0f;
					viscosityMultiplier = (viscosityMultiplier - 1f) / 20f + 1f; // scale down the effect

					int fillAmount = Math.min(this.currentRecipe.fluid.getAmount(), resource.getAmount());
					float fillTime = fillAmount / (float) FluidType.BUCKET_VOLUME;
					fillTime *= viscosityMultiplier;

					setTicksPerWorkCycle(Math.round(fillTime * TICKS_PER_RECIPE_TIME));
					setEnergyPerWorkCycle(0);
				}
			}
		}
	}

	@Override
	public void onTake(int slotIndex, Player player) {
		if (slotIndex == InventoryBottler.SLOT_EMPTYING_PROCESSING) {
			if (this.currentRecipe != null && !this.currentRecipe.fillRecipe) {
                this.currentRecipe = null;
				setTicksPerWorkCycle(0);
			}
		} else if (slotIndex == InventoryBottler.SLOT_FILLING_PROCESSING) {
			if (this.currentRecipe != null && this.currentRecipe.fillRecipe) {
                this.currentRecipe = null;
				setTicksPerWorkCycle(0);
			}
		}
	}

	@Override
	public void writeGuiData(FriendlyByteBuf data) {
		super.writeGuiData(data);
		if (this.currentRecipe == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(this.currentRecipe.fillRecipe);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readGuiData(FriendlyByteBuf data) {
		super.readGuiData(data);
        this.isFillRecipe = data.readBoolean();
	}

	@Override
	public boolean hasResourcesMin(float percentage) {
		IInventoryAdapter inventory = getInternalInventory();
		ItemStack emptyCan = inventory.getItem(InventoryBottler.SLOT_FILLING_PROCESSING);
		if (emptyCan.isEmpty()) {
			return false;
		}

		return (float) emptyCan.getCount() / (float) emptyCan.getMaxStackSize() > percentage;
	}

	@Override
	public boolean hasWork() {
		FluidHelper.FillStatus emptyStatus;
		FluidHelper.FillStatus fillStatus;
		IErrorLogic errorLogic = getErrorLogic();
		errorLogic.clearErrors();

		checkEmptyRecipe();
		if (this.currentRecipe != null) {
			IFluidTank tank = this.tankManager.getTank(0);
			if (tank != null) {
				emptyStatus = FluidHelper.drainContainers(this.tankManager, this, InventoryBottler.SLOT_EMPTYING_PROCESSING, InventoryBottler.SLOT_OUTPUT_EMPTY_CONTAINER, false);
			} else {
				emptyStatus = FillStatus.SUCCESS;
			}
		} else {
			emptyStatus = null;
		}
		if (emptyStatus != FillStatus.SUCCESS) {
			checkFillRecipe();
			if (this.currentRecipe == null) {
				return false;
			} else {
				fillStatus = FluidHelper.fillContainers(this.tankManager, this, InventoryBottler.SLOT_FILLING_PROCESSING, InventoryBottler.SLOT_OUTPUT_FULL_CONTAINER, this.currentRecipe.fluid.getFluid(), false);
			}
		} else {
			return true;
		}

		if (fillStatus == FillStatus.SUCCESS) {
			return true;
		}

		errorLogic.setCondition(fillStatus == FluidHelper.FillStatus.NO_FLUID, ForestryError.NO_RESOURCE_LIQUID);
		errorLogic.setCondition(fillStatus == FluidHelper.FillStatus.NO_SPACE, ForestryError.NO_SPACE_INVENTORY);
		errorLogic.setCondition(emptyStatus == FluidHelper.FillStatus.NO_SPACE_FLUID, ForestryError.NO_SPACE_TANK);
		if (emptyStatus == FillStatus.INVALID_INPUT || fillStatus == FillStatus.INVALID_INPUT || errorLogic.hasErrors()) {
            this.currentRecipe = null;
			return false;
		}
		return true;
	}

	@Override
	public TankRenderInfo getResourceTankInfo() {
		return new TankRenderInfo(this.resourceTank);
	}

	/* ILIQUIDCONTAINER */

	@Override
	public TankManager getTankManager() {
		return this.tankManager;
	}


	//TODO - is this efficient? or even correct?
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == ForgeCapabilities.FLUID_HANDLER) {
			return LazyOptional.of(() -> this.tankManager).cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerBottler(windowId, player.getInventory(), this);
	}
}
