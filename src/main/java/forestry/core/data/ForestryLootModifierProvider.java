package forestry.core.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import net.minecraftforge.common.data.GlobalLootModifierProvider;

import forestry.api.ForestryConstants;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.loot.GrafterLootModifier;
import forestry.core.loot.ConditionLootModifier;

import static net.minecraft.advancements.critereon.ItemPredicate.Builder.item;
import static net.minecraft.world.level.storage.loot.predicates.MatchTool.toolMatches;

/**
 * Data provider for the generation of global loot modifiers.
 * <p>
 * Currently the only modifier is the {@link ConditionLootModifier}
 */
public class ForestryLootModifierProvider extends GlobalLootModifierProvider {
	public ForestryLootModifierProvider(PackOutput output) {
		super(output, ForestryConstants.MOD_ID);
	}

	@Override
	protected void start() {
		for (Map.Entry<ResourceLocation, Collection<LootTableHelper.Entry>> mapEntry : LootTableHelper.getInstance().entries.asMap().entrySet()) {
			List<String> extensions = mapEntry.getValue().stream().map(entry -> entry.extension).toList();
			add(mapEntry.getKey().getPath(), new ConditionLootModifier(mapEntry.getKey(), extensions));
		}
		add("grafter", new GrafterLootModifier(new LootItemCondition[]{
				toolMatches(item().of(ArboricultureItems.GRAFTER.item())).or(toolMatches(item().of(ArboricultureItems.GRAFTER_PROVEN.item()))).build()
		}));
	}
}
