package forestry.core.content.decorative;

import forestry.api.core.IBlockSubtype;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * The twenty-two metal plating blocks. The first six are cast from an ingot, the sixteen after them are
 * lacquered out of any other plating and one dye.
 */
public enum MetalPlatingBlockType implements IBlockSubtype {
	GOLD(MapColor.GOLD, null),
	IRON(MapColor.METAL, null),
	COPPER(MapColor.COLOR_ORANGE, null),
	TIN(MapColor.METAL, null),
	BRONZE(MapColor.TERRACOTTA_YELLOW, null),
	NETHERITE(MapColor.COLOR_BLACK, null),
	WHITE(MapColor.WOOL, Tags.Items.DYES_WHITE),
	LIGHT_GRAY(MapColor.COLOR_LIGHT_GRAY, Tags.Items.DYES_LIGHT_GRAY),
	GRAY(MapColor.COLOR_GRAY, Tags.Items.DYES_GRAY),
	BLACK(MapColor.COLOR_BLACK, Tags.Items.DYES_BLACK),
	BROWN(MapColor.COLOR_BROWN, Tags.Items.DYES_BROWN),
	RED(MapColor.COLOR_RED, Tags.Items.DYES_RED),
	ORANGE(MapColor.COLOR_ORANGE, Tags.Items.DYES_ORANGE),
	YELLOW(MapColor.COLOR_YELLOW, Tags.Items.DYES_YELLOW),
	LIME(MapColor.COLOR_LIGHT_GREEN, Tags.Items.DYES_LIME),
	GREEN(MapColor.COLOR_GREEN, Tags.Items.DYES_GREEN),
	CYAN(MapColor.COLOR_CYAN, Tags.Items.DYES_CYAN),
	BLUE(MapColor.COLOR_BLUE, Tags.Items.DYES_BLUE),
	LIGHT_BLUE(MapColor.COLOR_LIGHT_BLUE, Tags.Items.DYES_LIGHT_BLUE),
	PURPLE(MapColor.COLOR_PURPLE, Tags.Items.DYES_PURPLE),
	MAGENTA(MapColor.COLOR_MAGENTA, Tags.Items.DYES_MAGENTA),
	PINK(MapColor.COLOR_PINK, Tags.Items.DYES_PINK);

	private final MapColor color;
	@Nullable
	private final TagKey<Item> dye;

	// Deviation from 1.20.1: the map colors lived in a static HashMap built fresh on every call there, and
	// the dye tags in a second one. Both are fields here, so the recipe loop can walk values() in
	// declaration order rather than a hash order that datagen would write differently between runs
	MetalPlatingBlockType(MapColor color, @Nullable TagKey<Item> dye) {
		this.color = color;
		this.dye = dye;
	}

	/**
	 * @return The map color the block carries
	 */
	public MapColor getMapColor() {
		return this.color;
	}

	/**
	 * @return The dye tag the lacquering recipe takes, or null where the plating is cast from an ingot
	 */
	@Nullable
	public TagKey<Item> getDye() {
		return this.dye;
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ENGLISH);
	}

	/**
	 * Ex. {@code RED} -> {@code "red_metal_plating"}
	 *
	 * @return The name the recipe id is built from, which reads colour first
	 */
	public String getName() {
		return getSerializedName() + "_metal_plating";
	}
}
