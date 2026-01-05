package forestry.energy.tiles;

import forestry.api.core.ForestryError;
import forestry.api.fuels.FuelManager;
import forestry.core.config.Constants;
import forestry.core.features.CoreItems;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.tiles.TemperatureState;
import forestry.energy.features.EnergyTiles;
import forestry.energy.inventory.InventoryEnginePeat;
import forestry.energy.menu.PeatEngineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class PeatEngineBlockEntity extends EngineBlockEntity implements WorldlyContainer {
	private ItemStack fuel = ItemStack.EMPTY;
	private int burnTime;
	private int totalBurnTime;
	private int ashProduction;
	private final int ashForItem;

	public PeatEngineBlockEntity(BlockPos pos, BlockState state) {
		super(EnergyTiles.PEAT_ENGINE.tileType(), pos, state, "engine.copper", Constants.ENGINE_COPPER_HEAT_MAX, 200000);

        this.ashForItem = Constants.ENGINE_COPPER_ASH_FOR_ITEM;
		setInternalInventory(new InventoryEnginePeat(this));
	}

	private int getFuelSlot() {
		IInventoryAdapter inventory = getInternalInventory();
		if (inventory.getItem(InventoryEnginePeat.SLOT_FUEL).isEmpty()) {
			return -1;
		}

		if (determineFuelValue(inventory.getItem(InventoryEnginePeat.SLOT_FUEL)) > 0) {
			return InventoryEnginePeat.SLOT_FUEL;
		}

		return -1;
	}

	private int getFreeWasteSlot() {
		IInventoryAdapter inventory = getInternalInventory();
		for (int i = InventoryEnginePeat.SLOT_WASTE_1; i <= InventoryEnginePeat.SLOT_WASTE_COUNT; i++) {
			ItemStack waste = inventory.getItem(i);
			if (waste.isEmpty()) {
				return i;
			}

			if (!CoreItems.ASH.itemEqual(waste)) {
				continue;
			}

			if (waste.getCount() < waste.getMaxStackSize()) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		if (updateOnInterval(40)) {
			int fuelSlot = getFuelSlot();
			boolean hasFuel = fuelSlot >= 0 && determineBurnDuration(getInternalInventory().getItem(fuelSlot)) > 0;
			getErrorLogic().setCondition(!hasFuel, ForestryError.NO_FUEL);
		}
	}

	@Override
	public void burn() {

        this.currentOutput = 0;

		if (this.burnTime > 0) {
            this.burnTime--;
			addAsh(1);

			if (isRedstoneActivated()) {
                this.currentOutput = determineFuelValue(this.fuel);
                this.energyStorage.generateEnergy(this.currentOutput);
                this.level.updateNeighbourForOutputSignal(this.worldPosition, getBlockState().getBlock());    //TODO - I thuink
			}
		} else if (isRedstoneActivated()) {
			int fuelSlot = getFuelSlot();
			int wasteSlot = getFreeWasteSlot();

			if (fuelSlot >= 0 && wasteSlot >= 0) {
				IInventoryAdapter inventory = getInternalInventory();
				ItemStack fuelStack = inventory.getItem(fuelSlot);
                this.burnTime = this.totalBurnTime = determineBurnDuration(fuelStack);
				if (this.burnTime > 0 && !fuelStack.isEmpty()) {
                    this.fuel = fuelStack.copy();
					removeItem(fuelSlot, 1);
				}
			}
		}
	}

	@Override
	public void dissipateHeat() {
		if (this.heat <= 0) {
			return;
		}

		int loss = 0;

		if (!isBurning()) {
			loss += 1;
		}

		TemperatureState tempState = getTemperatureState();
		if (tempState == TemperatureState.OVERHEATING || tempState == TemperatureState.OPERATING_TEMPERATURE) {
			loss += 1;
		}

        this.heat -= loss;
	}

	@Override
	public void generateHeat() {

		int heatToAdd = 0;

		if (isBurning()) {
			heatToAdd++;
			if ((double) this.energyStorage.getEnergyStored() / (double) this.energyStorage.getMaxEnergyStored() > 0.5) {
				heatToAdd++;
			}
		}

		addHeat(heatToAdd);
	}

	private void addAsh(int amount) {

        this.ashProduction += amount;
		if (this.ashProduction < this.ashForItem) {
			return;
		}

		// If we have reached the necessary amount, we need to add ash
		int wasteSlot = getFreeWasteSlot();
		if (wasteSlot >= 0) {
			IInventoryAdapter inventory = getInternalInventory();
			ItemStack wasteStack = inventory.getItem(wasteSlot);
			if (wasteStack.isEmpty()) {
				inventory.setItem(wasteSlot, CoreItems.ASH.stack());
			} else {
				wasteStack.grow(1);
			}
		}
		// Reset
        this.ashProduction = 0;
	}

	/**
	 * Returns the fuel value (power per cycle) an item of the passed ItemStack provides
	 */
	private static int determineFuelValue(ItemStack fuel) {
		if (FuelManager.peatEngineFuel.containsKey(fuel)) {
			return FuelManager.peatEngineFuel.get(fuel).powerPerCycle();
		} else {
			return 0;
		}
	}

	/**
	 * Returns the fuel value (power per cycle) an item of the passed ItemStack provides
	 */
	private static int determineBurnDuration(ItemStack fuel) {
		if (FuelManager.peatEngineFuel.containsKey(fuel)) {
			return FuelManager.peatEngineFuel.get(fuel).burnDuration();
		} else {
			return 0;
		}
	}

	// / STATE INFORMATION
	@Override
	public boolean isBurning() {
		return mayBurn() && this.burnTime > 0;
	}

	@Override
	public int getBurnTimeRemainingScaled(int i) {
		if (this.totalBurnTime == 0) {
			return 0;
		}

		return this.burnTime * i / this.totalBurnTime;
	}

	@Override
	public boolean hasFuelMin(float percentage) {
		int fuelSlot = this.getFuelSlot();
		if (fuelSlot < 0) {
			return false;
		}

		IInventoryAdapter inventory = getInternalInventory();
		return (float) inventory.getItem(fuelSlot).getCount() / (float) inventory.getItem(fuelSlot).getMaxStackSize() > percentage;
	}

	// / LOADING AND SAVING
	@Override
	public void load(CompoundTag compoundNBT) {
		super.load(compoundNBT);

		if (compoundNBT.contains("EngineFuelItemStack")) {
			CompoundTag fuelItemNbt = compoundNBT.getCompound("EngineFuelItemStack");
            this.fuel = ItemStack.of(fuelItemNbt);
		}

        this.burnTime = compoundNBT.getInt("EngineBurnTime");
        this.totalBurnTime = compoundNBT.getInt("EngineTotalTime");
		if (compoundNBT.contains("AshProduction")) {
            this.ashProduction = compoundNBT.getInt("AshProduction");
		}
	}


	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);

		if (!this.fuel.isEmpty()) {
			nbt.put("EngineFuelItemStack", this.fuel.serializeNBT());
		}

		nbt.putInt("EngineBurnTime", this.burnTime);
		nbt.putInt("EngineTotalTime", this.totalBurnTime);
		nbt.putInt("AshProduction", this.ashProduction);
	}

	@Override
	public void writeGuiData(FriendlyByteBuf data) {
		super.writeGuiData(data);
		data.writeInt(this.burnTime);
		data.writeInt(this.totalBurnTime);
	}

	@Override
	public void readGuiData(FriendlyByteBuf data) {
		super.readGuiData(data);
        this.burnTime = data.readInt();
        this.totalBurnTime = data.readInt();
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory pPlayerInventory, Player pPlayer) {
		return new PeatEngineMenu(windowId, pPlayerInventory, this);
	}
}
