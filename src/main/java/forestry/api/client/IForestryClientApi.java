package forestry.api.client;

import java.util.ServiceLoader;

import forestry.api.client.apiculture.IBeeClientManager;
import forestry.api.client.arboriculture.ITreeClientManager;
import forestry.api.client.genetics.IGeneticClientManager;
import forestry.api.client.lepidopterology.IButterflyClientManager;
import forestry.api.client.plugin.IClientHelper;

/**
 * The Forestry Client API manages client-only data related to Forestry.
 */
public interface IForestryClientApi {
	IForestryClientApi INSTANCE = ServiceLoader.load(IForestryClientApi.class).findFirst().orElseThrow();

	ITextureManager getTextureManager();

	/**
	 * @return The client-only genetics manager, responsible Portable Analyzer plugins.
	 * @since 2.9.0
	 */
	IGeneticClientManager getGeneticManager();

	IBeeClientManager getBeeManager();

	ITreeClientManager getTreeManager();

	IButterflyClientManager getButterflyManager();

	/**
	 * @return The IClientHelper containing methods to create instances of Forestry's client objects.
	 * @since 1.0.5
	 */
	IClientHelper getHelper();
}
