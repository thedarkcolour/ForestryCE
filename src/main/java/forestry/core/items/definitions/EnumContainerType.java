package forestry.core.items.definitions;

import forestry.api.core.IItemSubtype;

import java.util.Locale;

public enum EnumContainerType implements IItemSubtype {
	CAN,
	CAPSULE,
	REFRACTORY;

	@Override
	public String identifier() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}
