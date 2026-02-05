package forestry.core.blocks;

import forestry.api.core.IBlockSubtype;

import java.util.Locale;

public enum EnumResourceType implements IBlockSubtype {
	APATITE,
	TIN,
	BRONZE,
	AMBER;

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}
