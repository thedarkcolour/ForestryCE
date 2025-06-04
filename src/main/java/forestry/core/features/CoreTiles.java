package forestry.core.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.blocks.NaturalistChestBlockType;
import forestry.core.blocks.BlockTypeCoreTesr;
import forestry.core.tiles.*;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.FeatureTileType;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class CoreTiles {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);

	public static final FeatureTileType<TileAnalyzer> ANALYZER = REGISTRY.tile(TileAnalyzer::new, "analyzer", () -> CoreBlocks.BASE.get(BlockTypeCoreTesr.ANALYZER).collect());
	public static final FeatureTileType<TileEscritoire> ESCRITOIRE = REGISTRY.tile(TileEscritoire::new, "escritoire", () -> CoreBlocks.BASE.get(BlockTypeCoreTesr.ESCRITOIRE).collect());
	public static final FeatureTileType<TileApiaristChest> APIARIST_CHEST = REGISTRY.tile(TileApiaristChest::new, "apiarist_chest", () -> CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.APIARIST_CHEST).collect());
	public static final FeatureTileType<TileArboristChest> ARBORIST_CHEST = REGISTRY.tile(TileArboristChest::new, "arborist_chest", () -> CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.ARBORIST_CHEST).collect());
	public static final FeatureTileType<TileLepidopteristChest> LEPIDOPTERIST_CHEST = REGISTRY.tile(TileLepidopteristChest::new, "lepidopterist_chest", () -> CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.LEPIDOPTERIST_CHEST).collect());
}
