package forestry.core.content.decorative;

import forestry.api.core.IBlockSubtype;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * The nineteen jumbo candles, one full block each.
 */
public enum JumboCandleBlockType implements IBlockSubtype {
	NORMAL(MapColor.SAND, null),
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
	LIGHT_BLUE(MapColor.COLOR_LIGHT_BLUE, Tags.Items.DYES_LIGHT_BLUE),
	BLUE(MapColor.COLOR_BLUE, Tags.Items.DYES_BLUE),
	PURPLE(MapColor.COLOR_PURPLE, Tags.Items.DYES_PURPLE),
	MAGENTA(MapColor.COLOR_MAGENTA, Tags.Items.DYES_MAGENTA),
	PINK(MapColor.COLOR_PINK, Tags.Items.DYES_PINK),
	REFRACTORY(MapColor.COLOR_RED, null),
	RAINBOW(MapColor.COLOR_MAGENTA, null);

	private final MapColor color;
	@Nullable
	private final TagKey<Item> dye;

	JumboCandleBlockType(MapColor color, @Nullable TagKey<Item> dye) {
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
	 * @return The dye tag the dyeing recipe takes, or null where the candle is made from wax
	 */
	@Nullable
	public TagKey<Item> getDye() {
		return this.dye;
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}
