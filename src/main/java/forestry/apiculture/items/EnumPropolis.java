package forestry.apiculture.items;

import forestry.core.items.ItemOverlay;

import java.awt.*;
import java.util.Locale;

public enum EnumPropolis implements ItemOverlay.IOverlayInfo {
	NORMAL(new Color(0xc5b24e)),
	PULSATING(new Color(0x2ccdb1)),
	SILKY(new Color(0xddff00)),
	// todo remove in 1.21.1
	VOLCANIC(new Color(0xE84528));

	public static final EnumPropolis[] VALUES = values();

	private final String name;
	private final int primaryColor;

	EnumPropolis(Color color) {
		this.name = toString().toLowerCase(Locale.ENGLISH);
		this.primaryColor = color.getRGB();
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	@Override
	public int getPrimaryColor() {
		return this.primaryColor;
	}

	@Override
	public int getSecondaryColor() {
		return 0;
	}
}
