package forestry.core.content.machines.blocks;

import forestry.core.platform.block.IBlockType;
import forestry.core.platform.block.IMachineProperties;
import forestry.core.platform.block.MachineProperties;
import forestry.core.platform.tile.IForestryTicker;
import forestry.core.platform.tile.ForestryBlockEntity;
import forestry.core.content.machines.features.FactoryBlockEntities;
import forestry.core.content.machines.tiles.*;
import forestry.core.platform.registration.FeatureTileType;

public enum FactoryBlockType implements IBlockType {
	FABRICATOR(FactoryBlockEntities.FABRICATOR, "thermionic_fabricator", IMachineProperties.TankLayout.NONE, FabricatorBlockEntity::serverTick),
	SMELTER(FactoryBlockEntities.SMELTER, "smelter", IMachineProperties.TankLayout.NONE, SmelterBlockEntity::serverTick),
	BOTTLER(FactoryBlockEntities.BOTTLER, "bottler", IMachineProperties.TankLayout.RESOURCE, BottlerBlockEntity::serverTick),
	CARPENTER(FactoryBlockEntities.CARPENTER, "carpenter", IMachineProperties.TankLayout.RESOURCE, CarpenterBlockEntity::serverTick),
	CENTRIFUGE(FactoryBlockEntities.CENTRIFUGE, "centrifuge", IMachineProperties.TankLayout.NONE, CentrifugeBlockEntity::serverTick),
	FERMENTER(FactoryBlockEntities.FERMENTER, "fermenter", IMachineProperties.TankLayout.BOTH, FermenterBlockEntity::serverTick),
	MOISTENER(FactoryBlockEntities.MOISTENER, "moistener", IMachineProperties.TankLayout.RESOURCE, MoistenerBlockEntity::serverTick),
	SQUEEZER(FactoryBlockEntities.SQUEEZER, "squeezer", IMachineProperties.TankLayout.PRODUCT, SqueezerBlockEntity::serverTick),
	STILL(FactoryBlockEntities.STILL, "still", IMachineProperties.TankLayout.BOTH, StillBlockEntity::serverTick);

	private final IMachineProperties<?> machineProperties;

	<T extends ForestryBlockEntity> FactoryBlockType(FeatureTileType<T> teClass, String name, IMachineProperties.TankLayout layout, IForestryTicker<T> serverTicker) {
		this.machineProperties = new MachineProperties.Builder<>(teClass, name)
			.setServerTicker(serverTicker)
			.setTankLayout(layout)
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
