package forestry.core.data;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.apiculture.features.ApicultureItems;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static net.minecraft.advancements.Advancement.Builder.advancement;

public class ForestryAdvancementProvider extends AdvancementProvider {
	public ForestryAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
		super(output, registries, existingFileHelper, List.of(new CoreAdvancements()));
	}

	private static class CoreAdvancements implements AdvancementGenerator {
		@Override
		public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> writer, ExistingFileHelper existingFileHelper) {
			ItemStack icon = SpeciesUtil.BEE_TYPE.get().createStack(ForestryBeeSpecies.INDUSTRIOUS, BeeLifeStage.QUEEN);

			advancement()
				.display(
					icon,
					Component.translatable("advancements.forestry.root.title"),
					Component.translatable("advancements.forestry.root.description"),
					ResourceLocation.withDefaultNamespace("textures/block/honeycomb_block.png"),
					AdvancementType.TASK,
					false,
					false,
					false
				)
				.addCriterion("tick", InventoryChangeTrigger.TriggerInstance.hasItems(ApicultureItems.BEE_COMBS.itemArray()))
				.rewards(new AdvancementRewards(0, List.of(ResourceKey.create(Registries.LOOT_TABLE, ForestryConstants.forestry("grant_guide"))), List.of(), Optional.empty()))
				.save(writer, ForestryConstants.MOD_ID + ":root");
		}
	}
}
