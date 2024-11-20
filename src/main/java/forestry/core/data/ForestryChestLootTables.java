package forestry.core.data;

import java.util.function.BiConsumer;

import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import forestry.api.ForestryConstants;

public class ForestryChestLootTables implements LootTableSubProvider {
	@Override
	public void generate(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
		consumer.accept(ForestryConstants.forestry("chests/village_naturalist"), LootTable.lootTable());
		for (LootTableHelper.Entry entry : LootTableHelper.getInstance().entries.values()) {
			consumer.accept(entry.getLocation(), entry.builder);
		}
	}
}
