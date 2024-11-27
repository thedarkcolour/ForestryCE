package forestry.core.items.definitions;

import java.util.Locale;

import forestry.api.core.IItemSubtype;

public enum EnumCraftingMaterial implements IItemSubtype {
	PULSATING_DUST,
	PULSATING_MESH,
	WOOD_PULP,
	BEESWAX,
	REFRACTORY_WAX,
	SILK_WISP,
	WOVEN_SILK,
	ICE_SHARD,
	PHOSPHOR,
	IMPREGNATED_STICK,
	SCENTED_PANELING;

	private final String name;

	EnumCraftingMaterial() {
		this.name = toString().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
