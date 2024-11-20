package forestry.apiculture.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.apiculture.IBeeClientManager;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.core.utils.SpeciesUtil;

// The item model for a bee, which supports custom bee textures
// Only supports the four life stages used in base Forestry (DRONE, LARVAE, PRINCESS, QUEEN)
// If you have a custom life stage, you'll need to implement your own model code
public class ModelBee implements IUnbakedGeometry<ModelBee> {
	private final ILifeStage stage;

	public ModelBee(ILifeStage stage) {
		this.stage = stage;
	}

	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
		IBeeClientManager manager = IForestryClientApi.INSTANCE.getBeeManager();
		Map<IBeeSpecies, ResourceLocation> models = manager.getBeeModels(this.stage);
		IdentityHashMap<IBeeSpecies, BakedModel> itemModels = new IdentityHashMap<>();

		for (IBeeSpecies species : SpeciesUtil.getAllBeeSpecies()) {
			ResourceLocation location = models.get(species);
			BakedModel model = baker.bake(location, BlockModelRotation.X0_Y0, spriteGetter);

			if (model != null) {
				itemModels.put(species, model);
			}
		}

		return new ModelBee.Baked(itemModels);
	}

	public static class Loader implements IGeometryLoader<ModelBee> {
		private final ModelBee[] models = new ModelBee[BeeLifeStage.values().length];

		@Override
		public ModelBee read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
			String stageName = GsonHelper.getAsString(jsonObject, "stage");
			BeeLifeStage stage = BeeLifeStage.valueOf(stageName.toUpperCase(Locale.ENGLISH));
			int ordinal = stage.ordinal();

			if (this.models[ordinal] == null) {
				this.models[ordinal] = new ModelBee(stage);
			}

			return this.models[ordinal];
		}
	}

	private static class Baked implements BakedModel {
		private final IdentityHashMap<IBeeSpecies, BakedModel> itemModels;

		public Baked(IdentityHashMap<IBeeSpecies, BakedModel> itemModels) {
			this.itemModels = itemModels;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pDirection, RandomSource pRandom) {
			return List.of();
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
			return null;
		}

		@Override
		public ItemOverrides getOverrides() {
			return new OverrideList();
		}

		public class OverrideList extends ItemOverrides {
			@Override
			public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int p_173469_) {
				IIndividual individual = IIndividualHandlerItem.getIndividual(stack);
				if (individual == null) {
					return model;
				} else {
					return Baked.this.itemModels.getOrDefault(individual.getSpecies(), model);
				}
			}
		}
	}
}
