package forestry.farming.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.farming.blocks.EnumFarmBlockType;
import forestry.farming.tiles.*;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.FeatureTileType;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class FarmingTiles {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.FARMING);

	public static final FeatureTileType<MultifarmControlBlockEntity> CONTROL = REGISTRY.tile(MultifarmControlBlockEntity::new, "control", () -> FarmingBlocks.FARM.getRowBlocks(EnumFarmBlockType.CONTROL));
	public static final FeatureTileType<MultifarmGearboxBlockEntity> GEARBOX = REGISTRY.tile(MultifarmGearboxBlockEntity::new, "gearbox", () -> FarmingBlocks.FARM.getRowBlocks(EnumFarmBlockType.GEARBOX));
	public static final FeatureTileType<MultifarmHatchBlockEntity> HATCH = REGISTRY.tile(MultifarmHatchBlockEntity::new, "hatch", () -> FarmingBlocks.FARM.getRowBlocks(EnumFarmBlockType.HATCH));
	public static final FeatureTileType<MultifarmBlockEntity> PLAIN = REGISTRY.tile(MultifarmBlockEntity::new, "plain", () -> FarmingBlocks.FARM.getRowBlocks(EnumFarmBlockType.PLAIN));
	public static final FeatureTileType<MultifarmValveBlockEntity> VALVE = REGISTRY.tile(MultifarmValveBlockEntity::new, "valve", () -> FarmingBlocks.FARM.getRowBlocks(EnumFarmBlockType.VALVE));
}
