package forestry.api.core;

public interface IFeatureSubtype {
	/**
	 * @return The identifier associated with this subtype, without any group prefix or suffix (ex. vintage for comb_vintage)
	 */
	String identifier();
}
