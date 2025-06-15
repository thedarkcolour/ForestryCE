package forestry.farming.blocks;

import forestry.api.core.IBlockSubtype;

import java.util.Locale;

public enum EnumFarmBlockType implements IBlockSubtype {
	PLAIN,
	GEARBOX,
	HATCH,
	VALVE,
	CONTROL;

	@Override
	public String identifier() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}
