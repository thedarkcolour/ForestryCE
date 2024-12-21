package forestry.lepidopterology.features;

import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.modules.ForestryModuleIds;
import forestry.lepidopterology.items.ItemButterflyGE;
import forestry.modules.features.FeatureItem;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class LepidopterologyItems {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.LEPIDOPTEROLOGY);

	public static final FeatureItem<ItemButterflyGE> BUTTERFLY_GE = REGISTRY.item(() -> new ItemButterflyGE(ButterflyLifeStage.BUTTERFLY), "butterfly_ge");
	public static final FeatureItem<ItemButterflyGE> SERUM_GE = REGISTRY.item(() -> new ItemButterflyGE(ButterflyLifeStage.SERUM), "serum_ge");
	public static final FeatureItem<ItemButterflyGE> CATERPILLAR_GE = REGISTRY.item(() -> new ItemButterflyGE(ButterflyLifeStage.CATERPILLAR), "caterpillar_ge");
	public static final FeatureItem<ItemButterflyGE> COCOON_GE = REGISTRY.item(() -> new ItemButterflyGE(ButterflyLifeStage.COCOON), "cocoon_ge");
}
