package forestry.core.content.resources;

import forestry.api.core.IItemSubtype;

import java.util.Locale;

public enum EnumCraftingMaterial implements IItemSubtype {
	PULSATING_MESH,
	WOOD_PULP,
	SILK_WISP,
	WOVEN_SILK,
	ICE_SHARD,
	PHOSPHOR,
	PHOSPHORESCENT_JELLY,
	IMPREGNATED_STICK,
	SCENTED_PANELING,
	;

	private final String name;

	EnumCraftingMaterial() {
		this.name = toString().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
