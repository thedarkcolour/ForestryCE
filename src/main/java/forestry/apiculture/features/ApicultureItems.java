package forestry.apiculture.features;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.core.ItemGroups;
import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.items.EnumHoneyComb;
import forestry.apiculture.items.EnumPollenCluster;
import forestry.apiculture.items.EnumPropolis;
import forestry.apiculture.items.ItemAmbrosia;
import forestry.apiculture.items.ItemArmorApiarist;
import forestry.apiculture.items.ItemBeeGE;
import forestry.apiculture.items.ItemCreativeHiveFrame;
import forestry.apiculture.items.ItemHiveFrame;
import forestry.apiculture.items.ItemHoneyComb;
import forestry.apiculture.items.ItemPollenCluster;
import forestry.apiculture.items.ItemPropolis;
import forestry.apiculture.items.ItemScoop;
import forestry.apiculture.items.ItemSmoker;
import forestry.apiculture.items.ItemWaxCast;
import forestry.core.items.ItemForestry;
import forestry.core.items.ItemForestryFood;
import forestry.core.items.ItemOverlay;
import forestry.modules.features.FeatureItem;
import forestry.modules.features.FeatureItemGroup;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class ApicultureItems {
	// / BEES
	public static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	public static final FeatureItem<ItemBeeGE> BEE_QUEEN = REGISTRY.item(() -> new ItemBeeGE(BeeLifeStage.QUEEN), "bee_queen_ge");
	public static final FeatureItem<ItemBeeGE> BEE_DRONE = REGISTRY.item(() -> new ItemBeeGE(BeeLifeStage.DRONE), "bee_drone_ge");
	public static final FeatureItem<ItemBeeGE> BEE_PRINCESS = REGISTRY.item(() -> new ItemBeeGE(BeeLifeStage.PRINCESS), "bee_princess_ge");
	public static final FeatureItem<ItemBeeGE> BEE_LARVAE = REGISTRY.item(() -> new ItemBeeGE(BeeLifeStage.LARVAE), "bee_larvae_ge");

	// / COMB FRAMES
	public static final FeatureItem<ItemHiveFrame> FRAME_UNTREATED = REGISTRY.item(() -> new ItemHiveFrame(80, 0.9f), "frame_untreated");
	public static final FeatureItem<ItemHiveFrame> FRAME_IMPREGNATED = REGISTRY.item(() -> new ItemHiveFrame(240, 0.4f), "frame_impregnated");
	public static final FeatureItem<ItemHiveFrame> FRAME_PROVEN = REGISTRY.item(() -> new ItemHiveFrame(720, 0.3f), "frame_proven");
	public static final FeatureItem<ItemCreativeHiveFrame> FRAME_CREATIVE = REGISTRY.item(ItemCreativeHiveFrame::new, "frame_creative");

	// BEE RESOURCES
	public static final FeatureItem<Item> HONEY_DROP = REGISTRY.item("honey_drop");
	public static final FeatureItem<Item> HONEYDEW = REGISTRY.item("honeydew");
	public static final FeatureItemGroup<ItemPropolis, EnumPropolis> PROPOLIS = REGISTRY.itemGroup(ItemPropolis::new, "propolis", EnumPropolis.values());

	public static final FeatureItem<Item> ROYAL_JELLY = REGISTRY.item("royal_jelly");

	public static final FeatureItem<ItemWaxCast> WAX_CAST = REGISTRY.item(ItemWaxCast::new, "wax_cast");
	public static final FeatureItemGroup<ItemPollenCluster, EnumPollenCluster> POLLEN_CLUSTER = REGISTRY.itemGroup(ItemPollenCluster::new, "pollen_cluster", EnumPollenCluster.values());
	public static final FeatureItemGroup<ItemHoneyComb, EnumHoneyComb> BEE_COMBS = REGISTRY.itemGroup(ItemHoneyComb::new, "bee_comb", EnumHoneyComb.VALUES);

	// / BEE FOOD PRODUCTS
	public static final FeatureItem<ItemForestryFood> HONEYED_SLICE = REGISTRY.item(() -> new ItemForestryFood(8, 0.6f), "honeyed_slice");
	public static final FeatureItem<ItemForestryFood> AMBROSIA = REGISTRY.item(() -> new ItemAmbrosia().setIsDrink(), "ambrosia");
	public static final FeatureItem<ItemForestryFood> HONEY_POT = REGISTRY.item(() -> new ItemForestryFood(2, 0.2f).setIsDrink(), "honey_pot");

	// / APIARIST'S CLOTHES
	public static final FeatureItem<ItemArmorApiarist> APIARIST_HELMET = REGISTRY.item(() -> new ItemArmorApiarist(ArmorItem.Type.HELMET), "apiarist_helmet");
	public static final FeatureItem<ItemArmorApiarist> APIARIST_CHEST = REGISTRY.item(() -> new ItemArmorApiarist(ArmorItem.Type.CHESTPLATE), "apiarist_chest");
	public static final FeatureItem<ItemArmorApiarist> APIARIST_LEGS = REGISTRY.item(() -> new ItemArmorApiarist(ArmorItem.Type.LEGGINGS), "apiarist_legs");
	public static final FeatureItem<ItemArmorApiarist> APIARIST_BOOTS = REGISTRY.item(() -> new ItemArmorApiarist(ArmorItem.Type.BOOTS), "apiarist_boots");

	// TOOLS
	public static final FeatureItem<ItemScoop> SCOOP = REGISTRY.item(ItemScoop::new, "scoop");
	public static final FeatureItem<ItemSmoker> SMOKER = REGISTRY.item(ItemSmoker::new, "smoker");
}
