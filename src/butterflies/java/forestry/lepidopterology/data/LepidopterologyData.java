package forestry.lepidopterology.data;

import java.util.Set;

import net.minecraft.core.registries.Registries;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.data.ContentJarData;
import forestry.core.data.DataRoots;
import forestry.core.data.JarLootTableProvider;
import forestry.core.data.JarScope;
import forestry.core.platform.util.SpeciesUtil;
import forestry.lepidopterology.butterflies.genetics.ButterflySpeciesProvider;
import forestry.lepidopterology.plugin.DefaultButterflySpecies;

/**
 * Registers every provider that writes into the butterflies jar. Loaded by core through
 * {@code META-INF/services/forestry.core.data.IForestryDataProvider}.
 */
public class LepidopterologyData extends ContentJarData {
	public LepidopterologyData() {
		super("butterflies", DataRoots.BUTTERFLIES, Set.of(ForestryModuleIds.LEPIDOPTEROLOGY));
	}

	@Override
	protected void addProviders(JarScope jar) {
		// Butterfly taxa must be live before any butterfly species is built, because a species resolves
		// its genus through the taxonomy
		ButterflyTaxonProvider.seedLiveTaxaForDatagen();
		ButterflySpeciesProvider.seedLiveSpeciesForDatagen();

		jar.helper().createTags(Registries.ITEM, LepidopterologyItemTagsProvider::addTags);
		jar.helper().createRecipes(LepidopterologyRecipeProvider::addRecipes);

		jar.addServer(new ButterflyTaxonProvider(jar.output()));
		jar.addServer(SpeciesUtil.BUTTERFLY_TYPE.get().createSpeciesProvider(jar.output(), jar.lookup(), DefaultButterflySpecies::register));
		jar.addServer(new ButterflyMutationProvider(jar.output(), jar.lookup()));
		jar.addServer(new JarLootTableProvider(jar.output(), jar.lookup(), LepidopterologyBlockLootTables::new));
		jar.addServer(new LepidopterologyDataMapProvider(jar.output(), jar.lookup()));
		jar.addClient(new LepidopterologyItemModelProvider(jar.output(), jar.existingFileHelper()));
	}
}
