package forestry.apiculture.blocks;

import forestry.core.blocks.IBlockType;
import forestry.core.blocks.IMachineProperties;
import forestry.core.blocks.MachineProperties;
import forestry.core.features.CoreTiles;
import forestry.core.tiles.TileNaturalistChest;
import forestry.modules.features.FeatureTileType;

public enum NaturalistChestBlockType implements IBlockType {
	APIARIST_CHEST("bee_chest", CoreTiles.APIARIST_CHEST),
	ARBORIST_CHEST("tree_chest", CoreTiles.ARBORIST_CHEST),
	LEPIDOPTERIST_CHEST("butterfly_chest", CoreTiles.LEPIDOPTERIST_CHEST);

	private final MachineProperties<?> machineProperties;

	NaturalistChestBlockType(String name, FeatureTileType<? extends TileNaturalistChest> tileType) {
		this.machineProperties = new MachineProperties.Builder<>(tileType, name)
			.setClientTicker(TileNaturalistChest::clientTick)
			.setShape(TileNaturalistChest.CHEST_SHAPE)
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
