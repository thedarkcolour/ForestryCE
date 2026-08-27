package forestry.core.content.energy.tiles;

import forestry.api.IForestryApi;
import forestry.api.core.circuits.ForestryCircuitSocketTypes;
import forestry.api.core.circuits.ICircuitBoard;
import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.core.engine.circuits.IEngineUpgradeable;
import forestry.core.engine.circuits.ISocketable;
import forestry.core.platform.config.Constants;
import forestry.core.platform.fluids.*;
import forestry.core.platform.inventory.InventoryAdapter;
import forestry.core.platform.tile.ILiquidTankTile;
import forestry.core.content.energy.features.EnergyTiles;
import forestry.core.content.energy.inventory.InventoryEngineCombustion;
import forestry.core.content.energy.menu.CombustionEngineMenu;
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
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

import static net.neoforged.neoforge.fluids.FluidType.BUCKET_VOLUME;
import forestry.api.ForestryDataMaps;
import forestry.api.core.machines.fuels.BiogasEngineFuel;

public class CombustionEngineBlockEntity extends EngineBlockEntity implements WorldlyContainer, ILiquidTankTile, IEngineUpgradeable, ISocketable {
	private final StandardTank burnTank;
	private final StandardTank waterTank;
	private final FilteredTank fuelTank;
	private final FilteredTank coolantTank;
	private final TankManager tankManager;
	private float burnTime;
	private int coolantTime;
	//upgrades
	private final InventoryAdapter sockets = new InventoryAdapter(1, "sockets");
	private float outputBoost = 1.0f;
	private float outputMult = 1.0f;
	private float efficiencyMult = 1.0f;
	private float burnRate = 1.0f;

	public CombustionEngineBlockEntity(BlockPos pos, BlockState state) {
		// Deviation from 1.20.1: the hint key was "engine_iron" there, which matches no key in
		// hints.properties. Every other engine uses the dotted form
		super(EnergyTiles.COMBUSTION_ENGINE.tileType(), pos, state, "engine.iron", Constants.ENGINE_COPPER_HEAT_MAX, 80000);

		setInternalInventory(new InventoryEngineCombustion(this));

		this.fuelTank = new FilteredTank(Constants.ENGINE_TANK_CAPACITY).setFilter(ForestryDataMaps.COMBUSTION_FUELS);
		this.coolantTank = new FilteredTank(Constants.ENGINE_TANK_CAPACITY, true, false).setFilter(ForestryDataMaps.COMBUSTION_COOLANTS);
		this.burnTank = new StandardTank(BUCKET_VOLUME, false, false);
		this.waterTank = new StandardTank(BUCKET_VOLUME, false, false);
		this.tankManager = new TankManager(this, this.fuelTank, this.coolantTank, this.burnTank, this.waterTank);
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);
		if (!updateOnInterval(20)) {
			return;
		}

		// Check if we have suitable items waiting in the item slot
		FluidHelper.drainContainers(this.tankManager, this, InventoryEngineCombustion.SLOT_CAN);

		IErrorLogic errorLogic = getErrorLogic();

		errorLogic.setCondition(this.coolantTank.isEmpty() && this.waterTank.isEmpty(), ForestryError.NO_COOLANT);

