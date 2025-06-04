package forestry.core.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ForestryConfig {
	private static final ModConfigSpec CLIENT_SPEC;
	private static final ModConfigSpec COMMON_SPEC;
	private static final ModConfigSpec SERVER_SPEC;

	public static final Client CLIENT;
	public static final Common COMMON;
	public static final Server SERVER;

	public static class Common {
		public Common(ModConfigSpec.Builder builder) {

		}
	}

	public static class Client {
		// Misc
		public final ModConfigSpec.BooleanValue showParticles;
		public final ModConfigSpec.BooleanValue enableHints;
		public final ModConfigSpec.BooleanValue enableGlints;
		// Mail
		public final ModConfigSpec.BooleanValue mailAlertsEnabled;
		// JEI Bees
		public final ModConfigSpec.BooleanValue showRequirements;
		public final ModConfigSpec.BooleanValue showSecretMutations;
		public final ModConfigSpec.BooleanValue identifyGenome;

		public Client(ModConfigSpec.Builder builder) {
			this.showParticles = builder
				.comment("Whether any of Forestry's particles are rendered.")
				.define("particles", true);
			this.enableHints = builder
				.comment("Whether the \"Did you know?\" ledgers are shown in Forestry menus.")
				.define("enable_hints", true);
			this.enableGlints = builder
				.comment("Whether certain specimens, like Imperial Bees, will have enchantment glints on them.")
				.define("enable_glints", true);

			this.mailAlertsEnabled = builder
				.comment("Whether alerts are enabled for Forestry's mail system.")
				.define("mail_alerts_enable", true);

			builder.push("jei_bees");
			this.showRequirements = builder
				.comment("Set to false to disable display of mutation requirements")
				.define("show_requirements", true);
			this.showSecretMutations = builder
				.comment("Set to false to disable display of secret mutations")
				.define("show_secret_mutations", true);
			this.identifyGenome = builder
				.comment("Set to false to disable showing identified genome in recipes")
				.define("show_analyzed_tooltip", true);
			builder.pop();
		}
	}

	public static class Server {
		// Genetics
		public final ModConfigSpec.DoubleValue researchMutationBoostMultiplier;
		public final ModConfigSpec.DoubleValue maxResearchMutationBoostPercent;
		// Bees
		public final ModConfigSpec.BooleanValue pollinateVanillaLeaves;
		public final ModConfigSpec.DoubleValue wildHiveSpawnRate;
		public final ModConfigSpec.BooleanValue useHaploidDrones;
		// Trees
		public final ModConfigSpec.DoubleValue treesSpawnNaturally;
		// Butterflies
		public final ModConfigSpec.BooleanValue disableButterflySpawning;
		public final ModConfigSpec.IntValue butterflyClusterLimit;
		public final ModConfigSpec.IntValue butterflyClusterWidth;
		public final ModConfigSpec.IntValue butterflyClusterHeight;
		// Farms
		public final ModConfigSpec.IntValue multiFarmFertilizerModifier;
		public final ModConfigSpec.IntValue multiFarmSize;
		public final ModConfigSpec.BooleanValue squareMultiFarms;
		public final ModConfigSpec.IntValue legacyFarmFertilizerModifier;
		public final ModConfigSpec.IntValue legacyFarmsPlanterRings;
		public final ModConfigSpec.BooleanValue legacyFarmsUseRings;
		public final ModConfigSpec.IntValue legacyFarmsRingSize;
		// Misc
		public final ModConfigSpec.BooleanValue enableBackpackResupply;
		public final ModConfigSpec.BooleanValue spawnTinOre;
		public final ModConfigSpec.BooleanValue spawnApatiteOre;

		public Server(ModConfigSpec.Builder builder) {
			// Genetics
			builder.push("genetics");
			this.researchMutationBoostMultiplier = builder
				.comment("When a player researches a mutation using the Escritoire, mutation chances for hives owned by that player are multiplied by this factor, with the increase in chance limited to the value set in \"research_mutation_boost_multiplier\".")
				.defineInRange("research_mutation_boost_multiplier", 1.5, 1.0, 1000.0);
			this.maxResearchMutationBoostPercent = builder
				.comment("When a player researchs a mutation using the Escritoire, mutation chances for hives owned by that player are multiplied by a certain factor, with the increase in chance capped to this value.")
				.defineInRange("max_research_mutation_boost_percent", 5.0, 0.0, 100.0);
			builder.pop();

			// Bees
			builder.push("bees");
			this.pollinateVanillaLeaves = builder
				.comment("Whether bees and butterflies can pollinate Vanilla leaves. Might be undesirable for builds that rely on leaves.")
				.define("pollinate_vanilla_leaves", true);
			this.wildHiveSpawnRate = builder
				.comment("The base chance for a wild beehive to spawn naturally.")
				.defineInRange("wild_hive_spawn_rate", 1.0, 0.0, 1000.0);
			this.useHaploidDrones = builder
				.comment("In real life, drone bees are haploid, which means they only carry one set of chromosomes. If this option is enabled, only a drone's active alleles will be used for inheritance, making drones effectively haploid. This CHANGES Forestry's bee breeding mechanics.")
				.define("use_haploid_drones", false);
			builder.pop();

			// Farming
			builder.push("multiblock_farm");
			this.multiFarmFertilizerModifier = builder
				.comment("Determines how much fertilizer value a multiblock farm uses when harvesting a block. For reference, 1 Fertilizer = 100 fertilizer value by default.")
				.defineInRange("multiblock_farm_fertilizer_modifier", 4, 1, 10000);
			this.multiFarmSize = builder
				.comment("")
				.defineInRange("multiblock_farm_size", 2, 1, 10);
			this.squareMultiFarms = builder
				.comment("Whether Forestry multiblock farms have square shaped farmlands instead of the default diamond shape.")
				.define("square_multiblock_farms", false);
			builder.pop();
			builder.push("legacy_farm");
			this.legacyFarmFertilizerModifier = builder
				.comment("Determines how much fertilizer value a legacy (single block) farm uses when harvesting a block. For reference, 1 Fertilizer = 100 fertilizer value by default.")
				.defineInRange("legacy_farm_fertilizer_modifier", 4, 1, 10000);
			this.legacyFarmsPlanterRings = builder
				.comment("Sets the size of the farmland that is used by all legacy (single block) farms.")
				.defineInRange("legacy_farms_planter_rings", 4, 1, 10);
			this.legacyFarmsUseRings = builder
				.comment("Whether legacy (single block) farms use a ring layout. The farmland size of the ring layout is always one block smaller.")
				.define("legacy_farms_use_rings", true);
			this.legacyFarmsRingSize = builder
				.comment("Sets the size of the inner ring of the ring layout used by legacy (single block) farms.")
				.defineInRange("legacy_farms_ring_size", 4, 1, 10);
			builder.pop();

			// Trees
			builder.push("trees");
			this.treesSpawnNaturally = builder
				.comment("Multiplies the chance of a Forestry tree spawning in the wild. Set to 0 to disable Forestry tree spawning.")
				.defineInRange("tree_spawn_chance_modifier", 0.0f, 0.0f, 1000000.0f);
			builder.pop();

			// Butterflies
			builder.push("butterflies");
			this.disableButterflySpawning = builder
				.comment("Whether butterflies can spawn from Forestry leaves.")
				.define("disable_butterfly_spawning", false);
			this.butterflyClusterLimit = builder
				.comment("The maximum number of butterflies that can spawn in the same area or cluster.")
				.defineInRange("butterfly_cluster_limit", 20, 1, 2000);
			this.butterflyClusterWidth = builder
				.comment("The width of the cluster area used when checking if the \"butterfly_cluster_limit\" has been reached.")
				.defineInRange("butterfly_cluster_width", 128, 0, 2000);
			this.butterflyClusterHeight = builder
				.comment("The height of the cluster area used when checking if the \"butterfly_cluster_limit\" has been reached.")
				.defineInRange("butterfly_cluster_height", 64, 0, 2000);
			builder.pop();

			// Misc
			this.enableBackpackResupply = builder
				.comment("Whether backpacks can have their resupply mode enabled, which stocks a player's inventory using blocks from the backpack's inventory.")
				.define("enable_backpack_resupply", true);
			this.spawnTinOre = builder
				.comment("Whether Tin Ore veins generate naturally in the Overworld.")
				.define("spawn_tin_ore", true);
			this.spawnApatiteOre = builder
				.comment("Whether Apatite Ore veins generate naturally in the Overworld.")
				.define("spawn_apatite_ore", true);
		}
	}

	public static void register(ModContainer ctx) {
		ctx.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
		ctx.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
		ctx.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
	}

	static {
		{
			Pair<Client, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Client::new);
			CLIENT = specPair.getLeft();
			CLIENT_SPEC = specPair.getRight();
		}
		{
			Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
			COMMON = specPair.getLeft();
			COMMON_SPEC = specPair.getRight();
		}
		{
			Pair<Server, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Server::new);
			SERVER = specPair.getLeft();
			SERVER_SPEC = specPair.getRight();
		}
	}
}
