package forestry.apiimpl.client.plugin;

import com.mojang.datafixers.util.Pair;
import forestry.api.client.arboriculture.ILeafSprite;
import forestry.api.client.arboriculture.ILeafTint;
import forestry.api.client.genetics.IAnalyzerPlugin;
import forestry.api.client.plugin.IClientRegistration;
import forestry.api.genetics.ILifeStage;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class ClientRegistration implements IClientRegistration {
	// ID -> (life stage -> bee model)
	private final IdentityHashMap<ILifeStage, Map<ResourceLocation, ResourceLocation>> beeModels = new IdentityHashMap<>();
	// life stage -> bee model
	private final IdentityHashMap<ILifeStage, ResourceLocation> defaultBeeModels = new IdentityHashMap<>();
	// ID -> (butterfly item texture, entity texture)
	private final HashMap<ResourceLocation, Pair<ResourceLocation, ResourceLocation>> butterflyTextures = new HashMap<>();
	// ID -> (sapling block model, item model)
	private final HashMap<ResourceLocation, Pair<ResourceLocation, ResourceLocation>> saplingModels = new HashMap<>();
	// ID -> leaf sprite
	private final HashMap<ResourceLocation, ILeafSprite> leafSprites = new HashMap<>();
	// ID -> leaf tints
	private final HashMap<ResourceLocation, ILeafTint> leafTints = new HashMap<>();
	// ID -> Analyzer plugin
	private final HashMap<ResourceLocation, IAnalyzerPlugin<?, ?>> analyzerPlugins = new HashMap<>();

	@Override
	public void setDefaultBeeModel(ILifeStage stage, ResourceLocation modelLocation) {
		this.defaultBeeModels.put(stage, modelLocation);
	}

	@Override
	public void setCustomBeeModel(ResourceLocation speciesId, ILifeStage stage, ResourceLocation model) {
		this.beeModels.computeIfAbsent(stage, k -> new HashMap<>()).put(speciesId, model);
	}

	@Override
	public void setSaplingModel(ResourceLocation speciesId, ResourceLocation blockModel, ResourceLocation itemModel) {
		this.saplingModels.put(speciesId, Pair.of(blockModel, itemModel));
	}

	@Override
	public void setLeafSprite(ResourceLocation speciesId, ILeafSprite sprite) {
		this.leafSprites.put(speciesId, sprite);
	}

	@Override
	public void setLeafTint(ResourceLocation speciesId, ILeafTint tint) {
		this.leafTints.put(speciesId, tint);
	}

	@Override
	public void setButterflySprites(ResourceLocation speciesId, ResourceLocation itemTexture, ResourceLocation entityTexture) {
		this.butterflyTextures.put(speciesId, Pair.of(itemTexture, entityTexture));
	}

	@Override
	public void setAnalyzerPlugin(ResourceLocation speciesTypeId, IAnalyzerPlugin<?, ?> plugin) {
		this.analyzerPlugins.put(speciesTypeId, plugin);
	}

	public Map<ILifeStage, Map<ResourceLocation, ResourceLocation>> getBeeModels() {
		return this.beeModels;
	}

	public HashMap<ResourceLocation, Pair<ResourceLocation, ResourceLocation>> getSaplingModels() {
		return this.saplingModels;
	}

	public HashMap<ResourceLocation, ILeafSprite> getLeafSprites() {
		return this.leafSprites;
	}

	public HashMap<ResourceLocation, ILeafTint> getTints() {
		return this.leafTints;
	}

	public HashMap<ResourceLocation, Pair<ResourceLocation, ResourceLocation>> getButterflyTextures() {
		return this.butterflyTextures;
	}

	public ResourceLocation getDefaultBeeModel(ILifeStage stage) {
		return this.defaultBeeModels.get(stage);
	}

	public HashMap<ResourceLocation, IAnalyzerPlugin<?, ?>> getAnalyzerPlugins() {
		return this.analyzerPlugins;
	}
}
