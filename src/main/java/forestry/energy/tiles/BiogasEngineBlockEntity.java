package forestry.energy.tiles;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.api.recipes.IBiogasFuel;
import forestry.core.config.Constants;
import forestry.core.fluids.*;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.utils.RecipeUtils;
import forestry.energy.features.EnergyTiles;
import forestry.energy.inventory.InventoryEngineBiogas;
import forestry.energy.menu.BiogasEngineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

import static net.neoforged.neoforge.fluids.FluidType.BUCKET_VOLUME;

public class BiogasEngineBlockEntity extends EngineBlockEntity implements WorldlyContainer, ILiquidTankTile {
	public static final int ENGINE_BRONZE_HEAT_MAX = 10000;
	public static final int ENGINE_BRONZE_HEAT_GENERATION_ENERGY = 1;
	private final FilteredTank fuelTank;
	private final FilteredTank heatingTank;
	private final StandardTank burnTank;
	private final TankManager tankManager;

	private boolean shutdown; // true if the engine is too cold and needs to warm itself up.

	public BiogasEngineBlockEntity(BlockPos pos, BlockState state) {
		super(EnergyTiles.BIOGAS_ENGINE.tileType(), pos, state, "engine.bronze", ENGINE_BRONZE_HEAT_MAX, 300000);

		setInternalInventory(new InventoryEngineBiogas(this));

		this.fuelTank = new FilteredTank(Constants.ENGINE_TANK_CAPACITY).setFilter(FluidRecipeFilter.BIOGAS_FUEL);
		this.heatingTank = new FilteredTank(Constants.ENGINE_TANK_CAPACITY, true, false).setFilter(FluidTagFilter.LAVA);
		this.burnTank = new StandardTank(BUCKET_VOLUME, false, false);

		this.tankManager = new TankManager(this, this.fuelTank, this.heatingTank, this.burnTank);
	}

	@Override
	public TankManager getTankManager() {
		return this.tankManager;
	}

	@Nullable
	public Fluid getBurnTankFluidType() {
		return this.burnTank.getFluidType();
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);
		if (!updateOnInterval(20)) {
			return;
		}

		// Check if we have suitable items waiting in the item slot
		FluidHelper.drainContainers(this.tankManager, this, InventoryEngineBiogas.SLOT_CAN);

		IErrorLogic errorLogic = getErrorLogic();

		boolean hasHeat = getHeatLevel() > 0.2 || this.heatingTank.getFluidAmount() > 0;
		errorLogic.setCondition(!hasHeat, ForestryError.NO_HEAT);

