package forestry.arboriculture.models;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.datafixers.util.Pair;

import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.arboriculture.ITreeClientManager;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.arboriculture.tiles.TileSapling;
import forestry.core.utils.SpeciesUtil;

public class ModelSapling implements IUnbakedGeometry<ModelSapling> {
	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
		IdentityHashMap<ITreeSpecies, BakedModel> itemModels = new IdentityHashMap<>();
		IdentityHashMap<ITreeSpecies, BakedModel> blockModels = new IdentityHashMap<>();

		ITreeClientManager treeManager = IForestryClientApi.INSTANCE.getTreeManager();
		for (ITreeSpecies species : SpeciesUtil.getAllTreeSpecies()) {
			Pair<ResourceLocation, ResourceLocation> pair = treeManager.getSaplingModels(species);
			ResourceLocation blockModelLocation = pair.getFirst();
			ResourceLocation itemModelLocation = pair.getSecond();

			BakedModel blockModel = baker.bake(blockModelLocation, BlockModelRotation.X0_Y0, spriteGetter);
			if (blockModel != null) {
				blockModels.put(species, blockModel);
			}
			BakedModel itemModel = baker.bake(itemModelLocation, BlockModelRotation.X0_Y0, spriteGetter);
			if (itemModel != null) {
				itemModels.put(species, itemModel);
			}
		}

		return new Baked(itemModels, blockModels);
	}

	public static class Baked implements BakedModel {
		private final IdentityHashMap<ITreeSpecies, BakedModel> itemModels;
		private final IdentityHashMap<ITreeSpecies, BakedModel> blockModels;
		private final BakedModel defaultBlock;
		private final BakedModel defaultItem;
		@Nullable
		private ItemOverrides overrideList;

		public Baked(IdentityHashMap<ITreeSpecies, BakedModel> itemModels, IdentityHashMap<ITreeSpecies, BakedModel> blockModels) {
			this.itemModels = itemModels;
			this.blockModels = blockModels;
			ITreeSpeciesType speciesType = SpeciesUtil.TREE_TYPE.get();
			ITreeSpecies oakSpecies = speciesType.getSpecies(ForestryTreeSpecies.OAK);
			this.defaultBlock = Objects.requireNonNull(blockModels.get(oakSpecies));
			this.defaultItem = Objects.requireNonNull(itemModels.get(oakSpecies));
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
			ITreeSpecies species = extraData.get(TileSapling.TREE_SPECIES);
			if (species == null) {
				species = SpeciesUtil.getTreeSpecies(ForestryTreeSpecies.OAK);
			}
			return blockModels.get(species).getQuads(state, side, rand);
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
			return getQuads(state, side, rand, ModelData.EMPTY, null);
		}

		@Override
		public boolean useAmbientOcclusion() {
			return defaultBlock.useAmbientOcclusion();
		}

		@Override
		public boolean isGui3d() {
			return defaultItem.isGui3d();
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
			return defaultBlock.getParticleIcon();
		}

		@Override
		public TextureAtlasSprite getParticleIcon(ModelData data) {
			ITreeSpecies species = data.get(TileSapling.TREE_SPECIES);

			return blockModels.getOrDefault(species, defaultBlock).getParticleIcon();
		}

		@Override
		public ItemOverrides getOverrides() {
			if (overrideList == null) {
				overrideList = new OverrideList();
			}
			return overrideList;
		}

		public class OverrideList extends ItemOverrides {
			@Nullable
			@Override
			public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int p_173469_) {
				IIndividual individual = IIndividualHandlerItem.getIndividual(stack);
				if (individual == null) {
					return model;
				} else {
					return itemModels.getOrDefault(individual.getSpecies(), model);
				}
			}
		}
	}
}
