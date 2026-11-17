package forestry.agriculture.minifarm.blocks;

import forestry.core.platform.block.IBlockType;
import forestry.core.platform.block.IMachineProperties;
import forestry.core.platform.block.MachineProperties;
import forestry.agriculture.features.FarmingBlockEntities;
import forestry.agriculture.minifarm.tiles.AbstractMinifarmBlockEntity;
import forestry.core.platform.registration.FeatureTileType;

import java.util.List;

public enum MinifarmBlockType implements IBlockType {
	ARBORETUM(FarmingBlockEntities.ARBORETUM, "arboretum"),
	FARM_CROPS(FarmingBlockEntities.CROPS, "farm_crops"),
	FARM_MUSHROOM(FarmingBlockEntities.MUSHROOM, "farm_mushroom"),
	FARM_GOURD(FarmingBlockEntities.GOURD, "farm_gourd"),
	FARM_NETHER(FarmingBlockEntities.NETHER, "farm_nether"),
	FARM_ENDER(FarmingBlockEntities.ENDER, "farm_ender"),
	PEAT_POG(FarmingBlockEntities.BOG, "peat_bog");

	public static final List<MinifarmBlockType> VALUES = List.of(values());

	private final IMachineProperties<?> machineProperties;

	MinifarmBlockType(FeatureTileType<? extends AbstractMinifarmBlockEntity> teClass, String name) {
		this.machineProperties = new MachineProperties.Builder<>(teClass, name)
			.setServerTicker(AbstractMinifarmBlockEntity::serverTick)
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