		errorLogic.setCondition(this.burnTank.isEmpty() && this.fuelTank.isEmpty(), ForestryError.NO_FUEL);
	}

	@Override
	protected void dissipateHeat() {
		if (!isBurning() && this.heat > 0)
			this.heat--;
		int heatToCool = 0;
		double heatLevel = getHeatLevel();
		if (heatLevel > 0.2)
			heatToCool++;
		if (heatLevel > 0.25)
			heatToCool++;
		if (heatLevel > 0.45)
			heatToCool += 2;
		if (heatLevel > 0.55)
			heatToCool += 2;
		if (heatLevel > 0.65)
			heatToCool += 2;
		if (heatLevel > 0.75)
			heatToCool += 2;
		if (heatLevel > 0.85)
			heatToCool += 2;

		FluidStack water = this.waterTank.drainInternal(heatToCool, IFluidHandler.FluidAction.EXECUTE);
		this.heat -= water.getAmount();
		if (this.waterTank.isEmpty()) {
			FluidStack fluidStack = this.coolantTank.drainInternal(BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
			if (!fluidStack.isEmpty()) {
				fluidStack.setAmount(determineCoolantTime(fluidStack));
				this.waterTank.setCapacity(fluidStack.getAmount());
				this.waterTank.setFluid(fluidStack);
				if (water.getFluid() != fluidStack.getFluid()) {
					removeEngineUpgrade(0, determineCoolantModifier(water) / 100f, 0);
					applyEngineUpgrade(0, determineCoolantModifier(fluidStack) / 100f, 0);
				}
			}
		}
	}

	@Override
	protected void generateHeat() {
		if (isBurning()) {
			addHeat((int) (2 * this.outputMult));
		}
	}

	@Override
	protected void burn() {

		this.currentOutput = 0;

		if (isRedstoneActivated()) {
			if (this.burnTime > 0 && !this.waterTank.isEmpty()) {
				this.currentOutput = (int) (determineFuelValue(this.burnTank.getFluid()) * this.outputMult);
				if (this.energyStorage.getMaxEnergyStored() - this.energyStorage.getEnergyStored() >= this.currentOutput) {
					this.burnTime -= this.burnRate;
					setChanged();
					this.energyStorage.generateEnergy(this.currentOutput);
					FluidStack fuel = this.burnTank.getFluid();
					fuel.setAmount((int) this.burnTime);
					this.burnTank.setFluid(fuel);
					this.level.updateNeighbourForOutputSignal(this.worldPosition, getBlockState().getBlock());
				} else {
					this.currentOutput = 0;
				}
			} else if (this.fuelTank.getFluidAmount() >= BUCKET_VOLUME) {
				FluidStack fuel = this.fuelTank.drainInternal(BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE).copy();
				int time = determineBurnTime(fuel);
				if (!fuel.isEmpty()) {
					fuel.setAmount(time);
					this.burnTime = time;
				}
				this.burnTank.setCapacity(time);
				this.burnTank.setFluid(fuel);
			}
		}

	}

	@Override
	protected boolean isBurning() {
		return mayBurn() && (this.level.isClientSide || (!this.burnTank.isEmpty() && this.currentOutput != 0));
	}

	/**
	 * Returns the fuel value (power per cycle) an item of the passed fluid
	 */
	private static int determineFuelValue(@Nullable FluidStack fluidStack) {
		if (fluidStack != null) {
			BiogasEngineFuel fuel = fluidStack.getFluidHolder().getData(ForestryDataMaps.COMBUSTION_FUELS);
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
			BiogasEngineFuel fuel = fluidStack.getFluidHolder().getData(ForestryDataMaps.COMBUSTION_FUELS);
			if (fuel != null) {
				return fuel.burnDuration();
			}
		}
		return 0;
	}

	private static int determineCoolantTime(@Nullable FluidStack fluidStack) {
		if (fluidStack != null) {
			BiogasEngineFuel fuel = fluidStack.getFluidHolder().getData(ForestryDataMaps.COMBUSTION_COOLANTS);
			if (fuel != null) {
				return fuel.burnDuration();
			}
		}
		return 0;
	}

	private static int determineCoolantModifier(@Nullable FluidStack fluidStack) {
		if (fluidStack != null) {
			BiogasEngineFuel fuel = fluidStack.getFluidHolder().getData(ForestryDataMaps.COMBUSTION_COOLANTS);
			if (fuel != null) {
				return fuel.dissipationMultiplier();
			}
		}
		return 0;
	}

	public boolean isCrushedIce() {
		return this.waterTank.getFluidType() == ForestryFluids.ICE.getFluid();
	}

	// Deviation from 1.20.1: 1.21.1 block entities save through saveAdditional(CompoundTag, HolderLookup.Provider),
	// and the tank manager and the socket inventory take the registry lookup too.
	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.tankManager.write(nbt, registries);
		nbt.putFloat("burnTime", this.burnTime);
		nbt.putInt("coolantTime", this.coolantTime);
		this.sockets.write(nbt, registries);
	}

	// Deviation from 1.20.1: load(CompoundTag) became loadAdditional(CompoundTag, HolderLookup.Provider).
	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.tankManager.read(nbt, registries);
		this.burnTime = nbt.getFloat("burnTime");
		this.coolantTime = nbt.getInt("coolantTime");
		this.sockets.read(nbt, registries);

		ItemStack chip = this.sockets.getItem(0);
		if (!chip.isEmpty()) {
			ICircuitBoard chipset = IForestryApi.INSTANCE.getCircuitManager().getCircuitBoard(chip);
			if (chipset != null) {
				chipset.onLoad(this);
			}
		}
		if (!this.waterTank.isEmpty())
			applyEngineUpgrade(0, determineCoolantModifier(this.waterTank.getFluid()) / 100f, 0);
	}

	/* NETWORK */
	@Override
	public void writeData(RegistryFriendlyByteBuf data) {
		super.writeData(data);
		this.tankManager.writeData(data);
		this.sockets.writeData(data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(RegistryFriendlyByteBuf data) {
		super.readData(data);
		this.tankManager.readData(data);
		this.sockets.readData(data);
	}

	// Deviation from 1.20.1: the fluid handler is exposed through RegisterCapabilitiesEvent in
	// ModuleEnergy instead of an overridden getCapability.
	@Nullable
	public IFluidHandler getFluidHandler(@Nullable Direction facing) {
		return this.remove ? null : this.tankManager;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new CombustionEngineMenu(windowId, inv, this);
	}

	@Override
	public TankManager getTankManager() {
		return this.tankManager;
	}

	@Override
	public void applyEngineUpgrade(float outputBoost, float efficiencyMult, int heat) {
		//dont stack multiple chokes
		if (!(outputBoost < 0 && this.outputBoost < 0))
			this.efficiencyMult += efficiencyMult;
		this.outputBoost += outputBoost;
		this.outputMult = Math.max(0.5f, this.outputBoost);
		this.burnRate = this.outputMult / Math.min(1.6f, Math.max(this.efficiencyMult, isCrushedIce() ? 1.0f : 0.1f));
	}

	@Override
	public void removeEngineUpgrade(float outputBoost, float efficiencyMult, int heat) {
		//dont unstack multiple chokes
		if (!(outputBoost < 0 && this.outputBoost < outputBoost))
			this.efficiencyMult -= efficiencyMult;
		this.outputBoost -= outputBoost;
		this.outputMult = this.outputBoost;
		this.burnRate = this.outputBoost / Math.max(this.efficiencyMult, isCrushedIce() ? 1.0f : 0.1f);
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
