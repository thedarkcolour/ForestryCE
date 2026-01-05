package forestry.core.models.baker;

import forestry.api.ForestryConstants;
import forestry.core.models.ClientManager;
import forestry.core.utils.ResourceUtil;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;


/**
 * A model baker to make custom block models with more than one texture layer.
 */
//Todo: Test if baker can be replaced with model loaders
@OnlyIn(Dist.CLIENT)
public final class ModelBaker {

	private static final ResourceLocation FACE_LOCATION = ForestryConstants.forestry("baker_face");
	private static final float[] UVS = new float[]{0.0F, 0.0F, 16.0F, 16.0F, 0.0F, 0.0F, 16.0F, 16.0F};
	private static final FaceBakery FACE_BAKERY = new FaceBakery();
	private static final Vector3f POS_FROM = new Vector3f(0.0F, 0.0F, 0.0F);
	private static final Vector3f POS_TO = new Vector3f(16.0F, 16.0F, 16.0F);

	private final List<ModelBakerFace> faces = new ArrayList<>();

	private final ModelBakerModel currentModel = new ModelBakerModel(ClientManager.INSTANCE.getDefaultBlockState());

	private int colorIndex = -1;

	public ModelBaker addBlockModel(TextureAtlasSprite[] textures, int colorIndex) {
		this.colorIndex = colorIndex;

		for (Direction facing : Direction.VALUES) {
			addFace(facing, textures[facing.ordinal()]);
		}
		return this;
	}

	public ModelBaker addBlockModel(TextureAtlasSprite texture, int colorIndex) {
		return addBlockModel(new TextureAtlasSprite[]{texture, texture, texture, texture, texture, texture}, colorIndex);
	}

	public ModelBaker addFace(Direction facing, TextureAtlasSprite sprite) {
		if (sprite != ResourceUtil.getMissingTexture()) {
            this.faces.add(new ModelBakerFace(facing, this.colorIndex, sprite));
		}
		return this;
	}

	public ModelBakerModel bake(boolean flip) {
		BlockModelRotation modelRotation = BlockModelRotation.X0_Y0;

		if (flip) {
			modelRotation = BlockModelRotation.X0_Y180;
		}

		for (ModelBakerFace face : this.faces) {
			Direction facing = face.face;
			BlockFaceUV uvFace = new BlockFaceUV(UVS, 0);
			BlockElementFace partFace = new BlockElementFace(facing, face.colorIndex, "", uvFace);
			BakedQuad quad = FACE_BAKERY.bakeQuad(POS_FROM, POS_TO, partFace, face.spite, facing, modelRotation, null, true, FACE_LOCATION);

            this.currentModel.addQuad(facing, quad);
		}

		return this.currentModel;
	}

	public void setParticleSprite(TextureAtlasSprite particleSprite) {
        this.currentModel.setParticleSprite(particleSprite);
	}
}
