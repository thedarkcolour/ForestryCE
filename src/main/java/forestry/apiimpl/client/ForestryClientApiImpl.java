package forestry.apiimpl.client;

import forestry.api.client.IForestryClientApi;
import forestry.api.client.ITextureManager;
import forestry.api.client.apiculture.IBeeClientManager;
import forestry.api.client.arboriculture.ITreeClientManager;
import forestry.api.client.genetics.IGeneticClientManager;
import forestry.api.client.lepidopterology.IButterflyClientManager;
import forestry.api.client.plugin.IClientHelper;
import forestry.apiimpl.client.plugin.ClientHelper;
import forestry.core.render.ForestryTextureManager;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class ForestryClientApiImpl implements IForestryClientApi {
	private final IClientHelper helper = new ClientHelper();

	@Nullable
	private ITextureManager textureManager;
	@Nullable
	private IGeneticClientManager geneticManager;
	@Nullable
	private IBeeClientManager beeManager;
	@Nullable
	private ITreeClientManager treeManager;
	@Nullable
	private IButterflyClientManager butterflyManager;

	@Override
	public ITextureManager getTextureManager() {
		if (this.textureManager == null) {
			throw new IllegalStateException("ITextureManager not initialized yet. Please wait until Minecraft constructor has been called");
		}
		return this.textureManager;
	}

	@Override
	public IGeneticClientManager getGeneticManager() {
		IGeneticClientManager manager = this.geneticManager;
		if (manager == null) {
			throw new IllegalStateException("IGeneticClientManager not initialized yet");
		}
		return manager;
	}

	public IBeeClientManager getBeeManager() {
		IBeeClientManager manager = this.beeManager;
		if (manager == null) {
			throw new IllegalStateException("IBeeClientManager not initialized yet");
		}
		return manager;
	}

	@Override
	public ITreeClientManager getTreeManager() {
		ITreeClientManager manager = this.treeManager;
		if (manager == null) {
			throw new IllegalStateException("ITreeClientManager not initialized yet");
		}
		return manager;
	}

	@Override
	public IButterflyClientManager getButterflyManager() {
		IButterflyClientManager manager = this.butterflyManager;
		if (manager == null) {
			throw new IllegalStateException("IButterflyClientManager not initialized yet");
		}
		return manager;
	}

	@Override
	public IClientHelper getHelper() {
		return this.helper;
	}

	// Must be called after textureManager is initialized in Minecraft's constructor.
	public void initializeTextureManager(RegisterClientReloadListenersEvent event) {
		this.textureManager = new ForestryTextureManager();
	}

	@ApiStatus.Internal
	public void setGeneticsManager(IGeneticClientManager treeManager) {
		this.geneticManager = treeManager;
	}

	@ApiStatus.Internal
	public void setTreeManager(ITreeClientManager treeManager) {
		this.treeManager = treeManager;
	}

	@ApiStatus.Internal
	public void setButterflyManager(IButterflyClientManager butterflyManager) {
		this.butterflyManager = butterflyManager;
	}

	@ApiStatus.Internal
	public void setBeeManager(BeeClientManager beeManager) {
		this.beeManager = beeManager;
	}
}
