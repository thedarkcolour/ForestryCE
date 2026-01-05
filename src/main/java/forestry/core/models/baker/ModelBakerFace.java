package forestry.core.models.baker;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A face of a {@link ModelBakerModel }
 */
@OnlyIn(Dist.CLIENT)
public class ModelBakerFace {
	public final Direction face;

	public final TextureAtlasSprite spite;

	public final int colorIndex;

	public ModelBakerFace(Direction face, int colorIndex, TextureAtlasSprite sprite) {
		this.colorIndex = colorIndex;
		this.face = face;
		this.spite = sprite;
	}

}
