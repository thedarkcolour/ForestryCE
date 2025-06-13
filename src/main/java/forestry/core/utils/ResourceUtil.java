package forestry.core.utils;

import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.SimpleModelState;

/**
 * Util methods used at the installation of the game or at the reloading or baking of resources like models or
 * textures.
 */
public class ResourceUtil {
	public static TextureAtlasSprite getMissingTexture() {
		return getSprite(InventoryMenu.BLOCK_ATLAS, MissingTextureAtlasSprite.getLocation());
	}

	public static TextureAtlasSprite getSprite(ResourceLocation atlas, ResourceLocation sprite) {
		return Minecraft.getInstance().getTextureAtlas(atlas).apply(sprite);
	}

	public static TextureAtlasSprite getBlockSprite(ResourceLocation location) {
		return getSprite(InventoryMenu.BLOCK_ATLAS, location);
	}

	public static TextureAtlasSprite getBlockSprite(String location) {
		return getBlockSprite(ResourceLocation.parse(location));
	}

	public static SimpleModelState loadTransform(ResourceLocation location) {
		// todo
		return new SimpleModelState(Transformation.identity());
	}
}
