package forestry.apiculture.blocks;

import forestry.apiculture.features.ApicultureTiles;
import forestry.apiculture.tiles.TileBeeHousingBase;
import forestry.core.blocks.IBlockType;
import forestry.core.blocks.IMachineProperties;
import forestry.core.blocks.MachineProperties;
import forestry.modules.features.FeatureTileType;

public enum BlockTypeApiculture implements IBlockType {
	BEE_HOUSE(ApicultureTiles.BEE_HOUSE, "bee_house"),
	APIARY(ApicultureTiles.APIARY, "apiary");

	private final IMachineProperties<?> machineProperties;

	<T extends TileBeeHousingBase> BlockTypeApiculture(FeatureTileType<? extends T> teClass, String name) {
		this.machineProperties = new MachineProperties.Builder<>(teClass, name)
			.setClientTicker(TileBeeHousingBase::clientTick)
			.setServerTicker(TileBeeHousingBase::serverTick)
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
