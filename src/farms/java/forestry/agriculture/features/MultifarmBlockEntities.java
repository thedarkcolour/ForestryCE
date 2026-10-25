package forestry.agriculture.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.agriculture.multifarm.blocks.MultifarmBlockType;
import forestry.agriculture.multifarm.tiles.*;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.FeatureTileType;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;

@FeatureProvider
public class MultifarmBlockEntities {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.FARMING);

	public static final FeatureTileType<MultifarmControlBlockEntity> CONTROL = REGISTRY.tile(MultifarmControlBlockEntity::new, "control", () -> MultifarmBlocks.FARM.getRowBlocks(MultifarmBlockType.CONTROL));
	public static final FeatureTileType<MultifarmGearboxBlockEntity> GEARBOX = REGISTRY.tile(MultifarmGearboxBlockEntity::new, "gearbox", () -> MultifarmBlocks.FARM.getRowBlocks(MultifarmBlockType.GEARBOX));
	public static final FeatureTileType<MultifarmHatchBlockEntity> HATCH = REGISTRY.tile(MultifarmHatchBlockEntity::new, "hatch", () -> MultifarmBlocks.FARM.getRowBlocks(MultifarmBlockType.HATCH));
	public static final FeatureTileType<PlainMultifarmBlockEntity> PLAIN = REGISTRY.tile(PlainMultifarmBlockEntity::new, "plain", () -> MultifarmBlocks.FARM.getRowBlocks(MultifarmBlockType.PLAIN));
	public static final FeatureTileType<MultifarmValveBlockEntity> VALVE = REGISTRY.tile(MultifarmValveBlockEntity::new, "valve", () -> MultifarmBlocks.FARM.getRowBlocks(MultifarmBlockType.VALVE));
}
