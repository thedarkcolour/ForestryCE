package forestry.core.content.energy.blocks;

import forestry.core.platform.block.IBlockType;
import forestry.core.platform.block.IMachineProperties;
import forestry.core.platform.block.MachineProperties;
import forestry.core.content.energy.features.EnergyBlockEntities;
import forestry.core.content.energy.tiles.EngineBlockEntity;
import forestry.core.platform.registration.FeatureTileType;

public enum EngineBlockType implements IBlockType {
	PEAT(createEngineProperties(EnergyBlockEntities.PEAT_ENGINE, "peat")),
	BIOGAS(createEngineProperties(EnergyBlockEntities.BIOGAS_ENGINE, "biogas")),
	CLOCKWORK(createEngineProperties(EnergyBlockEntities.CLOCKWORK_ENGINE, "clockwork")),
	COMBUSTION(createEngineProperties(EnergyBlockEntities.COMBUSTION_ENGINE, "combustion")),
	SOLAR(createEngineProperties(EnergyBlockEntities.SOLAR_ENGINE, "solar"));

	public static final EngineBlockType[] VALUES = values();

	private final IMachineProperties<?> machineProperties;

	EngineBlockType(IMachineProperties<?> machineProperties) {
		this.machineProperties = machineProperties;
	}

	private static <T extends EngineBlockEntity> IMachineProperties<T> createEngineProperties(FeatureTileType<T> teClass, String name) {
		return new MachineProperties.Builder<>(teClass, name)
			.setClientTicker(EngineBlockEntity::clientTick)
			.setServerTicker(EngineBlockEntity::serverTick)
			.create();
	}

	@Override
	public IMachineProperties<?> getMachineProperties() {
		return this.machineProperties;
	}

	@Override
	public String getSerializedName() {
		return getMachineProperties().getSerializedName();
	}
}
