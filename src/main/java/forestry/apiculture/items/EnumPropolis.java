/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.apiculture.items;

import java.awt.Color;
import java.util.Locale;

import forestry.core.items.ItemOverlay;

public enum EnumPropolis implements ItemOverlay.IOverlayInfo {
	NORMAL(new Color(0xc5b24e)),
	PULSATING(new Color(0x2ccdb1)),
	SILKY(new Color(0xddff00));

	public static final EnumPropolis[] VALUES = values();

	private final String name;
	private final int primaryColor;

	EnumPropolis(Color color) {
		this.name = toString().toLowerCase(Locale.ENGLISH);
		this.primaryColor = color.getRGB();
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	@Override
	public int getPrimaryColor() {
		return primaryColor;
	}

	@Override
	public int getSecondaryColor() {
		return 0;
	}
}
