package forestry.agriculture.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import forestry.agriculture.features.FarmingBlocks;
import forestry.agriculture.multifarm.blocks.MultifarmBlockType;
import forestry.agriculture.multifarm.blocks.MultifarmMaterialType;
import forestry.agriculture.minifarm.blocks.MinifarmBlockType;
import forestry.core.data.ForestryAdvancements;

/**
 * The two advancements that ask for farms jar content. Both hang under core's get_fabricator, which
 * the game resolves once the two packs are loaded together.
 */
// Deviation from 1.20.1: 1.20.1 wrote all 57 advancements from one provider. The planters and the
// multifarm are farms jar content, which core's provider cannot see, so these two write here
public class AgricultureAdvancementProvider extends AdvancementProvider {
	public AgricultureAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
		super(output, registries, existingFileHelper, List.of(new FarmAdvancements()));
	}

	private static class FarmAdvancements implements AdvancementProvider.AdvancementGenerator {
		@Override
		public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> writer, ExistingFileHelper existingFileHelper) {
			AdvancementHolder fabricator = ForestryAdvancements.reference("get_fabricator");

			List<Item> planters = new ArrayList<>();
			for (BlockItem planter : FarmingBlocks.MANAGED_MINIFARM.getItems()) {
				planters.add(planter.asItem());
			}
			for (BlockItem planter : FarmingBlocks.MANUAL_MINIFARM.getItems()) {
				planters.add(planter.asItem());
			}

			// Farming Simulator
			AdvancementHolder farmingSimulator = ForestryAdvancements.add(writer, "farming_simulator",
				FarmingBlocks.MANAGED_MINIFARM.stack(MinifarmBlockType.ARBORETUM),
				fabricator,
				InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(planters.toArray(Item[]::new))));

			// Feed The World
			// Granted manually via ContainerFarm
			ForestryAdvancements.add(writer, "feed_the_world",
				FarmingBlocks.FARM.stack(MultifarmBlockType.PLAIN, MultifarmMaterialType.STONE_BRICK),
				farmingSimulator,
				CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()),
				AdvancementType.GOAL, false);
		}
	}
}
