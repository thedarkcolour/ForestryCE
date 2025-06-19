package forestry.core.render;

import forestry.api.client.ITextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class ForestryTextureManager implements ITextureManager {
	private final ForestrySpriteUploader uploader = new ForestrySpriteUploader(Minecraft.getInstance().getTextureManager());

	public ForestrySpriteUploader getSpriteUploader() {
		return this.uploader;
	}

	@Override
	public TextureAtlasSprite getSprite(ResourceLocation location) {
		return this.uploader.getSprite(location);
	}
}
