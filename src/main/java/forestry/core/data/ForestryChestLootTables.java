package forestry.core.data;

import forestry.api.ForestryConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.BiConsumer;

public class ForestryChestLootTables implements LootTableSubProvider {
	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
		consumer.accept(ResourceKey.create(Registries.LOOT_TABLE, ForestryConstants.forestry("chests/village_naturalist")), LootTable.lootTable());
		for (LootTableHelper.Entry entry : LootTableHelper.getInstance().entries.values()) {
			consumer.accept(entry.getLocation(), entry.builder());
		}
	}
}