		boolean hasFuel = this.burnTank.getFluidAmount() > 0 || this.fuelTank.getFluidAmount() > 0;
		errorLogic.setCondition(!hasFuel, ForestryError.NO_FUEL);
	}

	/**
	 * Burns fuel increasing stored energy
	 */
	@Override
	public void burn() {

        this.currentOutput = 0;

		if (isRedstoneActivated() && (this.fuelTank.getFluidAmount() >= BUCKET_VOLUME || this.burnTank.getFluidAmount() > 0)) {

			double heatStage = getHeatLevel();

			// If we have reached a safe temperature, enable energy transfer
			if (heatStage > 0.25 && this.shutdown) {
				shutdown(false);
			} else if (this.shutdown) {
				if (this.heatingTank.getFluidAmount() > 0 && this.heatingTank.getFluidType() == Fluids.LAVA) {
					addHeat(Constants.ENGINE_HEAT_VALUE_LAVA);
                    this.heatingTank.drainInternal(1, IFluidHandler.FluidAction.EXECUTE);
				}
			}

			// We need a minimum temperature to generate energy
			if (heatStage > 0.2) {
				if (this.burnTank.getFluidAmount() > 0) {
					FluidStack drained = this.burnTank.drainInternal(1, IFluidHandler.FluidAction.EXECUTE);
                    this.currentOutput = determineFuelValue(drained);
                    this.energyStorage.generateEnergy(this.currentOutput);
                    this.level.updateNeighbourForOutputSignal(this.worldPosition, getBlockState().getBlock());
				} else {
					FluidStack fuel = this.fuelTank.drainInternal(BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
					int burnTime = determineBurnTime(fuel);
					if (!fuel.isEmpty()) {
						fuel.setAmount(burnTime);
					}
                    this.burnTank.setCapacity(burnTime);
                    this.burnTank.setFluid(fuel);
				}
			} else {
				shutdown(true);
			}
		}
	}

	private void shutdown(boolean val) {
        this.shutdown = val;
	}

	@Override
	public void dissipateHeat() {
		if (this.heat <= 0) {
			return;
		}

		int loss = 1; // Basic loss even when running

		if (!isBurning()) {
			loss++;
		}

		double heatStage = getHeatLevel();
		if (heatStage > 0.55) {
			loss++;
		}

		// Lose extra heat when using water as fuel.
		if (this.fuelTank.getFluidAmount() > 0) {
			FluidStack fuelFluidStack = this.fuelTank.getFluid();
			if (!fuelFluidStack.isEmpty()) {
				IBiogasFuel fuel = getFuel(fuelFluidStack);
				if (fuel != null) {
					loss = loss * fuel.getDissipationMultiplier();
				}
			}
		}

        this.heat -= loss;
	}

	@Override
	public void generateHeat() {

		int generate = 0;

		if (isRedstoneActivated() && this.burnTank.getFluidAmount() > 0) {
			double heatStage = getHeatLevel();
			if (heatStage >= 0.75) {
				generate += ENGINE_BRONZE_HEAT_GENERATION_ENERGY * 3;
			} else if (heatStage > 0.24) {
				generate += ENGINE_BRONZE_HEAT_GENERATION_ENERGY * 2;
			} else if (heatStage > 0.2) {
				generate += ENGINE_BRONZE_HEAT_GENERATION_ENERGY;
			}
		}

        this.heat += generate;

	}

	/**
	 * @return The biogas fuel recipe matching the given fluid, or {@code null} if it is not a valid fuel.
	 */
	@Nullable
	private static IBiogasFuel getFuel(@Nullable FluidStack fluidStack) {
		if (fluidStack == null || fluidStack.isEmpty()) {
			return null;
		}
		RecipeManager manager = RecipeUtils.getRecipeManager();
		return manager == null ? null : RecipeUtils.getBiogasFuel(manager, fluidStack.getFluid());
	}

	/**
	 * Returns the fuel value (power per cycle) an item of the passed fluid
	 */
	private static int determineFuelValue(@Nullable FluidStack fluidStack) {
		IBiogasFuel fuel = getFuel(fluidStack);
		return fuel == null ? 0 : fuel.getPowerPerCycle();
	}

	/**
	 * @return Duration of burn cycle of one bucket
	 */
	private static int determineBurnTime(@Nullable FluidStack fluidStack) {
		IBiogasFuel fuel = getFuel(fluidStack);
		return fuel == null ? 0 : fuel.getBurnDuration();
	}

	// / STATE INFORMATION
	@Override
	protected boolean isBurning() {
		return mayBurn() && this.burnTank.getFluidAmount() > 0;
	}

	@Override
	public int getBurnTimeRemainingScaled(int i) {
		if (this.burnTank.getCapacity() == 0) {
			return 0;
		}

		return this.burnTank.getFluidAmount() * i / this.burnTank.getCapacity();
	}

	public int getOperatingTemperatureScaled(int i) {
		return (int) Math.round(this.heat * i / (this.maxHeat * 0.2));
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		if (nbt.contains("shutdown")) {
            this.shutdown = nbt.getBoolean("shutdown");
		}
        this.tankManager.read(nbt, registries);
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		nbt.putBoolean("shutdown", this.shutdown);
        this.tankManager.write(nbt, registries);
	}

	/* NETWORK */
	@Override
	public void writeData(FriendlyByteBuf data) {
		super.writeData(data);
		data.writeBoolean(this.shutdown);
        this.tankManager.writeData(data);
        this.burnTank.writeData(data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(FriendlyByteBuf data) {
		super.readData(data);
        this.shutdown = data.readBoolean();
        this.tankManager.readData(data);
        this.burnTank.readData(data);
	}

	@Nullable
	public IFluidHandler getFluidHandler(@Nullable Direction facing) {
		return this.remove ? null : this.tankManager;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new BiogasEngineMenu(windowId, inv, this);
	}
}
