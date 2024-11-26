package forestry.core.render;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;

import forestry.api.ForestryConstants;
import forestry.api.client.ForestrySprites;

/**
 * Uploads the forestry gui icon texture sprites to the forestry gui atlas texture.
 *
 * @see ForestryTextureManager
 */
public class ForestrySpriteUploader extends TextureAtlasHolder {
	public static final ResourceLocation ATLAS_PATH = ForestryConstants.forestry("gui");

	public ForestrySpriteUploader(TextureManager manager) {
		super(manager, ForestrySprites.TEXTURE_ATLAS, ATLAS_PATH);
	}

	// Public override
	@Override
	public TextureAtlasSprite getSprite(ResourceLocation location) {
		return super.getSprite(location);
	}
}
