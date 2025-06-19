package forestry.energy.tiles;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.core.network.IStreamableGui;
import forestry.core.network.packets.PacketActiveUpdate;
import forestry.core.tiles.IActivatable;
import forestry.core.tiles.TemperatureState;
import forestry.core.tiles.TileBase;
import forestry.core.utils.NetworkUtil;
import forestry.energy.EnergyHelper;
import forestry.energy.EnergyTransferMode;
import forestry.energy.ForestryEnergyStorage;
import forestry.energy.blocks.EngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class EngineBlockEntity extends TileBase implements IActivatable, IStreamableGui {
	public static final float ENGINE_PISTON_SPEED_MAX = 0.08f;
	private static final int CANT_SEND_ENERGY_TIME = 20;

	private boolean active = false; // Used for smp.
	private int cantSendEnergyCountdown = CANT_SEND_ENERGY_TIME;
	/**
	 * Indicates whether the piston is receding from or approaching the
	 * combustion chamber
	 */
	public int stagePiston = 0;
	/**
	 * Piston speed as supplied by the server
	 */
	public float pistonSpeedServer = 0;

	protected int currentOutput = 0;
	protected int heat;
	protected final int maxHeat;
	protected boolean forceCooldown = false;
	public float progress;
	protected final ForestryEnergyStorage energyStorage;
	private final String hintKey;

	protected EngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, String hintKey, int maxHeat, int maxEnergy) {
		super(type, pos, state);
		this.hintKey = hintKey;
		this.maxHeat = maxHeat;
		this.energyStorage = new ForestryEnergyStorage(2000, maxEnergy, EnergyTransferMode.EXTRACT);
	}

	public String getHintKey() {
		return this.hintKey;
	}

	protected void addHeat(int i) {
		this.heat += i;

		if (this.heat > this.maxHeat) {
			this.heat = this.maxHeat;
		}
	}

	protected abstract void dissipateHeat();

	protected abstract void generateHeat();

	protected boolean mayBurn() {
		return !this.forceCooldown;
	}

	protected abstract void burn();


	@Override
	public void clientTick(Level level, BlockPos pos, BlockState state) {
		if (this.stagePiston != 0) {
			this.progress += this.pistonSpeedServer;

			if (this.progress > 1) {
				this.stagePiston = 0;
				this.progress = 0;
			}
		} else if (this.active) {
			this.stagePiston = 1;
		}
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		TemperatureState energyState = getTemperatureState();
		if (energyState == TemperatureState.MELTING && this.heat > 0) {
			this.forceCooldown = true;
		} else if (this.forceCooldown && this.heat <= 0) {
			this.forceCooldown = false;
		}

		IErrorLogic errorLogic = getErrorLogic();
		errorLogic.setCondition(this.forceCooldown, ForestryError.FORCED_COOLDOWN);

		boolean enabledRedstone = isRedstoneActivated();
		errorLogic.setCondition(!enabledRedstone, ForestryError.NO_REDSTONE);

		// Determine targeted tile
		BlockState blockState = getBlockState();
		Direction facing = blockState.getValue(EngineBlock.VERTICAL_FACING);
		BlockEntity tile = level.getBlockEntity(getBlockPos().relative(facing));

		float newPistonSpeed = getPistonSpeed();
		if (newPistonSpeed != this.pistonSpeedServer) {
			this.pistonSpeedServer = newPistonSpeed;
			sendNetworkUpdate();
		}

		if (this.stagePiston != 0) {
			this.progress += this.pistonSpeedServer;

			EnergyHelper.sendEnergy(this.energyStorage, facing, tile);

			if (this.progress > 0.25 && this.stagePiston == 1) {
				this.stagePiston = 2;
			} else if (this.progress >= 0.5) {
				this.progress = 0;
				this.stagePiston = 0;
			}
		} else if (enabledRedstone && EnergyHelper.isEnergyReceiverOrEngine(facing.getOpposite(), tile)) {
			if (EnergyHelper.canSendEnergy(this.energyStorage, facing, tile)) {
				this.stagePiston = 1; // If we can transfer energy, start running
				setActive(true);
				this.cantSendEnergyCountdown = CANT_SEND_ENERGY_TIME;
			} else {
				if (isActive()) {
					this.cantSendEnergyCountdown--;
					if (this.cantSendEnergyCountdown <= 0) {
						setActive(false);
					}
				}
			}
		} else {
			setActive(false);
		}

		dissipateHeat();
		generateHeat();
		// Now let's fire up the engine:
		if (mayBurn()) {
			burn();
		} else {
			this.energyStorage.drainEnergy(20);
		}
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	@Override
	public void setActive(boolean active) {
		if (this.active == active) {
			return;
		}
		this.active = active;

		if (!this.level.isClientSide) {
			NetworkUtil.sendToPlayersTrackingPos(new PacketActiveUpdate(this), this.worldPosition, (ServerLevel) this.level);
		}
	}

	// STATE INFORMATION
	protected double getHeatLevel() {
		return (double) this.heat / (double) this.maxHeat;
	}

	protected abstract boolean isBurning();

	public int getBurnTimeRemainingScaled(int i) {
		return 0;
	}

	public boolean hasFuelMin(float percentage) {
		return false;
	}

	public int getCurrentOutput() {
		if (isBurning() && isRedstoneActivated()) {
			return this.currentOutput;
		} else {
			return 0;
		}
	}

	public int getHeat() {
		return this.heat;
	}

	public TemperatureState getTemperatureState() {
		return TemperatureState.getState(this.heat, this.maxHeat);
	}

	protected float getPistonSpeed() {
		return switch (getTemperatureState()) {
			case COOL -> 0.03f;
			case WARMED_UP -> 0.04f;
			case OPERATING_TEMPERATURE -> 0.05f;
			case RUNNING_HOT -> 0.06f;
			case OVERHEATING -> 0.07f;
			case MELTING -> ENGINE_PISTON_SPEED_MAX;
			default -> 0;
		};
	}

	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.energyStorage.read(nbt, registries);

		this.heat = nbt.getInt("EngineHeat");

		this.progress = nbt.getFloat("EngineProgress");
		this.forceCooldown = nbt.getBoolean("ForceCooldown");
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.energyStorage.write(nbt, registries);

		nbt.putInt("EngineHeat", this.heat);
		nbt.putFloat("EngineProgress", this.progress);
		nbt.putBoolean("ForceCooldown", this.forceCooldown);
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf buffer) {
		super.writeData(buffer);
		buffer.writeBoolean(this.active);
		buffer.writeInt(this.heat);
		buffer.writeFloat(this.pistonSpeedServer);
		this.energyStorage.writeData(buffer);
	}

	@Override
	public void readData(RegistryFriendlyByteBuf buffer) {
		super.readData(buffer);
		this.active = buffer.readBoolean();
		this.heat = buffer.readInt();
		this.pistonSpeedServer = buffer.readFloat();
		this.energyStorage.readData(buffer);
	}

	@Override
	public void writeGuiData(RegistryFriendlyByteBuf buffer) {
		buffer.writeInt(this.currentOutput);
		buffer.writeInt(this.heat);
		buffer.writeBoolean(this.forceCooldown);
		this.energyStorage.writeData(buffer);
	}

	@Override
	public void readGuiData(RegistryFriendlyByteBuf buffer) {
		this.currentOutput = buffer.readInt();
		this.heat = buffer.readInt();
		this.forceCooldown = buffer.readBoolean();
		this.energyStorage.readData(buffer);
	}

	public ForestryEnergyStorage getEnergyManager() {
		return this.energyStorage;
	}
}
