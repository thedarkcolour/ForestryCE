package forestry.storage.items;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum EnumBackpackType implements StringRepresentable {
	NORMAL, WOVEN, NATURALIST;

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}
