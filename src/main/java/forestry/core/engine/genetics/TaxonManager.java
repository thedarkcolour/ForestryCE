package forestry.core.engine.genetics;

public final class TaxonManager extends DefinitionManager<TaxonDefinition> {
	public static final TaxonManager INSTANCE = new TaxonManager();

	private TaxonManager() {
		super("taxon", TaxonDefinition.CODEC, definitions -> GeneticsReloadHandler.rebuildTaxa(definitions.values()));
	}
}
