package forestry.core.items.definitions;

import forestry.core.items.ItemOverlay;
import net.minecraft.network.chat.TextColor;

import java.util.Locale;

public enum EnumElectronTube implements ItemOverlay.IOverlayInfo {
	COPPER(TextColor.fromRgb(0xe3b78e)),
	TIN(TextColor.fromRgb(0xE6F8FF)),
	BRONZE(TextColor.fromRgb(0xddc276)),
	IRON(TextColor.fromRgb(0xCCCCCC)),
	GOLD(TextColor.fromRgb(0xffff8b)),
	DIAMOND(TextColor.fromRgb(0x8CF5E3)),
	OBSIDIAN(TextColor.fromRgb(0x866bc0)),
	BLAZE(TextColor.fromRgb(0xd96600), TextColor.fromRgb(0xFFF87E)),
	EMERALD(TextColor.fromRgb(0x00CC41)),
	APATITE(TextColor.fromRgb(0x579CD9)),
	LAPIS(TextColor.fromRgb(0x1c57c6)),
	ENDER(TextColor.fromRgb(0x33adad), TextColor.fromRgb(0x255661)),
	AMBER(TextColor.fromRgb(0xE29536));

	private final String uid;
	private final int primaryColor;
	private final int secondaryColor;

	EnumElectronTube(TextColor secondaryColor) {
		this(secondaryColor, TextColor.fromRgb(0xffffff));
	}

	EnumElectronTube(TextColor secondaryColor, TextColor primaryColor) {
		this.uid = name().toLowerCase(Locale.ENGLISH);
		this.primaryColor = primaryColor.getValue();
		this.secondaryColor = secondaryColor.getValue();
	}

	@Override
	public String getSerializedName() {
		return this.uid;
	}

	@Override
	public int getPrimaryColor() {
		return this.primaryColor;
	}

	@Override
	public int getSecondaryColor() {
		return this.secondaryColor;
	}
}
