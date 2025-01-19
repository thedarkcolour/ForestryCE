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

import net.minecraft.util.StringRepresentable;

import forestry.api.core.IBlockSubtype;
import forestry.api.core.IItemSubtype;

public enum EnumHoneyComb implements StringRepresentable, IItemSubtype, IBlockSubtype {
	HONEY(new Color(0xe8d56a), new Color(0xffa12b)),
	COCOA(new Color(0x674016), new Color(0xffb62b)),
	SIMMERING(new Color(0x981919), new Color(0xffb62b)),
	STRINGY(new Color(0xc8be67), new Color(0xbda93e)),
	FROZEN(new Color(0xf9ffff), new Color(0xa0ffff)),
	DRIPPING(new Color(0xdc7613), new Color(0xffff00)),
	SILKY(new Color(0x508907), new Color(0xddff00)),
	PARCHED(new Color(0xdcbe13), new Color(0xffff00)),
	MYSTERIOUS(new Color(0x161616), new Color(0xe099ff)),
	POWDERY(new Color(0x676767), new Color(0xffffff)),
	WHEATEN(new Color(0xfeff8f), new Color(0xffffff)),
	MOSSY(new Color(0x2a3313), new Color(0x7e9939)),
	MELLOW(new Color(0x886000), new Color(0xfff960)),
	KAOLIN(new Color(0x5e6c8d), new Color(0xafb9d6)),
	VINTAGE(new Color(0xDEB887), new Color(0xCD853F)),
	SPONGE(new Color(0x9D8F39), new Color(0xE1E351)),
	SCULKEN(new Color(0x111B21), new Color(0x05625D));
	//""(new Color(0xd7bee5), new Color(0xfd58ab)); // kindof pinkish

	public static final EnumHoneyComb[] VALUES = values();

	public final String name;
	public final int primaryColor;
	public final int secondaryColor;
	private final boolean unused;

	EnumHoneyComb(Color primary, Color secondary) {
		this(primary, secondary, false);
	}

	EnumHoneyComb(Color primary, Color secondary, boolean unused) {
		this.unused = unused;
		this.name = toString().toLowerCase(Locale.ENGLISH);
		this.primaryColor = primary.getRGB();
		this.secondaryColor = secondary.getRGB();
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public static EnumHoneyComb get(int meta) {
		if (meta >= VALUES.length) {
			meta = 0;
		}
		return VALUES[meta];
	}
}
