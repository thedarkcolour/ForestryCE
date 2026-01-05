package forestry.core.items.definitions;

import forestry.api.core.IItemSubtype;

import java.util.Locale;

public enum EnumContainerType implements IItemSubtype {
	CAN,
	CAPSULE,
	REFRACTORY;

	private final String name;

	EnumContainerType() {
		this.name = name().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
