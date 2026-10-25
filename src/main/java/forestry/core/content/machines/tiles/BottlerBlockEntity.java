package forestry.core.content.machines.tiles;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.core.platform.config.Constants;
import forestry.core.platform.fluids.FluidHelper;
import forestry.core.platform.fluids.FluidHelper.FillStatus;
import forestry.core.platform.fluids.StandardTank;
import forestry.core.platform.fluids.TankManager;
import forestry.api.core.IInventoryAdapter;
import forestry.core.platform.inventory.watchers.ISlotPickupWatcher;
import forestry.core.platform.render.TankRenderInfo;
import forestry.core.platform.tile.ILiquidTankTile;
import forestry.core.platform.tile.PoweredBlockEntity;
import forestry.core.content.machines.blocks.FactoryBlock;
import forestry.core.content.machines.features.FactoryBlockEntities;
import forestry.core.content.machines.gui.BottlerMenu;
import forestry.core.content.machines.inventory.BottlerInventory;
import forestry.core.content.machines.recipes.BottlerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Optional;

public class BottlerBlockEntity extends PoweredBlockEntity implements WorldlyContainer, ILiquidTankTile, ISlotPickupWatcher {
	private static final int STEPS_PER_RECIPE_TIME = 5;
	private static final int ENERGY_PER_RECIPE_TIME = 1000;

	private final StandardTank resourceTank;
	private final TankManager tankManager;

	private final EnumMap<Direction, Boolean> canDump;
	private boolean dumpingFluid = false;
	@Nullable
	private BottlerRecipe currentRecipe;
	@OnlyIn(Dist.CLIENT)
	public boolean isFillRecipe;

	public BottlerBlockEntity(BlockPos pos, BlockState state) {
		super(FactoryBlockEntities.BOTTLER.tileType(), pos, state, 1100, 40000);

		setInternalInventory(new BottlerInventory(this));

        this.resourceTank = new StandardTank(Constants.PROCESSOR_TANK_CAPACITY);
        this.tankManager = new TankManager(this, this.resourceTank);

        this.canDump = new EnumMap<>(Direction.class);
	}

	/* SAVING & LOADING */

	@Override
	public void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.saveAdditional(compound, registries);
        this.tankManager.write(compound, registries);
	}

	@Override
	public void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.loadAdditional(compound, registries);
        this.tankManager.read(compound, registries);
		checkEmptyRecipe();
		checkFillRecipe();
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf data) {
		super.writeData(data);
        this.tankManager.writeData(data);
	}

	@Override
	public void readData(RegistryFriendlyByteBuf data) {
		super.readData(data);
        this.tankManager.readData(data);
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		if (updateOnInterval(20)) {
			ItemStack leftProcessingStack = getItem(BottlerInventory.SLOT_EMPTYING_PROCESSING);
			ItemStack rightProcessingStack = getItem(BottlerInventory.SLOT_FILLING_PROCESSING);
			if (leftProcessingStack.isEmpty()) {
				ItemStack inputStack = getItem(BottlerInventory.SLOT_INPUT_FULL_CONTAINER);
				if (!inputStack.isEmpty()) {
					leftProcessingStack = removeItem(BottlerInventory.SLOT_INPUT_FULL_CONTAINER, 1);
					setItem(BottlerInventory.SLOT_EMPTYING_PROCESSING, leftProcessingStack);
				}
			}
			if (rightProcessingStack.isEmpty()) {
				ItemStack inputStack = getItem(BottlerInventory.SLOT_INPUT_EMPTY_CONTAINER);
				if (!inputStack.isEmpty()) {
					rightProcessingStack = removeItem(BottlerInventory.SLOT_INPUT_EMPTY_CONTAINER, 1);
					setItem(BottlerInventory.SLOT_FILLING_PROCESSING, rightProcessingStack);
				}
			}

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
					Optional<IFluidHandler> fluidDestination = FluidUtil.getFluidHandler(this.level, this.worldPosition.relative(facing), facing.getOpposite());

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
				status = FluidHelper.fillContainers(this.tankManager, this, BottlerInventory.SLOT_FILLING_PROCESSING, BottlerInventory.SLOT_OUTPUT_FULL_CONTAINER, this.currentRecipe.fluid.getFluid(), true);
			} else {
				status = FluidHelper.drainContainers(this.tankManager, this, BottlerInventory.SLOT_EMPTYING_PROCESSING, BottlerInventory.SLOT_OUTPUT_EMPTY_CONTAINER, true);
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
		ItemStack emptyCan = getItem(BottlerInventory.SLOT_FILLING_PROCESSING);
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

					setStepsPerWorkCycle(Math.round(fillTime * STEPS_PER_RECIPE_TIME));
					setEnergyPerWorkCycle(Math.round(fillTime * ENERGY_PER_RECIPE_TIME));
				}
			}
		}
	}

	private void checkEmptyRecipe() {
		ItemStack filledCan = getItem(BottlerInventory.SLOT_EMPTYING_PROCESSING);
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

					setStepsPerWorkCycle(Math.round(fillTime * STEPS_PER_RECIPE_TIME));
					setEnergyPerWorkCycle(0);
				}
			}
		}
	}

	@Override
	public void onTake(int slotIndex, Player player) {
		if (slotIndex == BottlerInventory.SLOT_EMPTYING_PROCESSING) {
			if (this.currentRecipe != null && !this.currentRecipe.fillRecipe) {
                this.currentRecipe = null;
				setStepsPerWorkCycle(0);
			}
		} else if (slotIndex == BottlerInventory.SLOT_FILLING_PROCESSING) {
			if (this.currentRecipe != null && this.currentRecipe.fillRecipe) {
                this.currentRecipe = null;
				setStepsPerWorkCycle(0);
			}
		}
	}

	@Override
	public void writeGuiData(RegistryFriendlyByteBuf data) {
		super.writeGuiData(data);
		if (this.currentRecipe == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(this.currentRecipe.fillRecipe);
		}
	}

	@Override
	public void readGuiData(RegistryFriendlyByteBuf data) {
		super.readGuiData(data);
        this.isFillRecipe = data.readBoolean();
	}

	@Override
	public boolean hasResourcesMin(float percentage) {
		IInventoryAdapter inventory = getInternalInventory();
		ItemStack emptyCan = inventory.getItem(BottlerInventory.SLOT_FILLING_PROCESSING);
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
				emptyStatus = FluidHelper.drainContainers(this.tankManager, this, BottlerInventory.SLOT_EMPTYING_PROCESSING, BottlerInventory.SLOT_OUTPUT_EMPTY_CONTAINER, false);
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
				fillStatus = FluidHelper.fillContainers(this.tankManager, this, BottlerInventory.SLOT_FILLING_PROCESSING, BottlerInventory.SLOT_OUTPUT_FULL_CONTAINER, this.currentRecipe.fluid.getFluid(), false);
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

	public IFluidHandler getFluidHandler(@Nullable Direction facing) {
		return this.tankManager;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new BottlerMenu(windowId, player.getInventory(), this);
	}
}
