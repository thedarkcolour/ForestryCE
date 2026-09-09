package forestry.agriculture.data;

import java.util.Set;

import net.minecraft.core.registries.Registries;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.data.ContentJarData;
import forestry.core.data.DataRoots;
import forestry.core.data.JarLootTableProvider;
import forestry.core.data.JarScope;

/**
 * Registers every provider that writes into the farms jar. Loaded by core through
 * {@code META-INF/services/forestry.core.data.IForestryDataProvider}.
 */
public class AgricultureData extends ContentJarData {
	public AgricultureData() {
		super("farms", DataRoots.FARMS, Set.of(ForestryModuleIds.FARMING, ForestryModuleIds.CULTIVATION));
	}

	@Override
	protected void addProviders(JarScope jar) {
		jar.helper().createTags(Registries.BLOCK, AgricultureBlockTagsProvider::addTags);
		jar.helper().createRecipes(AgricultureRecipeProvider::addRecipes);

		jar.addServer(new JarLootTableProvider(jar.output(), jar.lookup(), AgricultureBlockLootTables::new));
		jar.addServer(new AgricultureAdvancementProvider(jar.output(), jar.lookup(), jar.existingFileHelper()));
		jar.addServer(new AgricultureDataMapProvider(jar.output(), jar.lookup()));
		jar.addClient(new AgricultureBlockStateProvider(jar.output(), jar.existingFileHelper()));
	}
}
