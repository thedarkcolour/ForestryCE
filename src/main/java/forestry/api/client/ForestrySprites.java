package forestry.api.client;

import net.minecraft.resources.ResourceLocation;

import forestry.api.ForestryConstants;

/**
 * All sprites loaded by forestry for use in the {@link ITextureManager}.
 * SLOT_* sprites are also available on the block atlas.
 */
public class ForestrySprites {
	/**
	 * Used for menu rendering with {@link com.mojang.blaze3d.systems.RenderSystem#setShaderTexture(int, ResourceLocation)}
	 */
	public static final ResourceLocation TEXTURE_ATLAS = ForestryConstants.forestry("textures/atlas/gui.png");
	
	public static final ResourceLocation HABITAT_DESERT = ForestryConstants.forestry("habitats/desert");
	public static final ResourceLocation HABITAT_END = ForestryConstants.forestry("habitats/end");
	public static final ResourceLocation HABITAT_FOREST = ForestryConstants.forestry("habitats/forest");
	public static final ResourceLocation HABITAT_HILLS = ForestryConstants.forestry("habitats/hills");
	public static final ResourceLocation HABITAT_JUNGLE = ForestryConstants.forestry("habitats/jungle");
	public static final ResourceLocation HABITAT_MUSHROOM = ForestryConstants.forestry("habitats/mushroom");
	public static final ResourceLocation HABITAT_NETHER = ForestryConstants.forestry("habitats/nether");
	public static final ResourceLocation HABITAT_OCEAN = ForestryConstants.forestry("habitats/ocean");
	public static final ResourceLocation HABITAT_PLAINS = ForestryConstants.forestry("habitats/plains");
	public static final ResourceLocation HABITAT_SNOW = ForestryConstants.forestry("habitats/snow");
	public static final ResourceLocation HABITAT_SWAMP = ForestryConstants.forestry("habitats/swamp");
	public static final ResourceLocation HABITAT_TAIGA = ForestryConstants.forestry("habitats/taiga");
	public static final ResourceLocation MISC_ACCESS_SHARED = ForestryConstants.forestry("misc/access.shared");
	public static final ResourceLocation MISC_ENERGY = ForestryConstants.forestry("misc/energy");
	public static final ResourceLocation MISC_HINT = ForestryConstants.forestry("misc/hint");
	public static final ResourceLocation ANALYZER_ANYTHING = ForestryConstants.forestry("analyzer/anything");
	public static final ResourceLocation ANALYZER_BEE = ForestryConstants.forestry("analyzer/bee");
	public static final ResourceLocation ANALYZER_CAVE = ForestryConstants.forestry("analyzer/cave");
	public static final ResourceLocation ANALYZER_CLOSED = ForestryConstants.forestry("analyzer/closed");
	public static final ResourceLocation ANALYZER_DRONE = ForestryConstants.forestry("analyzer/drone");
	public static final ResourceLocation ANALYZER_FLYER = ForestryConstants.forestry("analyzer/flyer");
	public static final ResourceLocation ANALYZER_ITEM = ForestryConstants.forestry("analyzer/item");
	public static final ResourceLocation ANALYZER_NOCTURNAL = ForestryConstants.forestry("analyzer/nocturnal");
	public static final ResourceLocation ANALYZER_PRINCESS = ForestryConstants.forestry("analyzer/princess");
	public static final ResourceLocation ANALYZER_PURE_BREED = ForestryConstants.forestry("analyzer/pure_breed");
	public static final ResourceLocation ANALYZER_PURE_CAVE = ForestryConstants.forestry("analyzer/pure_cave");
	public static final ResourceLocation ANALYZER_PURE_FLYER = ForestryConstants.forestry("analyzer/pure_flyer");
	public static final ResourceLocation ANALYZER_PURE_NOCTURNAL = ForestryConstants.forestry("analyzer/pure_nocturnal");
	public static final ResourceLocation ANALYZER_QUEEN = ForestryConstants.forestry("analyzer/queen");
	public static final ResourceLocation ANALYZER_TREE = ForestryConstants.forestry("analyzer/tree");
	public static final ResourceLocation ANALYZER_SAPLING = ForestryConstants.forestry("analyzer/sapling");
	public static final ResourceLocation ANALYZER_POLLEN = ForestryConstants.forestry("analyzer/pollen");
	public static final ResourceLocation ANALYZER_FLUTTER = ForestryConstants.forestry("analyzer/flutter");
	public static final ResourceLocation ANALYZER_BUTTERFLY = ForestryConstants.forestry("analyzer/butterfly");
	public static final ResourceLocation ANALYZER_SERUM = ForestryConstants.forestry("analyzer/serum");
	public static final ResourceLocation ANALYZER_CATERPILLAR = ForestryConstants.forestry("analyzer/caterpillar");
	public static final ResourceLocation ANALYZER_COCOON = ForestryConstants.forestry("analyzer/cocoon");
	public static final ResourceLocation ERROR_ERRORED = ForestryConstants.forestry("errors/errored");
	public static final ResourceLocation ERROR_UNKNOWN = ForestryConstants.forestry("errors/unknown");
	public static final ResourceLocation SLOT_BLOCKED = ForestryConstants.forestry("slots/blocked");
	public static final ResourceLocation SLOT_BLOCKED_2 = ForestryConstants.forestry("slots/blocked_2");
	public static final ResourceLocation SLOT_LIQUID = ForestryConstants.forestry("slots/liquid");
	public static final ResourceLocation SLOT_CONTAINER = ForestryConstants.forestry("slots/container");
	public static final ResourceLocation SLOT_LOCKED = ForestryConstants.forestry("slots/locked");
	public static final ResourceLocation SLOT_COCOON = ForestryConstants.forestry("slots/cocoon");
	public static final ResourceLocation SLOT_BEE = ForestryConstants.forestry("slots/bee");
	public static final ResourceLocation MAIL_CARRIER_PLAYER = ForestryConstants.forestry("mail/carrier.player");
	public static final ResourceLocation MAIL_CARRIER_TRADER = ForestryConstants.forestry("mail/carrier.trader");
}
