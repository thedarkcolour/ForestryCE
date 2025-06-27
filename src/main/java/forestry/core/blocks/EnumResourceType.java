package forestry.core.blocks;

import forestry.api.core.IBlockSubtype;

import java.util.Locale;

public enum EnumResourceType implements IBlockSubtype {
	APATITE,
	TIN,
	BRONZE;

	@Override
	public String identifier() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}
