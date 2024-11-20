package forestry.core.render;

import java.util.ArrayList;
import java.util.function.Consumer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;

import forestry.api.ForestryConstants;

/**
 * Uploads the forestry gui icon texture sprites to the forestry gui atlas texture.
 *
 * @see ForestryTextureManager
 */
public class ForestryAtlasHolder extends TextureAtlasHolder implements Consumer<ResourceLocation> {
	private final ArrayList<ResourceLocation> registeredSprites = new ArrayList<>();

	public ForestryAtlasHolder(TextureManager manager, ResourceLocation atlasLocation) {
		super(manager, atlasLocation, ForestryConstants.forestry("gui"));
	}

	public void accept(ResourceLocation location) {
		this.registeredSprites.add(location);
	}

	// Public override
	@Override
	public TextureAtlasSprite getSprite(ResourceLocation location) {
		return super.getSprite(location);
	}
}
