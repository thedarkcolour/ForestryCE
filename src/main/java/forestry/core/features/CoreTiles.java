package forestry.core.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.platform.block.NaturalistChestBlockType;
import forestry.core.platform.block.CoreMachineBlockType;
import forestry.core.platform.tile.ApiaristChestBlockEntity;
import forestry.core.platform.tile.ArboristChestBlockEntity;
import forestry.core.platform.tile.LepidopteristChestBlockEntity;
import forestry.core.content.escritoire.*;
import forestry.core.content.analyzer.*;
import forestry.core.content.burnbarrel.BurnBarrelBlockEntity;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.FeatureTileType;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;

@FeatureProvider
public class CoreTiles {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);

	public static final FeatureTileType<AnalyzerBlockEntity> ANALYZER = REGISTRY.tile(AnalyzerBlockEntity::new, "analyzer", () -> CoreBlocks.BASE.get(CoreMachineBlockType.ANALYZER).collect());
	public static final FeatureTileType<EscritoireBlockEntity> ESCRITOIRE = REGISTRY.tile(EscritoireBlockEntity::new, "escritoire", () -> CoreBlocks.BASE.get(CoreMachineBlockType.ESCRITOIRE).collect());
	public static final FeatureTileType<ApiaristChestBlockEntity> APIARIST_CHEST = REGISTRY.tile(ApiaristChestBlockEntity::new, "apiarists_chest", () -> CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.APIARIST_CHEST).collect());
	public static final FeatureTileType<ArboristChestBlockEntity> ARBORIST_CHEST = REGISTRY.tile(ArboristChestBlockEntity::new, "arborists_chest", () -> CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.ARBORIST_CHEST).collect());
	public static final FeatureTileType<LepidopteristChestBlockEntity> LEPIDOPTERIST_CHEST = REGISTRY.tile(LepidopteristChestBlockEntity::new, "lepidopterists_chest", () -> CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.LEPIDOPTERIST_CHEST).collect());
	public static final FeatureTileType<BurnBarrelBlockEntity> BURN_BARREL = REGISTRY.tile(BurnBarrelBlockEntity::new, "burn_barrel", () -> CoreBlocks.BURN_BARREL.collect());

}
