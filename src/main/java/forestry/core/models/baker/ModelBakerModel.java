package forestry.core.models.baker;

import forestry.arboriculture.models.ModelLeaves;
import forestry.core.utils.ResourceUtil;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ModelBakerModel implements BakedModel {

	private final boolean isGui3d;
	private boolean isAmbientOcclusion;
	private TextureAtlasSprite particleSprite;
	@Nullable
	private ModelState modelState;

	private final Map<Direction, List<BakedQuad>> faceQuads;
	private final List<BakedQuad> generalQuads;
	private final List<Pair<BlockState, BakedModel>> models;
	private final List<Pair<BlockState, BakedModel>> modelsPost;

	private float[] rotation = getDefaultRotation();
	private float[] translation = getDefaultTranslation();
	private float[] scale = getDefaultScale();

	ModelBakerModel(ModelState modelState) {
        this.models = new ArrayList<>();
        this.modelsPost = new ArrayList<>();
        this.faceQuads = new EnumMap<>(Direction.class);
        this.generalQuads = new ArrayList<>();
        this.particleSprite = ResourceUtil.getMissingTexture();
        this.isGui3d = true;
        this.isAmbientOcclusion = false;
		setModelState(modelState);

		for (Direction face : Direction.VALUES) {
            this.faceQuads.put(face, new ArrayList<>());
		}
	}

	private ModelBakerModel(ModelBakerModel old) {
		this.models = new ArrayList<>(old.models);
		this.modelsPost = new ArrayList<>(old.modelsPost);
		this.faceQuads = new EnumMap<>(old.faceQuads);
		this.generalQuads = new ArrayList<>(old.generalQuads);
		this.isGui3d = old.isGui3d;
		this.isAmbientOcclusion = old.isAmbientOcclusion;
		this.rotation = Arrays.copyOf(old.rotation, 3);
		this.translation = Arrays.copyOf(old.translation, 3);
		this.scale = Arrays.copyOf(old.scale, 3);
		this.particleSprite = old.particleSprite;
		setModelState(old.modelState);
	}

	@Override
	public boolean isGui3d() {
		return this.isGui3d;
	}

	@Override
	public boolean usesBlockLight() {
		return true;
	}

	public void setAmbientOcclusion(boolean ambientOcclusion) {
		this.isAmbientOcclusion = ambientOcclusion;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return this.isAmbientOcclusion;
	}

	public void setParticleSprite(TextureAtlasSprite particleSprite) {
		this.particleSprite = particleSprite;
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return this.particleSprite;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public ItemTransforms getTransforms() {
		return ModelLeaves.TRANSFORMS;
	}

	@Override
	public ItemOverrides getOverrides() {
		return ItemOverrides.EMPTY;
	}

	private static float[] getDefaultRotation() {
		return new float[]{-80, -45, 170};
	}

	private static float[] getDefaultTranslation() {
		return new float[]{0, 1.5F, -2.75F};
	}

	private static float[] getDefaultScale() {
		return new float[]{0.375F, 0.375F, 0.375F};
	}

	public void setRotation(float[] rotation) {
		this.rotation = rotation;
	}

	public void setTranslation(float[] translation) {
		this.translation = translation;
	}

	public void setScale(float[] scale) {
		this.scale = scale;
	}

	public float[] getRotation() {
		return this.rotation;
	}

	public float[] getTranslation() {
		return this.translation;
	}

	public float[] getScale() {
		return this.scale;
	}

	public void setModelState(ModelState modelState) {
		this.modelState = modelState;
	}

	public void addQuad(@Nullable Direction facing, BakedQuad quad) {
		if (facing != null) {
            this.faceQuads.get(facing).add(quad);
		} else {
            this.generalQuads.add(quad);
		}
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
		List<BakedQuad> quads = new ArrayList<>();
		for (Pair<BlockState, BakedModel> model : this.models) {
			List<BakedQuad> modelQuads = model.getRight().getQuads(model.getLeft(), side, rand);
			if (!modelQuads.isEmpty()) {
				quads.addAll(modelQuads);
			}
		}
		if (side != null) {
			quads.addAll(this.faceQuads.get(side));
		}
		quads.addAll(this.generalQuads);
		for (Pair<BlockState, BakedModel> model : this.modelsPost) {
			List<BakedQuad> modelQuads = model.getRight().getQuads(model.getLeft(), side, rand);
			if (!modelQuads.isEmpty()) {
				quads.addAll(modelQuads);
			}
		}
		return quads;
	}

	public ModelBakerModel copy() {
		return new ModelBakerModel(this);
	}
}
