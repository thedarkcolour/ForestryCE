package forestry.core.data;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import forestry.api.ForestryConstants;
import forestry.api.ForestryTags;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.apiculture.features.ApicultureItems;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.core.features.CoreItems;
import forestry.core.loot.OrganismFunction;
import forestry.core.utils.SpeciesUtil;
import forestry.storage.features.BackpackItems;

/**
 * Helper class to handle chest loot.
 * <p>
 * <p>
 * Used by {@link ForestryChestLootTables} and {@link ForestryLootModifierProvider}
 */
public class LootTableHelper {
	@Nullable
	public static LootTableHelper instance;

	public static LootTableHelper getInstance() {
		if (instance == null) {
			instance = new LootTableHelper();
		}
		return instance;
	}

	protected final Multimap<ResourceLocation, Entry> entries = LinkedHashMultimap.create();

	public LootTableHelper() {
		add(BuiltInLootTables.ABANDONED_MINESHAFT, "apiculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_apiculture_bees")
						.setRolls(ConstantValue.exactly(1))
						.add(beeLoot(ForestryBeeSpecies.STEADFAST))
						.add(EmptyLootItem.emptyItem().setWeight(9))
				));
		add(BuiltInLootTables.ABANDONED_MINESHAFT, "factory",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_factory_items")
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(CoreItems.BROKEN_BRONZE_PICKAXE.item()).setWeight(30))
						.add(LootItem.lootTableItem(CoreItems.BROKEN_BRONZE_SHOVEL.item()).setWeight(10))
						.add(LootItem.lootTableItem(CoreItems.KIT_PICKAXE.item()).setWeight(10))
						.add(LootItem.lootTableItem(CoreItems.KIT_SHOVEL.item()).setWeight(5))
						.add(EmptyLootItem.emptyItem().setWeight(50))
				));
		add(BuiltInLootTables.ABANDONED_MINESHAFT, "storage",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_storage_items")
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(BackpackItems.MINER_BACKPACK.item()))
						.add(EmptyLootItem.emptyItem().setWeight(20))));
		add(BuiltInLootTables.DESERT_PYRAMID, "apiculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_apiculture_bees")
						.setRolls(ConstantValue.exactly(1))
						.add(beeLoot(ForestryBeeSpecies.STEADFAST))
						.add(beeLoot(ForestryBeeSpecies.STEADFAST).setWeight(3)
								.apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
						.add(EmptyLootItem.emptyItem().setWeight(6))
				));
		add(BuiltInLootTables.DESERT_PYRAMID, "arboriculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_arboriculture_items")
						.setRolls(ConstantValue.exactly(1))
						.add(saplingLoot(ForestryTreeSpecies.ACACIA_VANILLA))
						.add(EmptyLootItem.emptyItem().setWeight(3))
				));
		add(BuiltInLootTables.DESERT_PYRAMID, "factory",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_factory_items")
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(CoreItems.ASH.item())
								.apply(SetItemCountFunction.setCount(UniformGenerator.between(7, 12))))
						.add(EmptyLootItem.emptyItem().setWeight(1))
				));
		add(BuiltInLootTables.END_CITY_TREASURE, "apiculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_apiculture_bees").setRolls(ConstantValue.exactly(2))
						.add(beeLoot(ForestryBeeSpecies.STEADFAST)
								.apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))
								.setWeight(20)
						)
						.add(beeLoot(ForestryBeeSpecies.ENDED)
								.apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
								.setWeight(20)
						)
						.add(EmptyLootItem.emptyItem().setWeight(60))
				));
		add(BuiltInLootTables.END_CITY_TREASURE, "arboriculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_arboriculture_items")
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(ArboricultureItems.GRAFTER.item()))
						.add(EmptyLootItem.emptyItem().setWeight(1))
				));
		add(BuiltInLootTables.IGLOO_CHEST, "apiculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_apiculture_bees")
						.setRolls(ConstantValue.exactly(1))
						.add(beeLoot(ForestryBeeSpecies.STEADFAST))
						.add(beeLoot(ForestryBeeSpecies.WINTRY).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))).setWeight(2))
						.add(EmptyLootItem.emptyItem().setWeight(7))
				));
		add(BuiltInLootTables.JUNGLE_TEMPLE, "apiculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_apiculture_bees")
						.setRolls(ConstantValue.exactly(1))
						.add(beeLoot(ForestryBeeSpecies.STEADFAST))
						.add(beeLoot(ForestryBeeSpecies.TROPICAL).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))).setWeight(3))
						.add(EmptyLootItem.emptyItem().setWeight(6))
				));
		add(BuiltInLootTables.JUNGLE_TEMPLE, "arboriculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_arboriculture_items")
						.setRolls(ConstantValue.exactly(1))
						.add(saplingLoot(ForestryTreeSpecies.SIPIRI))
						.add(EmptyLootItem.emptyItem().setWeight(9))
				));
		add(BuiltInLootTables.NETHER_BRIDGE, "apiculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_apiculture_bees")
						.setRolls(ConstantValue.exactly(1))
						.add(beeLoot(ForestryBeeSpecies.STEADFAST))
						.add(beeLoot(ForestryBeeSpecies.SINISTER))
						.add(EmptyLootItem.emptyItem().setWeight(8))
				));
		add(BuiltInLootTables.SIMPLE_DUNGEON, "apiculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_apiculture_bees")
						.setRolls(ConstantValue.exactly(1))
						.add(beeLoot(ForestryBeeSpecies.STEADFAST))
						.add(EmptyLootItem.emptyItem().setWeight(9))
				));
		add(BuiltInLootTables.SPAWN_BONUS_CHEST, "apiculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_apiculture_bees")
						.setRolls(ConstantValue.exactly(1))
						.add(LootItem.lootTableItem(ApicultureItems.SCOOP.item()))
				));
		add(BuiltInLootTables.STRONGHOLD_CORRIDOR, "apiculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_apiculture_bees")
						.setRolls(ConstantValue.exactly(1))
						.add(beeLoot(ForestryBeeSpecies.STEADFAST))
						.add(EmptyLootItem.emptyItem().setWeight(9))
				));
		add(BuiltInLootTables.STRONGHOLD_CROSSING, "apiculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_apiculture_bees")
						.setRolls(ConstantValue.exactly(1))
						.add(beeLoot(ForestryBeeSpecies.STEADFAST))
						.add(EmptyLootItem.emptyItem().setWeight(9))
				));
		add(BuiltInLootTables.STRONGHOLD_LIBRARY, "apiculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_apiculture_bees")
						.setRolls(ConstantValue.exactly(1))
						.add(beeLoot(ForestryBeeSpecies.STEADFAST))
						.add(beeLoot(ForestryBeeSpecies.MONASTIC).setWeight(6))
						.add(EmptyLootItem.emptyItem().setWeight(3))
				));
		add(ForestryConstants.forestry("chests/village_naturalist"), "arboriculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_arboriculture_items")
						.setRolls(ConstantValue.exactly(3))
						.add(LootItem.lootTableItem(ArboricultureItems.GRAFTER.item()))
						.add(saplingLoot(ForestryTreeSpecies.LIME).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
						.add(saplingLoot(ForestryTreeSpecies.HILL_CHERRY).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
						.add(saplingLoot(ForestryTreeSpecies.LARCH).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
						.add(saplingLoot(ForestryTreeSpecies.TEAK))
						.add(saplingLoot(ForestryTreeSpecies.PADAUK))
				));
		add(ForestryConstants.forestry("chests/village_naturalist"), "apiculture",
				LootTable.lootTable().withPool(LootPool.lootPool()
						.name("forestry_apiculture_items")
						.setRolls(ConstantValue.exactly(4))
						.add(TagEntry.expandTag(ForestryTags.Items.VILLAGE_COMBS).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
						.add(LootItem.lootTableItem(ApicultureItems.SCOOP.item()).setWeight(5))
						.add(LootItem.lootTableItem(ApicultureItems.SMOKER))
				).withPool(LootPool.lootPool()
						.name("forestry_apiculture_bees")
						.setRolls(ConstantValue.exactly(3))
						.add(beeLoot(ForestryBeeSpecies.COMMON).setWeight(6))
						.add(beeLoot(ForestryBeeSpecies.MEADOWS).setWeight(6))
						.add(EmptyLootItem.emptyItem().setWeight(3))
				)
		);
	}

	private LootPoolSingletonContainer.Builder<?> saplingLoot(ResourceLocation species) {
		return saplingLoot(TreeLifeStage.SAPLING, species);
	}

	private LootPoolSingletonContainer.Builder<?> saplingLoot(TreeLifeStage type, ResourceLocation species) {
		return LootItem.lootTableItem(saplingItem(type))
				.apply(OrganismFunction.fromDefinition(SpeciesUtil.TREE_TYPE.get(), SpeciesUtil.getTreeSpecies(species)));
	}

	private LootPoolSingletonContainer.Builder<?> beeLoot(ResourceLocation species) {
		return beeLoot(BeeLifeStage.DRONE, species);
	}

	private LootPoolSingletonContainer.Builder<?> beeLoot(BeeLifeStage type, ResourceLocation species) {
		return LootItem.lootTableItem(beeItem(type))
				.apply(OrganismFunction.fromDefinition(SpeciesUtil.BEE_TYPE.get(), SpeciesUtil.getBeeSpecies(species)));
	}

	private Item saplingItem(TreeLifeStage type) {
		return switch (type) {
			case POLLEN -> ArboricultureItems.POLLEN_FERTILE.item();
			case SAPLING -> ArboricultureItems.SAPLING.item();
		};
	}

	private Item beeItem(BeeLifeStage type) {
		return switch (type) {
			case QUEEN -> ApicultureItems.BEE_QUEEN.item();
			case LARVAE -> ApicultureItems.BEE_LARVAE.item();
			case PRINCESS -> ApicultureItems.BEE_PRINCESS.item();
			case DRONE -> ApicultureItems.BEE_DRONE.item();
		};
	}

	protected void add(ResourceLocation location, String extension, LootTable.Builder builder) {
		entries.put(location, new Entry(location, extension, builder));
	}

	public static class Entry {
		public final ResourceLocation defaultLocation;
		public final String extension;
		public final LootTable.Builder builder;

		public Entry(ResourceLocation defaultLocation, String extension, LootTable.Builder builder) {
			this.defaultLocation = defaultLocation;
			this.extension = extension;
			this.builder = builder;
		}

		public ResourceLocation getLocation() {
			return ForestryConstants.forestry(defaultLocation.getPath() + "/" + extension);
		}
	}
}
