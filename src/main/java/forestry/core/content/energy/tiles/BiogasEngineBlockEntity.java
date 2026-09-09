package forestry.core.content.energy.tiles;

import forestry.api.ForestryDataMaps;
import forestry.api.IForestryApi;
import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.api.core.circuits.ForestryCircuitSocketTypes;
import forestry.api.core.circuits.ICircuitBoard;
import forestry.api.core.machines.fuels.BiogasEngineFuel;
import forestry.core.content.energy.features.EnergyTiles;
import forestry.core.content.energy.inventory.InventoryEngineBiogas;
import forestry.core.content.energy.menu.BiogasEngineMenu;
import forestry.core.engine.circuits.IEngineUpgradeable;
import forestry.core.engine.circuits.ISocketable;
import forestry.core.platform.config.Constants;
import forestry.core.platform.fluids.*;
import forestry.core.platform.inventory.InventoryAdapter;
import forestry.core.platform.tile.ILiquidTankTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

import static net.neoforged.neoforge.fluids.FluidType.BUCKET_VOLUME;

public class BiogasEngineBlockEntity extends EngineBlockEntity implements WorldlyContainer, ILiquidTankTile, IEngineUpgradeable, ISocketable {
	public static final int ENGINE_BRONZE_HEAT_MAX = 10000;
	private final FilteredTank fuelTank;
	private final FilteredTank heatingTank;
	private final StandardTank burnTank;
	private final TankManager tankManager;
	private float burnTime;
	private final InventoryAdapter sockets = new InventoryAdapter(1, "sockets");
	private float outputMult = 1.0f;
	private int heatBonus = 0;
	private float efficiencyMult = 1.0f;
	private float burnRate = 1.0f;

	private boolean shutdown; // true if the engine is too cold and needs to warm itself up.

	public BiogasEngineBlockEntity(BlockPos pos, BlockState state) {
		super(EnergyTiles.BIOGAS_ENGINE.tileType(), pos, state, "engine.bronze", ENGINE_BRONZE_HEAT_MAX, 60000);

		setInternalInventory(new InventoryEngineBiogas(this));

		this.fuelTank = new FilteredTank(Constants.ENGINE_TANK_CAPACITY).setFilter(ForestryDataMaps.BIOGAS_FUELS);
		this.heatingTank = new FilteredTank(Constants.ENGINE_TANK_CAPACITY, true, false).setFilter(FluidTagFilter.LAVA);
		this.burnTank = new StandardTank(BUCKET_VOLUME, false, false);

		this.tankManager = new TankManager(this, this.fuelTank, this.heatingTank, this.burnTank);
	}

