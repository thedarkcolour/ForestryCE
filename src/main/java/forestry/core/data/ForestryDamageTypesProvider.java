package forestry.core.data;

import forestry.core.damage.CoreDamageTypes;

import thedarkcolour.modkit.data.MKDamageTypeProvider;

public class ForestryDamageTypesProvider {
	public static void addTypes(MKDamageTypeProvider provider) {
		provider.add(CoreDamageTypes.AGGRESSIVE);
		provider.add(CoreDamageTypes.HEROIC);
		provider.add(CoreDamageTypes.MISANTHROPE);
		provider.add(CoreDamageTypes.RADIOACTIVE);
		provider.add(CoreDamageTypes.HIVE);
		provider.add(CoreDamageTypes.CLOCKWORK);
	}
}
