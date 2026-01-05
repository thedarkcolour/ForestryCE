package forestry.factory.blocks;

import forestry.core.blocks.IBlockType;
import forestry.core.blocks.IMachineProperties;
import forestry.core.blocks.MachineProperties;
import forestry.core.tiles.IForestryTicker;
import forestry.core.tiles.TileForestry;
import forestry.factory.features.FactoryTiles;
import forestry.factory.tiles.TileFabricator;
import forestry.factory.tiles.TileRaintank;
import forestry.modules.features.FeatureTileType;

public enum BlockTypeFactoryPlain implements IBlockType {
	FABRICATOR(FactoryTiles.FABRICATOR, "fabricator", TileFabricator::serverTick),
	RAINTANK(FactoryTiles.RAIN_TANK, "raintank", TileRaintank::serverTick);

	private final IMachineProperties<?> machineProperties;

	<T extends TileForestry> BlockTypeFactoryPlain(FeatureTileType<T> teClass, String name, IForestryTicker<T> serverTicker) {
		this.machineProperties = new MachineProperties.Builder<>(teClass, name)
			.setServerTicker(serverTicker)
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
