package forestry.storage.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.datafixers.util.Either;

import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import forestry.api.ForestryConstants;

import org.joml.Vector3f;

@SuppressWarnings("deprecation")
public class FilledCrateModel implements IUnbakedGeometry<FilledCrateModel> {
	@Nullable
	public static BakedModel cachedBaseModel = null;
	@Nullable
	public static ItemTransforms cachedTransforms = null;
	@Nullable
	public static List<BakedQuad> cachedQuads;

	private final BlockModel contents;

	public FilledCrateModel(BlockModel contents) {
		this.contents = contents;
	}

	@SuppressWarnings("DataFlowIssue")
	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
		if (cachedBaseModel == null) {
			cachedBaseModel = bakery.getModel(Loader.FILLED_CRATE_LOCATION).bake(bakery, spriteGetter, modelState, modelLocation);
			cachedTransforms = cachedBaseModel.getTransforms();
			cachedQuads = cachedBaseModel.getQuads(null, null, RandomSource.create());
		}

		return new Baked(contents.bake(bakery, spriteGetter, modelState, modelLocation), cachedQuads, cachedTransforms);
	}

	public static class Loader implements IGeometryLoader<FilledCrateModel> {
		private static final ResourceLocation FILLED_CRATE_LOCATION = ForestryConstants.forestry("item/filled_crate");

		@Override
		public FilledCrateModel read(JsonObject json, JsonDeserializationContext ctx) {
			List<BlockElement> elements = new ArrayList<>();
			Map<String, Either<Material, String>> textureMap = new HashMap<>();
			ArrayList<Material> materials = new ArrayList<>();

			// vanilla models support layer0 to layer4; layer0 is implied to be the crate
			for (int layer = 1; layer <= 4; ++layer) {
				if (json.has("textures") && json.get("textures").getAsJsonObject().has("layer" + layer)) {
					elements.add(make2dElement(layer, 3f, 4f, 11f, 12f, 0.002f));
					// add material
					ResourceLocation contentsTexture = ResourceLocation.tryParse(GsonHelper.getAsString(GsonHelper.getAsJsonObject(json, "textures"), "layer" + layer));
					//noinspection DataFlowIssue
					Material contentsMaterial = new Material(TextureAtlas.LOCATION_BLOCKS, contentsTexture);
					textureMap.put("layer" + layer, Either.left(contentsMaterial));
					materials.add(contentsMaterial);
				}
			}
			BlockModel contents = new BlockModel(FILLED_CRATE_LOCATION, elements, textureMap, false, BlockModel.GuiLight.FRONT, ItemTransforms.NO_TRANSFORMS, List.of());
			materials.trimToSize();

			return new FilledCrateModel(contents);
		}
	}

	public static BlockElement make2dElement(int layer, float startX, float startY, float endX, float endY, float zOffset) {
		Map<Direction, BlockElementFace> faces = Map.of(
				Direction.SOUTH, new BlockElementFace(null, layer, "layer" + layer, new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)),
				Direction.NORTH, new BlockElementFace(null, layer, "layer" + layer, new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0))
		);
		// front and back 2d faces
		// add the element, scaled down
		return new BlockElement(new Vector3f(startX, startY, 7.5f - zOffset), new Vector3f(endX, endY, 8.5f + zOffset), faces, null, false);
	}

	private static class Baked implements BakedModel {
		private final ItemTransforms transforms;
		private final TextureAtlasSprite particle;
		private final List<BakedQuad> quads;

		private Baked(BakedModel bakedContents, List<BakedQuad> baseQuads, ItemTransforms transforms) {
			RandomSource random = RandomSource.create();
			List<BakedQuad> contentsQuads = bakedContents.getQuads(null, null, random);
			this.quads = new ArrayList<>(baseQuads.size() + contentsQuads.size());
			this.quads.addAll(baseQuads);
			this.quads.addAll(contentsQuads);

			this.transforms = transforms;
			this.particle = bakedContents.getParticleIcon();
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction cullFace, RandomSource rand) {
			return quads;
		}

		@Override
		public boolean useAmbientOcclusion() {
			return false;
		}

		@Override
		public boolean isGui3d() {
			return false;
		}

		@Override
		public boolean usesBlockLight() {
			return false;
		}

		@Override
		public boolean isCustomRenderer() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleIcon() {
			return this.particle;
		}

		@Override
		public ItemOverrides getOverrides() {
			return ItemOverrides.EMPTY;
		}

		@Override
		public ItemTransforms getTransforms() {
			return this.transforms;
		}
	}
}