	@Override
	public TankManager getTankManager() {
		return this.tankManager;
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

	@Override
	public void burn() {
		this.currentOutput = 0;

		if (isRedstoneActivated() && (this.fuelTank.getFluidAmount() >= BUCKET_VOLUME || this.burnTank.getFluidAmount() > 0)) {

			double heatStage = getHeatLevel();

			// If we have reached a safe temperature, enable energy transfer
			if (heatStage > 0.25 && this.shutdown) {
				shutdown(false);
			} else if (this.shutdown && canActuallyBurnFluid() && canColdStart()) {
				if (this.heatingTank.getFluidAmount() > 0 && this.heatingTank.getFluidType() == Fluids.LAVA) {
					addHeat(Constants.ENGINE_HEAT_VALUE_LAVA);
					this.heatingTank.drainInternal(1, IFluidHandler.FluidAction.EXECUTE);
				}
			}

			// We need a minimum temperature to generate energy
			if (heatStage > 0.2) {
				if (this.burnTank.getFluidAmount() > 0) {
					FluidStack drained = this.burnTank.drainInternal(1, IFluidHandler.FluidAction.SIMULATE);
					this.currentOutput = (int) (determineFuelValue(drained) * this.outputMult);
					if (this.energyStorage.getMaxEnergyStored() - this.energyStorage.getEnergyStored() >= this.currentOutput) {
						this.burnTime -= this.burnRate;
						setChanged();
						this.energyStorage.generateEnergy(this.currentOutput);
						drained.setAmount((int) this.burnTime);
						this.burnTank.setFluid(drained);
						this.level.updateNeighbourForOutputSignal(this.worldPosition, getBlockState().getBlock());
					} else {
						this.currentOutput = 0;
					}
				} else {
					FluidStack fuel = this.fuelTank.drainInternal(BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
					int time = determineBurnTime(fuel);
					if (!fuel.isEmpty()) {
						fuel.setAmount(time);
						this.burnTime = time;
					}
					this.burnTank.setCapacity(time);
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

	private boolean canActuallyBurnFluid() {
		if (this.heatBonus == 0) {
			return true;
		}
		FluidStack fuel = this.burnTank.getFluidAmount() > 0 ? this.burnTank.getFluid() : this.fuelTank.getFluid();
		if (!fuel.isEmpty()) {
			BiogasEngineFuel fuel2 = fuel.getFluidHolder().getData(ForestryDataMaps.BIOGAS_FUELS);
			return fuel2 != null && fuel2.dissipationMultiplier() == 1;
		}
		return false;
	}

	private boolean canColdStart() {
		return (this.heat > 0 && this.energyStorage.getEnergyStored() < this.energyStorage.getMaxEnergyStored()) || this.energyStorage.getEnergyStored() == 0;
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
		if (this.burnTank.getFluidAmount() > 0 && this.currentOutput > 0) {
			FluidStack fuelFluidStack = this.burnTank.getFluid();
			if (!fuelFluidStack.isEmpty()) {
				BiogasEngineFuel fuel = fuelFluidStack.getFluidHolder().getData(ForestryDataMaps.BIOGAS_FUELS);
				if (fuel != null) {
					loss = loss * (fuel.dissipationMultiplier() + this.heatBonus);
				}
			}
		}

		this.heat -= loss;
	}

	@Override
	public void generateHeat() {

		if (isBurning()) {
			this.heat++;
			if (getHeatLevel() > 0.24) {
				this.heat++;
			}
		}

	}

	/**
	 * Returns the fuel value (power per cycle) an item of the passed fluid
	 */
	private static int determineFuelValue(@Nullable FluidStack fluidStack) {
		if (fluidStack != null) {
			BiogasEngineFuel fuel = fluidStack.getFluidHolder().getData(ForestryDataMaps.BIOGAS_FUELS);
			if (fuel != null) {
				return fuel.powerPerCycle();
			}
		}
		return 0;
	}

	/**
	 * @return Duration of burn cycle of one bucket
	 */
	private static int determineBurnTime(@Nullable FluidStack fluidStack) {
		if (fluidStack != null) {
			BiogasEngineFuel fuel = fluidStack.getFluidHolder().getData(ForestryDataMaps.BIOGAS_FUELS);
			if (fuel != null) {
				return fuel.burnDuration();
			}
		}
		return 0;
	}

	// / STATE INFORMATION
	@Override
	protected boolean isBurning() {
		return mayBurn() && this.burnTank.getFluidAmount() > 0 && (this.level.isClientSide || this.currentOutput != 0);
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

		this.sockets.read(nbt, registries);

		if (nbt.contains("shutdown")) {
			this.shutdown = nbt.getBoolean("shutdown");
		}
		this.burnTime = nbt.getFloat("burnTime");
		this.tankManager.read(nbt, registries);

		ItemStack chip = this.sockets.getItem(0);
		if (!chip.isEmpty()) {
			ICircuitBoard chipset = IForestryApi.INSTANCE.getCircuitManager().getCircuitBoard(chip);
			if (chipset != null) {
				chipset.onLoad(this);
			}
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		this.sockets.write(nbt, registries);

		nbt.putBoolean("shutdown", this.shutdown);
		nbt.putFloat("burnTime", this.burnTime);
		this.tankManager.write(nbt, registries);
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf data) {
		super.writeData(data);
		data.writeBoolean(this.shutdown);
		this.tankManager.writeData(data);
		this.burnTank.writeData(data);
		this.sockets.writeData(data);
	}

	@Override
	public void readData(RegistryFriendlyByteBuf data) {
		super.readData(data);
		this.shutdown = data.readBoolean();
		this.tankManager.readData(data);
		this.burnTank.readData(data);
		this.sockets.readData(data);
	}

	@Nullable
	public IFluidHandler getFluidHandler(@Nullable Direction facing) {
		return this.remove ? null : this.tankManager;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new BiogasEngineMenu(windowId, inv, this);
	}

	@Override
	public void applyEngineUpgrade(float outputBoost, float efficiencyMult, int heat) {
		if (heat > 0) {
			if (this.heatBonus == 0) {
				this.heatBonus = heat;
				this.efficiencyMult += efficiencyMult;
			}
		} else {
			this.efficiencyMult += efficiencyMult;
		}
		if (this.heatBonus != 0) {
			this.outputMult = this.heatBonus > 1 ? 2 : 0.5f;
		} else {
			this.outputMult = 1;
		}
		this.burnRate = this.outputMult / Math.min(1.6f, this.efficiencyMult);
	}

	@Override
	public void removeEngineUpgrade(float outputBoost, float efficiencyMult, int heat) {
		if (heat > 0) {
			if (this.heatBonus == heat) {
				this.heatBonus = 0;
				this.efficiencyMult -= efficiencyMult;
			}
		} else {
			this.efficiencyMult -= efficiencyMult;
		}
		if (this.heatBonus != 0) {
			this.outputMult = this.heatBonus > 1 ? 2 : 0.5f;
		} else {
			this.outputMult = 1;
		}
		this.burnRate = this.outputMult / Math.min(1.6f, this.efficiencyMult);
	}

	@Override
	public int getSocketCount() {
		return this.sockets.getContainerSize();
	}

	@Override
	public ItemStack getSocket(int slot) {
		return this.sockets.getItem(slot);
	}

	@Override
	public void setSocket(int slot, ItemStack stack) {
		if (!stack.isEmpty() && !IForestryApi.INSTANCE.getCircuitManager().isCircuitBoard(stack)) {
			return;
		}

		// Dispose correctly of old chipsets
		if (!this.sockets.getItem(slot).isEmpty()) {
			if (IForestryApi.INSTANCE.getCircuitManager().isCircuitBoard(this.sockets.getItem(slot))) {
				ICircuitBoard chipset = IForestryApi.INSTANCE.getCircuitManager().getCircuitBoard(this.sockets.getItem(slot));
				if (chipset != null) {
					chipset.onRemoval(this);
				}
			}
		}

		this.sockets.setItem(slot, stack);
		// sockets has no tile link of its own
		setChanged();
		if (stack.isEmpty()) {
			return;
		}

		ICircuitBoard chipset = IForestryApi.INSTANCE.getCircuitManager().getCircuitBoard(stack);
		if (chipset != null) {
			chipset.onInsertion(this);
		}
	}

	@Override
	public ResourceLocation getSocketType() {
		return ForestryCircuitSocketTypes.ENGINE;
	}
}
