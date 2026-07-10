package forestry.api.lepidopterology;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static forestry.api.ForestryConstants.forestry;

/**
 * All butterfly species registered by base Forestry.
 */
public class ForestryButterflySpecies {
	// Butterflies
	public static final ResourceLocation CABBAGE_WHITE = forestry("cabbage_white");
	public static final ResourceLocation BRIMSTONE = forestry("brimstone");
	public static final ResourceLocation AURORA = forestry("aurora");
	public static final ResourceLocation CLOUDED_YELLOW = forestry("clouded_yellow");
	public static final ResourceLocation PALAENO_SULPHUR = forestry("palaeno_sulphur");
	public static final ResourceLocation RESEDA = forestry("reseda");
	public static final ResourceLocation SPRING_AZURE = forestry("spring_azure");
	public static final ResourceLocation GOZORA_AZURE = forestry("gozora_azure");
	public static final ResourceLocation CITRUS_SWALLOWTAIL = forestry("citrus_swallow");
	public static final ResourceLocation EMERALD_PEACOCK = forestry("emerald_peacock");
	public static final ResourceLocation THOAS_SWALLOWTAIL = forestry("thoas_swallow");
	public static final ResourceLocation SPICEBUSH_SWALLOWTAIL = forestry("spicebush_swallow");
	public static final ResourceLocation BLACK_SWALLOWTAIL = forestry("black_swallow");
	public static final ResourceLocation ZEBRA_SWALLOWTAIL = forestry("zebra_swallow");
	public static final ResourceLocation GLASSWING = forestry("glasswing");
	public static final ResourceLocation SPECKLED_WOOD = forestry("speckled_wood");
	public static final ResourceLocation MADEIRAN_SPECKLED_WOOD = forestry("mspeckled_wood");
	public static final ResourceLocation CANARY_SPECKLED_WOOD = forestry("cspeckled_wood");
	public static final ResourceLocation MENELAUS_BLUE_MORPHO = forestry("mbluemorpho");
	public static final ResourceLocation PELEIDES_BLUE_MORPHO = forestry("pbluemorpho");
	public static final ResourceLocation RHETENOR_BLUE_MORPHO = forestry("rbluemorpho");
	public static final ResourceLocation COMMA = forestry("comma");
	public static final ResourceLocation BATESIA = forestry("batesia");
	public static final ResourceLocation BLUE_WING = forestry("blue_wing");
	public static final ResourceLocation MONARCH = forestry("monarch");
	public static final ResourceLocation BLUE_DUKE = forestry("blue_duke");
	public static final ResourceLocation GLASSY_TIGER = forestry("glassy_tiger");
	public static final ResourceLocation POSTMAN = forestry("postman");
	public static final ResourceLocation MALACHITE = forestry("malachite");
	public static final ResourceLocation LEOPARD_LACEWING = forestry("leopard_lacewing");
	public static final ResourceLocation DIANA_FRITILLARY = forestry("diana_fritillary");

	// Moths
	public static final ResourceLocation BRIMSTONE_MOTH = forestry("brimstone_moth");
	public static final ResourceLocation LATTICED_HEATH = forestry("latticed_heath");
	public static final ResourceLocation ATLAS = forestry("atlas");
	public static final ResourceLocation BOMBYX_MORI = forestry("bombyx_mori");

	/**
	 * All built-in butterfly species ids, in declaration order. Compile-time constant list (not a view of the
	 * reloadable species registry), safe to iterate at client-registration time.
	 */
	public static final List<ResourceLocation> ALL = List.of(
		CABBAGE_WHITE, BRIMSTONE, AURORA, CLOUDED_YELLOW, PALAENO_SULPHUR, RESEDA, SPRING_AZURE, GOZORA_AZURE,
		CITRUS_SWALLOWTAIL, EMERALD_PEACOCK, THOAS_SWALLOWTAIL, SPICEBUSH_SWALLOWTAIL, BLACK_SWALLOWTAIL,
		ZEBRA_SWALLOWTAIL, GLASSWING, SPECKLED_WOOD, MADEIRAN_SPECKLED_WOOD, CANARY_SPECKLED_WOOD,
		MENELAUS_BLUE_MORPHO, PELEIDES_BLUE_MORPHO, RHETENOR_BLUE_MORPHO, COMMA, BATESIA, BLUE_WING, MONARCH,
		BLUE_DUKE, GLASSY_TIGER, POSTMAN, MALACHITE, LEOPARD_LACEWING, DIANA_FRITILLARY,
		BRIMSTONE_MOTH, LATTICED_HEATH, ATLAS, BOMBYX_MORI
	);
}
