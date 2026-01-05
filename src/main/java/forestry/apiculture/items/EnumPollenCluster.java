package forestry.apiculture.items;

import forestry.core.items.ItemOverlay;

import java.awt.*;
import java.util.Locale;

public enum EnumPollenCluster implements ItemOverlay.IOverlayInfo {
	NORMAL(new Color(0xa28a25), new Color(0xa28a25)),
	CRYSTALLINE(new Color(0xffffff), new Color(0xc5feff));

	private final String name;
	private final int primaryColor;
	private final int secondaryColor;

	EnumPollenCluster(Color primary, Color secondary) {
		this.name = toString().toLowerCase(Locale.ENGLISH);
		this.primaryColor = primary.getRGB();
		this.secondaryColor = secondary.getRGB();
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
		return this.secondaryColor;
	}
}
