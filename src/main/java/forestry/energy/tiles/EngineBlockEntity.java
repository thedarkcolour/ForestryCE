package forestry.energy.tiles;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.core.config.Constants;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public abstract class EngineBlockEntity extends TileBase implements IActivatable, IStreamableGui {
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
	private final LazyOptional<IEnergyStorage> energyCap;
	private final String hintKey;

	protected EngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, String hintKey, int maxHeat, int maxEnergy) {
		super(type, pos, state);
		this.hintKey = hintKey;
		this.maxHeat = maxHeat;
		this.energyStorage = new ForestryEnergyStorage(2000, maxEnergy, EnergyTransferMode.EXTRACT);
		this.energyCap = LazyOptional.of(() -> this.energyStorage);
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
			NetworkUtil.sendNetworkPacket(new PacketActiveUpdate(this), this.worldPosition, this.level);
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

	/**
	 * Returns the current energy state of the engine
	 */
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
			case MELTING -> Constants.ENGINE_PISTON_SPEED_MAX;
			default -> 0;
		};
	}

	/* SAVING & LOADING */
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
        this.energyStorage.read(nbt);

        this.heat = nbt.getInt("EngineHeat");

        this.progress = nbt.getFloat("EngineProgress");
        this.forceCooldown = nbt.getBoolean("ForceCooldown");
	}


	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
        this.energyStorage.write(nbt);

		nbt.putInt("EngineHeat", this.heat);
		nbt.putFloat("EngineProgress", this.progress);
		nbt.putBoolean("ForceCooldown", this.forceCooldown);
	}

	/* NETWORK */
	@Override
	public void writeData(FriendlyByteBuf data) {
		super.writeData(data);
		data.writeBoolean(this.active);
		data.writeInt(this.heat);
		data.writeFloat(this.pistonSpeedServer);
        this.energyStorage.writeData(data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(FriendlyByteBuf data) {
		super.readData(data);
        this.active = data.readBoolean();
        this.heat = data.readInt();
        this.pistonSpeedServer = data.readFloat();
        this.energyStorage.readData(data);
	}

	@Override
	public void writeGuiData(FriendlyByteBuf data) {
		data.writeInt(this.currentOutput);
		data.writeInt(this.heat);
		data.writeBoolean(this.forceCooldown);
        this.energyStorage.writeData(data);
	}

	@Override
	public void readGuiData(FriendlyByteBuf data) {
        this.currentOutput = data.readInt();
        this.heat = data.readInt();
        this.forceCooldown = data.readBoolean();
        this.energyStorage.readData(data);
	}

	public ForestryEnergyStorage getEnergyManager() {
		return this.energyStorage;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
		if (!this.remove && capability == ForgeCapabilities.ENERGY && side == getBlockState().getValue(EngineBlock.VERTICAL_FACING)) {
			return this.energyCap.cast();
		}
		return super.getCapability(capability, side);
	}
}
