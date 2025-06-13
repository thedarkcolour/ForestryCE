package forestry.core.items.definitions;

import forestry.api.core.IItemSubtype;

import java.util.Locale;

public enum EnumCraftingMaterial implements IItemSubtype {
	PULSATING_DUST,
	PULSATING_MESH,
	WOOD_PULP,
	SILK_WISP,
	WOVEN_SILK,
	ICE_SHARD,
	PHOSPHOR,
	IMPREGNATED_STICK,
	SCENTED_PANELING;

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}
