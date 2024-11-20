package forestry.core.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.commands.CommandFunction;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.apiculture.features.ApicultureItems;
import forestry.core.utils.SpeciesUtil;

import static net.minecraft.advancements.Advancement.Builder.advancement;

public class ForestryAdvancementProvider extends ForgeAdvancementProvider {
	public ForestryAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
		super(output, registries, existingFileHelper, List.of(new CoreAdvancements()));
	}

	private static class CoreAdvancements implements AdvancementGenerator {
		@Override
		public void generate(HolderLookup.Provider registries, Consumer<Advancement> writer, ExistingFileHelper existingFileHelper) {
			ItemStack icon = SpeciesUtil.BEE_TYPE.get().createStack(ForestryBeeSpecies.INDUSTRIOUS, BeeLifeStage.QUEEN);

			advancement()
					.display(
							icon,
							Component.translatable("advancements.forestry.root.title"),
							Component.translatable("advancements.forestry.root.description"),
							new ResourceLocation("textures/block/honeycomb_block.png"),
							FrameType.TASK,
							false,
							false,
							false
					)
					.addCriterion("tick", InventoryChangeTrigger.TriggerInstance.hasItems(ApicultureItems.BEE_COMBS.itemArray()))
					.rewards(new AdvancementRewards(0, new ResourceLocation[]{ForestryConstants.forestry("grant_guide")}, new ResourceLocation[0], CommandFunction.CacheableFunction.NONE))
					.save(writer, ForestryConstants.MOD_ID + ":root");
		}
	}
}