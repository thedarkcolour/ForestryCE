package forestry.core.content.backpacks.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.content.resources.EnumCraftingMaterial;
import forestry.core.platform.registration.*;
import forestry.core.content.backpacks.items.ItemCrated;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

@FeatureProvider
public class CrateItems {
	private static final List<FeatureItem<ItemCrated>> CRATES = new ArrayList<>();
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.STORAGE);

	public static final FeatureItem<ItemCrated> CRATE = REGISTRY.item(() -> new ItemCrated(() -> ItemStack.EMPTY), "crate");

	// Core
	public static final FeatureItem<ItemCrated> CRATED_PEAT = register(CoreItems.PEAT, "crated_peat");
	public static final FeatureItem<ItemCrated> CRATED_APATITE = register(CoreItems.APATITE, "crated_apatite");
	public static final FeatureItem<ItemCrated> CRATED_FERTILIZER_COMPOUND = register(CoreItems.FERTILIZER_COMPOUND, "crated_fertilizer_compound");
	public static final FeatureItem<ItemCrated> CRATED_MULCH = register(CoreItems.MULCH, "crated_mulch");
	public static final FeatureItem<ItemCrated> CRATED_PHOSPHOR = register(CoreItems.CRAFTING_MATERIALS.get(EnumCraftingMaterial.PHOSPHOR), "crated_phosphor");
	public static final FeatureItem<ItemCrated> CRATED_ASH = register(CoreItems.ASH, "crated_ash");
	public static final FeatureItem<ItemCrated> CRATED_TIN = register(CoreItems.TIN_INGOT, "crated_tin");
	public static final FeatureItem<ItemCrated> CRATED_COPPER = register(Items.COPPER_INGOT, "crated_copper");
	public static final FeatureItem<ItemCrated> CRATED_BRONZE = register(CoreItems.BRONZE_INGOT, "crated_bronze");

	public static final FeatureItem<ItemCrated> CRATED_HUMUS = register(CoreBlocks.HUMUS, "crated_humus");
	public static final FeatureItem<ItemCrated> CRATED_BOG_EARTH = register(CoreBlocks.BOG_EARTH, "crated_bog_earth");

	public static final FeatureItem<ItemCrated> CRATED_WHEAT = register(Items.WHEAT, "crated_wheat");
	public static final FeatureItem<ItemCrated> CRATED_COOKIE = register(Items.COOKIE, "crated_cookie");
	public static final FeatureItem<ItemCrated> CRATED_REDSTONE = register(Items.REDSTONE, "crated_redstone");
	public static final FeatureItem<ItemCrated> CRATED_LAPIS = register(Items.LAPIS_LAZULI, "crated_lapis");
	public static final FeatureItem<ItemCrated> CRATED_SUGAR_CANE = register(Items.SUGAR_CANE, "crated_sugar_cane");
	public static final FeatureItem<ItemCrated> CRATED_CLAY_BALL = register(Items.CLAY_BALL, "crated_clay_ball");
	public static final FeatureItem<ItemCrated> CRATED_GLOWSTONE = register(Items.GLOWSTONE_DUST, "crated_glowstone");
	public static final FeatureItem<ItemCrated> CRATED_APPLE = register(Items.APPLE, "crated_apple");
	public static final FeatureItem<ItemCrated> CRATED_COAL = register(Items.COAL, "crated_coal");
	public static final FeatureItem<ItemCrated> CRATED_CHARCOAL = register(Items.CHARCOAL, "crated_charcoal");
	public static final FeatureItem<ItemCrated> CRATED_SEEDS = register(Items.WHEAT_SEEDS, "crated_seeds");
	public static final FeatureItem<ItemCrated> CRATED_POTATO = register(Items.POTATO, "crated_potato");
	public static final FeatureItem<ItemCrated> CRATED_CARROT = register(Items.CARROT, "crated_carrot");
	public static final FeatureItem<ItemCrated> CRATED_BEETROOT = register(Items.BEETROOT, "crated_beetroot");
	public static final FeatureItem<ItemCrated> CRATED_NETHER_WART = register(Items.NETHER_WART, "crated_nether_wart");

	public static final FeatureItem<ItemCrated> CRATED_OAK_LOG = register(Items.OAK_LOG, "crated_oak_log");
	public static final FeatureItem<ItemCrated> CRATED_BIRCH_LOG = register(Items.BIRCH_LOG, "crated_birch_log");
	public static final FeatureItem<ItemCrated> CRATED_JUNGLE_LOG = register(Items.JUNGLE_LOG, "crated_jungle_log");
	public static final FeatureItem<ItemCrated> CRATED_SPRUCE_LOG = register(Items.SPRUCE_LOG, "crated_spruce_log");
	public static final FeatureItem<ItemCrated> CRATED_ACACIA_LOG = register(Items.ACACIA_LOG, "crated_acacia_log");
	public static final FeatureItem<ItemCrated> CRATED_DARK_OAK_LOG = register(Items.DARK_OAK_LOG, "crated_dark_oak_log");
	public static final FeatureItem<ItemCrated> CRATED_COBBLESTONE = register(Items.COBBLESTONE, "crated_cobblestone");
	public static final FeatureItem<ItemCrated> CRATED_DIRT = register(Items.DIRT, "crated_dirt");
	public static final FeatureItem<ItemCrated> CRATED_GRASS_BLOCK = register(Items.GRASS_BLOCK, "crated_grass_block");
	public static final FeatureItem<ItemCrated> CRATED_STONE = register(Items.STONE, "crated_stone");
	public static final FeatureItem<ItemCrated> CRATED_GRANITE = register(Items.GRANITE, "crated_granite");
	public static final FeatureItem<ItemCrated> CRATED_DIORITE = register(Items.DIORITE, "crated_diorite");
	public static final FeatureItem<ItemCrated> CRATED_ANDESITE = register(Items.ANDESITE, "crated_andesite");
	public static final FeatureItem<ItemCrated> CRATED_PRISMARINE = register(Items.PRISMARINE, "crated_prismarine");
	public static final FeatureItem<ItemCrated> CRATED_PRISMARINE_BRICKS = register(Items.PRISMARINE_BRICKS, "crated_prismarine_bricks");
	public static final FeatureItem<ItemCrated> CRATED_DARK_PRISMARINE = register(Items.DARK_PRISMARINE, "crated_dark_prismarine");
	public static final FeatureItem<ItemCrated> CRATED_BRICKS = register(Items.BRICKS, "crated_bricks");
	public static final FeatureItem<ItemCrated> CRATED_CACTUS = register(Items.CACTUS, "crated_cactus");
	public static final FeatureItem<ItemCrated> CRATED_SAND = register(Items.SAND, "crated_sand");
	public static final FeatureItem<ItemCrated> CRATED_RED_SAND = register(Items.RED_SAND, "crated_red_sand");
	public static final FeatureItem<ItemCrated> CRATED_OBSIDIAN = register(Items.OBSIDIAN, "crated_obsidian");
	public static final FeatureItem<ItemCrated> CRATED_NETHERRACK = register(Items.NETHERRACK, "crated_netherrack");
	public static final FeatureItem<ItemCrated> CRATED_SOUL_SAND = register(Items.SOUL_SAND, "crated_soul_sand");
	public static final FeatureItem<ItemCrated> CRATED_SANDSTONE = register(Items.SANDSTONE, "crated_sandstone");
	public static final FeatureItem<ItemCrated> CRATED_NETHER_BRICKS = register(Items.NETHER_BRICKS, "crated_nether_bricks");
	public static final FeatureItem<ItemCrated> CRATED_MYCELIUM = register(Items.MYCELIUM, "crated_mycelium");
	public static final FeatureItem<ItemCrated> CRATED_GRAVEL = register(Items.GRAVEL, "crated_gravel");
	public static final FeatureItem<ItemCrated> CRATED_OAK_SAPLING = register(Items.OAK_SAPLING, "crated_oak_sapling");
	public static final FeatureItem<ItemCrated> CRATED_BIRCH_SAPLING = register(Items.BIRCH_SAPLING, "crated_birch_sapling");
	public static final FeatureItem<ItemCrated> CRATED_JUNGLE_SAPLING = register(Items.JUNGLE_SAPLING, "crated_jungle_sapling");
	public static final FeatureItem<ItemCrated> CRATED_SPRUCE_SAPLING = register(Items.SPRUCE_SAPLING, "crated_spruce_sapling");
	public static final FeatureItem<ItemCrated> CRATED_ACACIA_SAPLING = register(Items.ACACIA_SAPLING, "crated_acacia_sapling");
	public static final FeatureItem<ItemCrated> CRATED_DARK_OAK_SAPLING = register(Items.DARK_OAK_SAPLING, "crated_dark_oak_sapling");

	public static final FeatureItem<ItemCrated> CRATED_BEESWAX = register(CoreItems.BEESWAX, "crated_beeswax");
	public static final FeatureItem<ItemCrated> CRATED_REFRACTORY_WAX = register(CoreItems.REFRACTORY_WAX, "crated_refractory_wax");

	// Honeydew is a core item since phase 2, so its crate stays here. The rest of the bee crates
	// live in ApicultureCrates and register through registerCrate below
	public static final FeatureItem<ItemCrated> CRATED_HONEYDEW = register(CoreItems.HONEYDEW, "crated_honeydew");

	/**
	 * Used by another module to register a crate for an item it owns. The crate is registered through
	 * this registry, so its id and generated models are the same as any other crate, and it is added
	 * to {@link #getCrates()} so model datagen picks it up.
	 *
	 * @param contained  The item the crate holds
	 * @param identifier The registry path, ex. "crated_propolis"
	 * @return The registered crate
	 */
	public static FeatureItem<ItemCrated> registerCrate(ItemLike contained, String identifier) {
		return register(contained, identifier);
	}

	/**
	 * @return The crate registry, for a module registering a crate group of its own
	 */
	public static IFeatureRegistry registry() {
		return REGISTRY;
	}

	/**
	 * Used to add a crate group's members to the crate list after the group is built.
	 *
	 * @param crates The group's crates
	 */
	public static void addCrates(java.util.Collection<FeatureItem<ItemCrated>> crates) {
		CRATES.addAll(crates);
	}

	private static FeatureItem<ItemCrated> register(ItemLike contained, String identifier) {
		FeatureItem<ItemCrated> item = REGISTRY.item(() -> new ItemCrated(() -> new ItemStack(contained)), identifier);
		CRATES.add(item);
		return item;
	}

	public static List<FeatureItem<ItemCrated>> getCrates() {
		return CRATES;
	}
}
