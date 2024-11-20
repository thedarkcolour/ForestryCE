package forestry.farming.blocks;

import java.util.Locale;

import forestry.api.core.IBlockSubtype;

public enum EnumFarmBlockType implements IBlockSubtype {
	PLAIN,
	GEARBOX,
	HATCH,
	VALVE,
	CONTROL;

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}
