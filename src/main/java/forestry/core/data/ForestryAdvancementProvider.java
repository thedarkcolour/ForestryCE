package forestry.core.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTable;

import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import forestry.api.ForestryConstants;
import forestry.api.ForestryTags;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.apiculture.alveary.AlvearyBlock;
import forestry.apiculture.apiary.ApicultureBlockType;
import forestry.apiculture.bees.EnumHoneyComb;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.features.ApicultureItems;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.features.CharcoalBlocks;
import forestry.core.content.energy.blocks.EngineBlockType;
import forestry.core.content.energy.features.EnergyBlocks;
import forestry.core.content.machines.blocks.BlockTypeFactoryPlain;
import forestry.core.content.machines.blocks.BlockTypeFactoryTesr;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.features.FluidsItems;
import forestry.core.platform.advancements.ApicultureResearchTrigger;
import forestry.core.platform.advancements.ArboricultureResearchTrigger;
import forestry.core.platform.advancements.DiscoverSpeciesTrigger;
import forestry.core.platform.block.BlockTypeCoreTesr;
import forestry.core.platform.fluids.ForestryFluids;
import forestry.core.platform.item.FluidContainerType;
import forestry.core.platform.util.SpeciesUtil;

public class ForestryAdvancementProvider extends AdvancementProvider {
	public ForestryAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
		super(output, registries, existingFileHelper, List.of(new CoreAdvancements()));
	}

	private static class CoreAdvancements implements AdvancementProvider.AdvancementGenerator {
		@Override
		public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> writer, ExistingFileHelper existingFileHelper) {
			// Kept as the Industrious queen this tree already showed. 1.20.1 uses a Forest queen, but the
			// icon is a presentation choice rather than a port gap
			ItemStack icon = SpeciesUtil.BEE_TYPE.get().createStack(ForestryBeeSpecies.INDUSTRIOUS, BeeLifeStage.QUEEN);

			// Forestry
			// Deviation from 1.20.1: the reward is the grant_guide loot table, and a loot table is
			// named by a ResourceKey in 1.21
			ResourceKey<LootTable> guide = ResourceKey.create(Registries.LOOT_TABLE, ForestryConstants.forestry("grant_guide"));
			AdvancementHolder root = Advancement.Builder.advancement()
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
				// Deviation from 1.20.1: LocationPredicate.ANY is gone. An empty entity predicate is
				// what 1.20.1 wrote out, and the location trigger fires on every player tick either way
				.addCriterion("used_forestry", PlayerTrigger.TriggerInstance.located(EntityPredicate.Builder.entity()))
				.rewards(AdvancementRewards.Builder.loot(guide))
				.save(writer, ForestryConstants.forestry("root").toString());

			// Flame Grilled
			// Granted manually via BlockAsh
			AdvancementHolder woodPile = ForestryAdvancements.add(writer, "break_ash_block",
				CharcoalBlocks.LOG_PILE.stack(),
				root,
				CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

			// Ashes to Ashes
			ForestryAdvancements.add(writer, "get_burn_barrel",
				CoreBlocks.BURN_BARREL.stack(),
				woodPile,
				InventoryChangeTrigger.TriggerInstance.hasItems(CoreBlocks.BURN_BARREL.get()));

			// I Can, Can You?
			ForestryAdvancements.add(writer, "get_cans",
				FluidsItems.CONTAINERS.get(FluidContainerType.CAN).stack(),
				root,
				InventoryChangeTrigger.TriggerInstance.hasItems(FluidsItems.CONTAINERS.get(FluidContainerType.CAN)));

			// Here's the scoop!
			// Like what newspaper people say
			// Deviation from 1.20.1: the scoop moved to core, so the item is CoreItems.SCOOP
			AdvancementHolder scooped = ForestryAdvancements.add(writer, "get_scoop",
				CoreItems.SCOOP.stack(),
				root,
				InventoryChangeTrigger.TriggerInstance.hasItems(CoreItems.SCOOP.get()));

			// Ol' Reliable
			// A reference to Spongebob's special jellyfishing net
			// Deviation from 1.20.1: ApicultureItems.SCOOP_PROVEN became CoreItems.PROVEN_SCOOP
			ForestryAdvancements.add(writer, "get_proven_scoop",
				CoreItems.PROVEN_SCOOP.stack(),
				scooped,
				InventoryChangeTrigger.TriggerInstance.hasItems(CoreItems.PROVEN_SCOOP.get()),
				AdvancementType.GOAL, false);

			// Zonked Out
			// To be zonked is to be like, really tired, or really high. Which is kinda what the smoker does.
			AdvancementHolder smoked = ForestryAdvancements.add(writer, "get_smoker",
				ApicultureItems.SMOKER.stack(),
				scooped,
				InventoryChangeTrigger.TriggerInstance.hasItems(ApicultureItems.SMOKER.get()));

			// The Beekeeper
			// A reference to the Jason Statham movie of the same name
			// Deviation from 1.20.1: the explicit requirements array named one group per criterion,
			// which is what the default AND strategy already builds
			ForestryAdvancements.save(writer, "get_apiarists_armor",
				ForestryAdvancements.display("get_apiarists_armor", ApicultureItems.APIARIST_HELMET.stack(), smoked, AdvancementType.GOAL, false)
					.addCriterion("has_apiarist_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(ApicultureItems.APIARIST_HELMET.get()))
					.addCriterion("has_apiarist_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(ApicultureItems.APIARIST_CHEST.get()))
					.addCriterion("has_apiarist_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(ApicultureItems.APIARIST_LEGS.get()))
					.addCriterion("has_apiarist_boots", InventoryChangeTrigger.TriggerInstance.hasItems(ApicultureItems.APIARIST_BOOTS.get())));

			// This Is Where The Series Ends
			// A reference to the Yogscast, and their infamous Site Bee series
			AdvancementHolder bee = ForestryAdvancements.add(writer, "get_a_bee",
				SpeciesUtil.BEE_TYPE.get().createStack(ForestryBeeSpecies.MEADOWS, BeeLifeStage.DRONE),
				scooped,
				InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(
					ApicultureItems.BEE_DRONE.get(),
					ApicultureItems.BEE_PRINCESS.get(),
					ApicultureItems.BEE_QUEEN.get()
				)));

			// iPad Kid
			// A reference to kids and they phones
			AdvancementHolder analyser = ForestryAdvancements.add(writer, "get_analyser",
				CoreItems.PORTABLE_ANALYZER.stack(),
				bee,
				InventoryChangeTrigger.TriggerInstance.hasItems(CoreItems.PORTABLE_ANALYZER.get()));

			/*
			 * Advancements for finding specific bee species begin here
			 */

			// I'm Different!
			// A reference to Portal 2
			discoveredBee(writer, "get_valiant_drone", ForestryBeeSpecies.VALIANT, analyser);

			// Dungeon Flyer
			// A pun on Dungeon Crawler
			discoveredBee(writer, "get_steadfast_drone", ForestryBeeSpecies.STEADFAST, analyser);

			// What a deal!
			discoveredBee(writer, "get_monastic_drone", ForestryBeeSpecies.MONASTIC, analyser);

			// Yellow-and-Blackbeard
			// A play on Blackbeard, the pirate
			discoveredBee(writer, "get_pirate_drone", ForestryBeeSpecies.PIRATE, analyser);

			// The Land Beefore Time
			// A play on the land before time, that one movie I've definitely seen.
			// Deviation from 1.20.1: the species 1.20.1 called "bee_relic" is "chronofuge" here. The
			// advancement keeps the id and the lang keys it had
			discoveredBee(writer, "get_relic_drone", ForestryBeeSpecies.CHRONOFUGE, analyser);

			// Zombeefication
			discoveredBee(writer, "get_zombie_drone", ForestryBeeSpecies.ZOMBIFIED, analyser);

			// Buzzy Bees!
			// I couldn't think of anything clever for this, so this is named after the 1.15 update.
			discoveredBee(writer, "get_bee_drone", ForestryBeeSpecies.VANILLA, analyser);

			/*
			 * Advancements for finding specific bee species end here
			 */

			// Bee house, Sweet Bee house
			// A play on Home Sweet Home
			AdvancementHolder beeHouse = ForestryAdvancements.add(writer, "get_bee_house",
				ApicultureBlocks.BASE.stack(ApicultureBlockType.BEE_HOUSE),
				bee,
				InventoryChangeTrigger.TriggerInstance.hasItems(ApicultureBlocks.BASE.get(ApicultureBlockType.BEE_HOUSE).get()));

			// Beevolutionary!
			AdvancementHolder apiary = ForestryAdvancements.add(writer, "get_apiary",
				ApicultureBlocks.BASE.stack(ApicultureBlockType.APIARY),
				beeHouse,
				InventoryChangeTrigger.TriggerInstance.hasItems(ApicultureBlocks.BASE.get(ApicultureBlockType.APIARY).get()));

			// When Is a Raven Like a Writing Desk?
			// A reference to the quote which is from like, Lewis Carrol or something?
			AdvancementHolder escritoire = ForestryAdvancements.add(writer, "get_escritoire",
				CoreBlocks.BASE.stack(BlockTypeCoreTesr.ESCRITOIRE),
				apiary,
				InventoryChangeTrigger.TriggerInstance.hasItems(CoreBlocks.BASE.get(BlockTypeCoreTesr.ESCRITOIRE).get()));

			// Eureka
			// This is supposed to trigger when using a research note but I got lazy.
			AdvancementHolder researchNote = ForestryAdvancements.add(writer, "use_research_note",
				CoreItems.RESEARCH_NOTE.stack(),
				escritoire,
				InventoryChangeTrigger.TriggerInstance.hasItems(CoreItems.RESEARCH_NOTE.get()));

			// Master Arborist
			ForestryAdvancements.add(writer, "complete_tree_research",
				SpeciesUtil.TREE_TYPE.get().createStack(ForestryTreeSpecies.ELM, TreeLifeStage.SAPLING),
				researchNote,
				ArboricultureResearchTrigger.TriggerInstance.checkIfResearchIsGreaterThan(1.0),
				AdvancementType.CHALLENGE, false);

			// Master Apiarist
			ForestryAdvancements.add(writer, "complete_bee_research",
				SpeciesUtil.BEE_TYPE.get().createStack(ForestryBeeSpecies.IMPERIAL, BeeLifeStage.QUEEN),
				researchNote,
				ApicultureResearchTrigger.TriggerInstance.checkIfResearchIsGreaterThan(1.0),
				AdvancementType.CHALLENGE, false);

			// I've Been Framed
			ForestryAdvancements.add(writer, "get_frames",
				ApicultureItems.FRAME_UNTREATED.stack(),
				apiary,
				InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(
					ApicultureItems.FRAME_UNTREATED.get(),
					ApicultureItems.FRAME_IMPREGNATED.get(),
					ApicultureItems.FRAME_PROVEN.get()
				)));

			// Al-very Nice!
			// Granted manually via ContainerAlveary
			// Deviation from 1.20.1: BlockAlvearyType became the BlockAlveary.Type record
			AdvancementHolder alveary = ForestryAdvancements.add(writer, "get_alveary",
				ApicultureBlocks.ALVEARY.stack(AlvearyBlock.Type.PLAIN),
				apiary,
				CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

			// Make a House a Home
			ForestryAdvancements.add(writer, "get_alveary_upgrade",
				ApicultureBlocks.ALVEARY.stack(AlvearyBlock.Type.SWARMER),
				alveary,
				InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(
					ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.HEATER).get(),
					ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.FAN).get(),
					ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.STABILIZER).get(),
					ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.HYGROREGULATOR).get(),
					ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.SIEVE).get(),
					ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.SWARMER).get()
				)),
				AdvancementType.GOAL, false);

			// Minegraft
			// A pun on that one block game people like; Roblox
			AdvancementHolder grafter = ForestryAdvancements.add(writer, "get_grafter",
				ArboricultureItems.GRAFTER.stack(),
				beeHouse,
				InventoryChangeTrigger.TriggerInstance.hasItems(ArboricultureItems.GRAFTER.get()));

			// The Gift of the Graft
			// A reference to the phrase "The gift of the gab" because I couldn't think of anything else.
			// Deviation from 1.20.1: GRAFTER_PROVEN became PROVEN_GRAFTER
			ForestryAdvancements.add(writer, "get_proven_grafter",
				ArboricultureItems.PROVEN_GRAFTER.stack(),
				grafter,
				InventoryChangeTrigger.TriggerInstance.hasItems(ArboricultureItems.PROVEN_GRAFTER.get()),
				AdvancementType.GOAL, false);

			// Branching Off
			// todo make this trigger for all non-vanilla tree types
			// Deviation from 1.20.1: ArboricultureItems.SAPLING became TREE_SAPLING
			AdvancementHolder sapling = ForestryAdvancements.add(writer, "get_forestry_sapling",
				Items.OAK_SAPLING.getDefaultInstance(),
				grafter,
				InventoryChangeTrigger.TriggerInstance.hasItems(ArboricultureItems.TREE_SAPLING.get()));

			/*
			 * Advancements for end-of-line forestry saplings begin here
			 */

			// Roasting on an Open Fire
			discoveredTree(writer, "get_chestnut_sapling", ForestryTreeSpecies.CHESTNUT, sapling, AdvancementType.TASK);

			// The Tree of Life
			discoveredTree(writer, "get_baobab_sapling", ForestryTreeSpecies.BAOBAB, sapling, AdvancementType.TASK);

			// That is Mahogany!
			// A reference to The Hunger Games when Katniss stabs a table with a knife
			discoveredTree(writer, "get_mahogany_sapling", ForestryTreeSpecies.MAHOGANY, sapling, AdvancementType.TASK);

			// The Wind in the Willows
			// A reference to the book of the same name
			discoveredTree(writer, "get_willow_sapling", ForestryTreeSpecies.WILLOW, sapling, AdvancementType.TASK);

			// Tane Mahuta!
			// The name of New Zealand's largest Kauri tree
			discoveredTree(writer, "get_kauri_sapling", ForestryTreeSpecies.KAURI, sapling, AdvancementType.TASK);

			// What Else Can I Do?
			// A reference to the song of the same name from Disney's Encanto, and the 'hurricane of jacarandas'
			discoveredTree(writer, "get_jacaranda_sapling", ForestryTreeSpecies.JACARANDA, sapling, AdvancementType.TASK);

			// A Living Fossil
			// Ginkgo trees are often referred to as such
			AdvancementHolder ginkgo = discoveredTree(writer, "get_ginkgo_sapling", ForestryTreeSpecies.GINKGO, sapling, AdvancementType.TASK);

			// Fee-Fi-Fo-Fum!
			// A line spoken by the Giant in Jack and the Beanstalk
			discoveredTree(writer, "get_giganteum_sapling", ForestryTreeSpecies.GIANT_SEQUOIA, ginkgo, AdvancementType.GOAL);

			/*
			 * Advancements for end-of-line forestry saplings end here
			 */

			// The Fruits of my Labour
			// The fruits of ones labour is like, the result of all their hard work, yknow?
			Advancement.Builder fruits = ForestryAdvancements.display("get_all_fruits", Items.APPLE.getDefaultInstance(), sapling, AdvancementType.CHALLENGE, false);
			CoreItems.FRUITS.getItems().forEach(fruit -> fruits.addCriterion(
				"get_" + BuiltInRegistries.ITEM.getKey(fruit.asItem()).getPath(),
				InventoryChangeTrigger.TriggerInstance.hasItems(fruit)
			));
			ForestryAdvancements.save(writer, "get_all_fruits", fruits);

			// Honey, I'm Home!
			Advancement.Builder combs = ForestryAdvancements.display("get_all_combs", ApicultureItems.BEE_COMBS.get(EnumHoneyComb.HONEY).stack(), beeHouse, AdvancementType.CHALLENGE, false);
			ApicultureItems.BEE_COMBS.getItems().forEach(comb -> combs.addCriterion(
				"get_" + BuiltInRegistries.ITEM.getKey(comb.asItem()).getPath(),
				InventoryChangeTrigger.TriggerInstance.hasItems(comb)
			));
			ForestryAdvancements.save(writer, "get_all_combs", combs);

			// I See What's Going On Here
			ForestryAdvancements.add(writer, "get_spectacles",
				CoreItems.SPECTACLES.stack(),
				beeHouse,
				InventoryChangeTrigger.TriggerInstance.hasItems(CoreItems.SPECTACLES.get()));

			// Powering Up
			AdvancementHolder engine = ForestryAdvancements.add(writer, "get_engine",
				EnergyBlocks.ENGINES.stack(EngineBlockType.BIOGAS),
				root,
				InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(
					EnergyBlocks.ENGINES.get(EngineBlockType.CLOCKWORK).get(),
					EnergyBlocks.ENGINES.get(EngineBlockType.PEAT).get(),
					EnergyBlocks.ENGINES.get(EngineBlockType.BIOGAS).get(),
					EnergyBlocks.ENGINES.get(EngineBlockType.COMBUSTION).get(),
					EnergyBlocks.ENGINES.get(EngineBlockType.SOLAR).get()
				)));

			// Wound Up
			// Granted manually via ClockworkEngineBlockEntity
			ForestryAdvancements.add(writer, "take_damage_from_clockwork_engine",
				EnergyBlocks.ENGINES.stack(EngineBlockType.CLOCKWORK),
				engine,
				CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()),
				AdvancementType.TASK, true);

			// Walkin' on the Sun
			// A reference to the song of the same name.
			ForestryAdvancements.add(writer, "get_solar_panel",
				EnergyBlocks.SOLAR_PANEL.stack(),
				engine,
				InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(EnergyBlocks.SOLAR_PANEL.get())));

			// Next Level Crafting
			ForestryAdvancements.add(writer, "get_carpenter",
				FactoryBlocks.PLAIN.stack(BlockTypeFactoryPlain.CARPENTER),
				engine,
				InventoryChangeTrigger.TriggerInstance.hasItems(FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.CARPENTER).get()));

			// Lightly Bronzed
			// To be bronzed is to be tanned.
			AdvancementHolder bronzed = ForestryAdvancements.add(writer, "get_bronze",
				CoreItems.BRONZE_INGOT.stack(),
				engine,
				InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ForestryTags.Items.INGOTS_BRONZE)));

			// No Thanks, I Already Ate
			AdvancementHolder fertiliser = ForestryAdvancements.add(writer, "get_fertiliser",
				CoreItems.FERTILIZER_COMPOUND.stack(),
				bronzed,
				InventoryChangeTrigger.TriggerInstance.hasItems(CoreItems.FERTILIZER_COMPOUND.get()));

			// Green Means Clean!
			// todo allow biomass cans and capsules and stuff to trigger this too
			ForestryAdvancements.add(writer, "get_biomass",
				ForestryFluids.BIOMASS.getBucket().getDefaultInstance(),
				fertiliser,
				InventoryChangeTrigger.TriggerInstance.hasItems(ForestryFluids.BIOMASS.getBucket()));

			// You Spin Me Right Round
			AdvancementHolder centrifuge = ForestryAdvancements.add(writer, "get_centrifuge",
				FactoryBlocks.PLAIN.stack(BlockTypeFactoryPlain.CENTRIFUGE),
				bronzed,
				InventoryChangeTrigger.TriggerInstance.hasItems(FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.CENTRIFUGE).get()));

			// Honey (Sugar, Sugar)
			// Deviation from 1.20.1: the honey drop, honeydew and beeswax moved to core
			ForestryAdvancements.add(writer, "get_honey",
				CoreItems.HONEY_DROP.stack(),
				centrifuge,
				InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(
					CoreItems.HONEY_DROP.get(),
					CoreItems.HONEYDEW.get(),
					CoreItems.BEESWAX.get()
				)));

			// Freshly Squeezed
			AdvancementHolder squeezer = ForestryAdvancements.add(writer, "get_squeezer",
				FactoryBlocks.PLAIN.stack(BlockTypeFactoryPlain.SQUEEZER),
				bronzed,
				InventoryChangeTrigger.TriggerInstance.hasItems(FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.SQUEEZER).get()));

			// Bee Juice
			// A reference to Clarkson's Farm, and what Clarkson refers to his honey as
			// todo allow honey cans and capsules and stuff to trigger this too
			ForestryAdvancements.add(writer, "get_liquid_honey",
				ForestryFluids.HONEY.getBucket().getDefaultInstance(),
				squeezer,
				InventoryChangeTrigger.TriggerInstance.hasItems(ForestryFluids.HONEY.getBucket()));

			// Glass Crafting
			AdvancementHolder fabricator = ForestryAdvancements.add(writer, "get_fabricator",
				FactoryBlocks.PLAIN.stack(BlockTypeFactoryPlain.FABRICATOR),
				bronzed,
				InventoryChangeTrigger.TriggerInstance.hasItems(FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.FABRICATOR).get()));

			// Non-Inflammable
			ForestryAdvancements.add(writer, "do_fireproofing",
				CoreItems.REFRACTORY_WAX.stack(),
				fabricator,
				InventoryChangeTrigger.TriggerInstance.hasItems(CoreItems.REFRACTORY_WAX.get()));

			// farming_simulator and feed_the_world hang under get_fabricator, but the planters and the
			// multifarm they ask for live in the farms jar. See AgricultureAdvancementProvider

			// Make It Rain!
			// Granted manually via TileMillRainmaker
			ForestryAdvancements.add(writer, "use_rainmaker",
				FactoryBlocks.TESR.stack(BlockTypeFactoryTesr.RAINMAKER),
				bronzed,
				CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

			// I Didn't Hear No Bell
			// Deviation from 1.20.1: the bronze tools are named after the Survivalist here, so the
			// remnants are BROKEN_SURVIVALISTS_*. The advancement keeps its 1.20.1 id and lang keys
			ForestryAdvancements.add(writer, "break_bronze_tool",
				CoreItems.BROKEN_SURVIVALISTS_PICKAXE.stack(),
				bronzed,
				InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(
					CoreItems.BROKEN_SURVIVALISTS_AXE.get(),
					CoreItems.BROKEN_SURVIVALISTS_PICKAXE.get(),
					CoreItems.BROKEN_SURVIVALISTS_SHOVEL.get(),
					CoreItems.BROKEN_SURVIVALISTS_HOE.get(),
					CoreItems.BROKEN_SURVIVALISTS_SWORD.get()
				)),
				AdvancementType.TASK, true);
		}

		// Advancements that are missing:
		// We just got a letter - Open a letter
		// I've bee-n around - discover all natural hives
		// Duffman(?) - Brew an alcohol
		// Bright-eyed and Bushy-tailed - Sleep off drunkeness
		// Sting Operation - Complete a raid whilst wearing Apiarist Armor

		/**
		 * Adds one advancement that researching a single bee species earns.
		 *
		 * @param writer  The sink the provider writes to
		 * @param name    The advancement id's path
		 * @param species The bee species the advancement asks for
		 * @param parent  The advancement it hangs under
		 * @return The advancement, to hang a child under
		 */
		private static AdvancementHolder discoveredBee(Consumer<AdvancementHolder> writer, String name, ResourceLocation species, AdvancementHolder parent) {
			return ForestryAdvancements.add(writer, name,
				SpeciesUtil.BEE_TYPE.get().createStack(species, BeeLifeStage.DRONE),
				parent,
				DiscoverSpeciesTrigger.TriggerInstance.checkDiscovered(species));
		}

		/**
		 * Adds one advancement that researching a single tree species earns.
		 *
		 * @param writer  The sink the provider writes to
		 * @param name    The advancement id's path
		 * @param species The tree species the advancement asks for
		 * @param parent  The advancement it hangs under
		 * @param type    The frame the advancement tree draws around the icon
		 * @return The advancement, to hang a child under
		 */
		private static AdvancementHolder discoveredTree(Consumer<AdvancementHolder> writer, String name, ResourceLocation species, AdvancementHolder parent, AdvancementType type) {
			return ForestryAdvancements.add(writer, name,
				SpeciesUtil.TREE_TYPE.get().createStack(species, TreeLifeStage.SAPLING),
				parent,
				DiscoverSpeciesTrigger.TriggerInstance.checkDiscovered(species),
				type, false);
		}
	}
}
