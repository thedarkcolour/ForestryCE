package forestry.storage.features;

import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.modules.ForestryModuleIds;
import forestry.storage.items.EnumBackpackType;
import forestry.modules.features.FeatureItem;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import forestry.storage.ModuleStorage;
import forestry.storage.items.ItemBackpack;
import forestry.storage.items.ItemBackpackNaturalist;

@FeatureProvider
public class BackpackItems {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.STORAGE);

	public static final FeatureItem<?> APIARIST_BACKPACK = REGISTRY.item(() -> new ItemBackpackNaturalist(ForestrySpeciesTypes.BEE, ModuleStorage.APIARIST), "apiarist_bag");
	public static final FeatureItem<?> ARBORIST_BACKPACK = REGISTRY.item(() -> new ItemBackpackNaturalist(ForestrySpeciesTypes.TREE, ModuleStorage.ARBORIST), "arborist_bag");
	public static final FeatureItem<?> LEPIDOPTERIST_BACKPACK = REGISTRY.item(() -> new ItemBackpackNaturalist(ForestrySpeciesTypes.BUTTERFLY, ModuleStorage.LEPIDOPTERIST), "lepidopterist_bag");

	public static final FeatureItem<?> MINER_BACKPACK = REGISTRY.item(() -> new ItemBackpack(ModuleStorage.MINER, EnumBackpackType.NORMAL), "miner_bag");
	public static final FeatureItem<?> MINER_BACKPACK_T_2 = REGISTRY.item(() -> new ItemBackpack(ModuleStorage.MINER, EnumBackpackType.WOVEN), "miner_bag_woven");
	public static final FeatureItem<?> DIGGER_BACKPACK = REGISTRY.item(() -> new ItemBackpack(ModuleStorage.DIGGER, EnumBackpackType.NORMAL), "digger_bag");
	public static final FeatureItem<?> DIGGER_BACKPACK_T_2 = REGISTRY.item(() -> new ItemBackpack(ModuleStorage.DIGGER, EnumBackpackType.WOVEN), "digger_bag_woven");
	public static final FeatureItem<?> FORESTER_BACKPACK = REGISTRY.item(() -> new ItemBackpack(ModuleStorage.FORESTER, EnumBackpackType.NORMAL), "forester_bag");
	public static final FeatureItem<?> FORESTER_BACKPACK_T_2 = REGISTRY.item(() -> new ItemBackpack(ModuleStorage.FORESTER, EnumBackpackType.WOVEN), "forester_bag_woven");
	public static final FeatureItem<?> HUNTER_BACKPACK = REGISTRY.item(() -> new ItemBackpack(ModuleStorage.HUNTER, EnumBackpackType.NORMAL), "hunter_bag");
	public static final FeatureItem<?> HUNTER_BACKPACK_T_2 = REGISTRY.item(() -> new ItemBackpack(ModuleStorage.HUNTER, EnumBackpackType.WOVEN), "hunter_bag_woven");
	public static final FeatureItem<?> ADVENTURER_BACKPACK = REGISTRY.item(() -> new ItemBackpack(ModuleStorage.ADVENTURER, EnumBackpackType.NORMAL), "adventurer_bag");
	public static final FeatureItem<?> ADVENTURER_BACKPACK_T_2 = REGISTRY.item(() -> new ItemBackpack(ModuleStorage.ADVENTURER, EnumBackpackType.WOVEN), "adventurer_bag_woven");
	public static final FeatureItem<?> BUILDER_BACKPACK = REGISTRY.item(() -> new ItemBackpack(ModuleStorage.BUILDER, EnumBackpackType.NORMAL), "builder_bag");
	public static final FeatureItem<?> BUILDER_BACKPACK_T_2 = REGISTRY.item(() -> new ItemBackpack(ModuleStorage.BUILDER, EnumBackpackType.WOVEN), "builder_bag_woven");
}
