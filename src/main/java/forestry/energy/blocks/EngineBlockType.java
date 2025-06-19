package forestry.energy.blocks;

import forestry.core.blocks.IBlockType;
import forestry.core.blocks.IMachineProperties;
import forestry.core.blocks.MachineProperties;
import forestry.energy.features.EnergyTiles;
import forestry.energy.tiles.EngineBlockEntity;
import forestry.modules.features.FeatureTileType;

public enum EngineBlockType implements IBlockType {
	PEAT(createEngineProperties(EnergyTiles.PEAT_ENGINE, "peat")),
	BIOGAS(createEngineProperties(EnergyTiles.BIOGAS_ENGINE, "biogas")),
	CLOCKWORK(createEngineProperties(EnergyTiles.CLOCKWORK_ENGINE, "clockwork"));

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
	public String identifier() {
		return getMachineProperties().identifier();
	}
}
