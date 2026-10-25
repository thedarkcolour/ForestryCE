package forestry.core.content.machines.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.content.machines.blocks.FactoryBlockType;
import forestry.core.content.machines.blocks.SpecialFactoryBlockType;
import forestry.core.content.machines.tiles.*;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.FeatureTileType;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;

@FeatureProvider
public class FactoryBlockEntities {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.FACTORY);

	public static final FeatureTileType<BottlerBlockEntity> BOTTLER = REGISTRY.tile(BottlerBlockEntity::new, "bottler", () -> FactoryBlocks.PLAIN.get(FactoryBlockType.BOTTLER).collect());
	public static final FeatureTileType<CarpenterBlockEntity> CARPENTER = REGISTRY.tile(CarpenterBlockEntity::new, "carpenter", () -> FactoryBlocks.PLAIN.get(FactoryBlockType.CARPENTER).collect());
	public static final FeatureTileType<CentrifugeBlockEntity> CENTRIFUGE = REGISTRY.tile(CentrifugeBlockEntity::new, "centrifuge", () -> FactoryBlocks.PLAIN.get(FactoryBlockType.CENTRIFUGE).collect());
	public static final FeatureTileType<FabricatorBlockEntity> FABRICATOR = REGISTRY.tile(FabricatorBlockEntity::new, "thermionic_fabricator", () -> FactoryBlocks.PLAIN.get(FactoryBlockType.FABRICATOR).collect());
	public static final FeatureTileType<FermenterBlockEntity> FERMENTER = REGISTRY.tile(FermenterBlockEntity::new, "fermenter", () -> FactoryBlocks.PLAIN.get(FactoryBlockType.FERMENTER).collect());
	public static final FeatureTileType<RainmakerBlockEntity> RAINMAKER = REGISTRY.tile(RainmakerBlockEntity::new, "rainmaker", () -> FactoryBlocks.TESR.get(SpecialFactoryBlockType.RAINMAKER).collect());
	public static final FeatureTileType<MoistenerBlockEntity> MOISTENER = REGISTRY.tile(MoistenerBlockEntity::new, "moistener", () -> FactoryBlocks.PLAIN.get(FactoryBlockType.MOISTENER).collect());
	public static final FeatureTileType<SmelterBlockEntity> SMELTER = REGISTRY.tile(SmelterBlockEntity::new, "smelter", () -> FactoryBlocks.PLAIN.get(FactoryBlockType.SMELTER).collect());
	public static final FeatureTileType<SqueezerBlockEntity> SQUEEZER = REGISTRY.tile(SqueezerBlockEntity::new, "squeezer", () -> FactoryBlocks.PLAIN.get(FactoryBlockType.SQUEEZER).collect());
	public static final FeatureTileType<StillBlockEntity> STILL = REGISTRY.tile(StillBlockEntity::new, "still", () -> FactoryBlocks.PLAIN.get(FactoryBlockType.STILL).collect());
}
