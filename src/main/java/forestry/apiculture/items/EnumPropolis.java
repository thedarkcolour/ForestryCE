package forestry.apiculture.items;

import forestry.core.items.ItemOverlay;

import java.util.Locale;

public enum EnumPropolis implements ItemOverlay.IOverlayInfo {
	NORMAL(0xc5b24e),
	PULSATING(0x2ccdb1),
	SILKY(0xddff00),
	VOLCANIC(0xE84528);

	private final String name;
	private final int color;

	EnumPropolis(int color) {
		this.name = toString().toLowerCase(Locale.ENGLISH);
		this.color = color;
	}

	@Override
	public String identifier() {
		return this.name;
	}

	@Override
	public int getPrimaryColor() {
		return this.color;
	}

	@Override
	public int getSecondaryColor() {
		return 0;
	}
}
