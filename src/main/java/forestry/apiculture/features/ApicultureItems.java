package forestry.apiculture.features;

import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.bees.EnumHoneyComb;
import forestry.apiculture.bees.EnumPollenCluster;
import forestry.apiculture.bees.EnumPropolis;
import forestry.apiculture.bees.ItemAmbrosia;
import forestry.apiculture.apiarist.ItemArmorApiarist;
import forestry.apiculture.bees.ForestryBeeItem;
import forestry.apiculture.apiary.CreativeHiveFrameItem;
import forestry.apiculture.apiary.HiveFrameItem;
import forestry.apiculture.bees.ItemHoneyComb;
import forestry.apiculture.bees.PollenClusterItem;
import forestry.apiculture.bees.PropolisItem;
import forestry.apiculture.apiarist.ItemSmoker;
import forestry.core.platform.item.ItemForestry;
import forestry.core.platform.item.ItemForestryFood;
import forestry.core.platform.registration.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

@FeatureProvider
public class ApicultureItems {
	// / BEES
	public static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	public static final FeatureItem<ForestryBeeItem> BEE_QUEEN = REGISTRY.item(() -> new ForestryBeeItem(BeeLifeStage.QUEEN), BeeLifeStage.QUEEN.itemId().getPath());
	public static final FeatureItem<ForestryBeeItem> BEE_DRONE = REGISTRY.item(() -> new ForestryBeeItem(BeeLifeStage.DRONE), BeeLifeStage.DRONE.itemId().getPath());
	public static final FeatureItem<ForestryBeeItem> BEE_PRINCESS = REGISTRY.item(() -> new ForestryBeeItem(BeeLifeStage.PRINCESS), BeeLifeStage.PRINCESS.itemId().getPath());
	public static final FeatureItem<ForestryBeeItem> BEE_LARVAE = REGISTRY.item(() -> new ForestryBeeItem(BeeLifeStage.LARVAE), BeeLifeStage.LARVAE.itemId().getPath());

	// / COMB FRAMES
	public static final FeatureItem<HiveFrameItem> FRAME_UNTREATED = REGISTRY.item(() -> new HiveFrameItem(80, 0.9f), "untreated_frame");
	public static final FeatureItem<HiveFrameItem> FRAME_IMPREGNATED = REGISTRY.item(() -> new HiveFrameItem(240, 0.4f), "impregnated_frame");
	public static final FeatureItem<HiveFrameItem> FRAME_PROVEN = REGISTRY.item(() -> new HiveFrameItem(720, 0.3f), "proven_frame");
	public static final FeatureItem<CreativeHiveFrameItem> FRAME_CREATIVE = REGISTRY.item(CreativeHiveFrameItem::new, "creative_frame");

	// BEE RESOURCES
	public static final FeatureItem<Item> EXPERIENCE_DROP = REGISTRY.item("experience_drop");
	public static final FeatureItem<Item> MAGMATIC_DROP = REGISTRY.item("magmatic_drop");
	public static final FeatureItemGroup<PropolisItem, EnumPropolis> PROPOLIS = REGISTRY
		.itemGroup(PropolisItem::new, EnumPropolis.values())
		.identifier(type -> type == EnumPropolis.NORMAL ? "propolis" : type.getSerializedName() + "_propolis")
		.create();

	public static final FeatureItem<Item> ROYAL_JELLY = REGISTRY.item("royal_jelly");

	public static final FeatureItemGroup<PollenClusterItem, EnumPollenCluster> POLLEN_CLUSTER = REGISTRY
		.itemGroup(PollenClusterItem::new, EnumPollenCluster.values())
		.identifier(type -> type == EnumPollenCluster.NORMAL ? "pollen_cluster" : type.getSerializedName() + "_pollen_cluster")
		.create();
	public static final FeatureItemGroup<ItemHoneyComb, EnumHoneyComb> BEE_COMBS = REGISTRY
		.itemGroup(ItemHoneyComb::new, EnumHoneyComb.VALUES)
		.identifierSuffix("comb")
		.create();

	// / BEE FOOD PRODUCTS
	public static final FeatureItem<ItemForestryFood> HONEYED_SLICE = REGISTRY.item(() -> new ItemForestryFood(8, 0.6f), "honeyed_slice");
	public static final FeatureItem<ItemForestryFood> AMBROSIA = REGISTRY.item(() -> new ItemAmbrosia().setIsDrink(), "ambrosia");

	// / APIARIST'S CLOTHES
	public static final FeatureItem<ItemArmorApiarist> APIARIST_HELMET = REGISTRY.item(() -> new ItemArmorApiarist(ArmorItem.Type.HELMET), "apiarists_hat");
	public static final FeatureItem<ItemArmorApiarist> APIARIST_CHEST = REGISTRY.item(() -> new ItemArmorApiarist(ArmorItem.Type.CHESTPLATE), "apiarists_shirt");
	public static final FeatureItem<ItemArmorApiarist> APIARIST_LEGS = REGISTRY.item(() -> new ItemArmorApiarist(ArmorItem.Type.LEGGINGS), "apiarists_pants");
	public static final FeatureItem<ItemArmorApiarist> APIARIST_BOOTS = REGISTRY.item(() -> new ItemArmorApiarist(ArmorItem.Type.BOOTS), "apiarists_shoes");

	// TOOLS
	public static final FeatureItem<ItemSmoker> SMOKER = REGISTRY.item(ItemSmoker::new, "bee_smoker");

	// MISC
	public static final FeatureItem<ItemForestry> AMBER_DRONE = REGISTRY.item(ItemForestry::new, "amber_drone_fossil");
}
