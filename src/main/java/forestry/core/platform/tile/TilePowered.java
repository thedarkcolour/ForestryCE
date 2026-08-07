package forestry.core.platform.tile;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.core.engine.circuits.IMachineUpgradable;
import forestry.core.platform.network.IStreamableGui;
import forestry.core.platform.render.TankRenderInfo;
import forestry.core.content.energy.EnergyHelper;
import forestry.core.content.energy.EnergyTransferMode;
import forestry.core.content.energy.ForestryEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

/**
 * Base class for machines that consume energy to do work.
 *
 * <p>Work is measured in "steps", not game ticks. One step happens every {@link #TICKS_PER_STEP} game
 * ticks, and a work cycle takes some number of steps to complete.
 */
public abstract class TilePowered extends TileBase implements IRenderableTile, IMachineUpgradable, IStreamableGui, IPowerHandler {
	// the number of game ticks between two work steps
	private static final int TICKS_PER_STEP = 5;

	private final ForestryEnergyStorage energyStorage;
	protected float speedMultiplier = 1.0f;
	protected float powerMultiplier = 1.0f;
	protected double outputMultiplier = 1.0f;
	// The number of steps into the current work cycle. Between 0 and stepsPerWorkCycle
	private int workCounter;
	// The number of steps a work cycle takes to complete
	private int stepsPerWorkCycle;
	// The amount of energy consumed over the course of an entire work cycle
	private int energyPerWorkCycle;
	// the number of steps that this tile has had no power
	private int noPowerTime = 0;

	protected TilePowered(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxTransfer, int capacity) {
		super(type, pos, state);

		this.energyStorage = new ForestryEnergyStorage(maxTransfer, capacity, EnergyTransferMode.RECEIVE);

		this.stepsPerWorkCycle = 4;
	}

	public ForestryEnergyStorage getEnergyManager() {
		return this.energyStorage;
	}

	public int getWorkCounter() {
		return this.workCounter;
	}

	public void setStepsPerWorkCycle(int stepsPerWorkCycle) {
		this.stepsPerWorkCycle = stepsPerWorkCycle;
		this.workCounter = 0;
	}

	public int getStepsPerWorkCycle() {
		if (this.level.isClientSide) {
			return this.stepsPerWorkCycle;
		}
		return Math.round(this.stepsPerWorkCycle / this.speedMultiplier);
	}

	// energy drawn per step is energyPerWorkCycle / stepsPerWorkCycle
	public void setEnergyPerWorkCycle(int energyPerWorkCycle) {
		this.energyPerWorkCycle = EnergyHelper.scaleForDifficulty(energyPerWorkCycle);
	}

	public int getEnergyPerWorkCycle() {
		return Math.round(this.energyPerWorkCycle * this.powerMultiplier);
	}

	public double getOutputMultiplier() {
		return this.outputMultiplier;
	}

	/* STATE INFORMATION */
	public boolean hasResourcesMin(float percentage) {
		return false;
	}

	public boolean hasFuelMin(float percentage) {
		return false;
	}

	// Called every tick to determine whether the tile can start working or continue working
	public abstract boolean hasWork();

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		if (!updateOnInterval(TICKS_PER_STEP)) {
			return;
		}

		IErrorLogic errorLogic = getErrorLogic();

		boolean disabled = isRedstoneActivated();
		errorLogic.setCondition(disabled, ForestryError.DISABLED_BY_REDSTONE);
		if (disabled) {
			return;
		}

		if (!hasWork()) {
			return;
		}

		int stepsPerWorkCycle = getStepsPerWorkCycle();

		if (this.workCounter < stepsPerWorkCycle) {
			int energyPerWorkCycle = getEnergyPerWorkCycle();
			boolean consumedEnergy = EnergyHelper.consumeEnergyToDoWork(this.energyStorage, stepsPerWorkCycle, energyPerWorkCycle);
			if (consumedEnergy) {
				errorLogic.setCondition(false, ForestryError.NO_POWER);
				this.workCounter++;
				this.noPowerTime = 0;
			} else {
				this.noPowerTime++;
				if (this.noPowerTime > 4) {
					errorLogic.setCondition(true, ForestryError.NO_POWER);
				}
			}
		}

		if (this.workCounter >= stepsPerWorkCycle) {
			if (workCycle()) {
				this.workCounter = 0;
			}
		}
	}

	// Called when the tile reaches the end of a work cycle. Consume inputs and produce outputs here.
	protected abstract boolean workCycle();

	// Returns the width for a progress bar. pixels is the full width of the progress bar.
	public int getProgressScaled(int pixels) {
		int stepsPerWorkCycle = getStepsPerWorkCycle();
		if (stepsPerWorkCycle == 0) {
			return 0;
		}

		return this.workCounter * pixels / stepsPerWorkCycle;
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.energyStorage.write(nbt, registries);
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.energyStorage.read(nbt, registries);
	}

	@Override
	public void writeGuiData(FriendlyByteBuf data) {
		this.energyStorage.writeData(data);
		data.writeVarInt(this.workCounter);
		data.writeVarInt(getStepsPerWorkCycle());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readGuiData(FriendlyByteBuf data) {
		this.energyStorage.readData(data);
		this.workCounter = data.readVarInt();
		this.stepsPerWorkCycle = data.readVarInt();
	}

	/* IMachineUpgradable */
	public void applyMachineUpgrade(double speedChange, double powerChange, double outputChange) {
		this.speedMultiplier += speedChange;
		this.powerMultiplier += powerChange;
		this.outputMultiplier *= outputChange;
		this.workCounter = 0;
	}

	/* IMachineUpgradable */
	@Override
	public void removeMachineUpgrade(double speedChange, double powerChange, double outputChange) {
		this.speedMultiplier -= speedChange;
		this.powerMultiplier -= powerChange;
		this.outputMultiplier /= outputChange;
		this.workCounter = 0;

		if (Float.isNaN((float) this.outputMultiplier)) {
			this.outputMultiplier = 1.0f;
		}
	}

	/* IRenderableTile */
	@Override
	public TankRenderInfo getResourceTankInfo() {
		return TankRenderInfo.EMPTY;
	}

	@Override
	public TankRenderInfo getProductTankInfo() {
		return TankRenderInfo.EMPTY;
	}

	@Nullable
	public IEnergyStorage getEnergyHandler(@Nullable Direction facing) {
		return this.remove ? null : this.energyStorage;
	}
}
