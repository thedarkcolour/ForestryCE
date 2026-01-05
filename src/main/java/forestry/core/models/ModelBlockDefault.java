package forestry.core.models;

import com.google.common.base.Preconditions;
import forestry.core.models.baker.ModelBaker;
import forestry.core.models.baker.ModelBakerModel;
import forestry.core.utils.ResourceUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class ModelBlockDefault<B extends Block, K> implements BakedModel {
	@Nullable
	private ItemOverrides overrideList;

	protected final Class<B> blockClass;

	@Nullable
	protected ModelBakerModel blockModel;
	@Nullable
	protected ModelBakerModel itemModel;

	protected ModelBlockDefault(Class<B> blockClass) {
		this.blockClass = blockClass;
	}

	protected BakedModel bakeModel(BlockState state, K key, B block, ModelData extraData) {
		ModelBaker baker = new ModelBaker();

		bakeBlock(block, extraData, key, baker, false);

        this.blockModel = baker.bake(false);
		onCreateModel(this.blockModel);
		return this.blockModel;
	}

	protected BakedModel getModel(BlockState state, ModelData extraData) {
		Preconditions.checkArgument(this.blockClass.isInstance(state.getBlock()));

		K worldKey = getWorldKey(state, extraData);
		B block = this.blockClass.cast(state.getBlock());
		return bakeModel(state, worldKey, block, extraData);
	}

	protected BakedModel bakeModel(ItemStack stack, Level world, K key) {
		ModelBaker baker = new ModelBaker();
		Block block = Block.byItem(stack.getItem());
		Preconditions.checkArgument(this.blockClass.isInstance(block));
		B bBlock = this.blockClass.cast(block);
		bakeBlock(bBlock, ModelData.EMPTY, key, baker, true);

		return this.itemModel = baker.bake(true);
	}

	protected BakedModel getModel(ItemStack stack, Level world) {
		return bakeModel(stack, world, getInventoryKey(stack));
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, @Nullable RenderType renderType) {
		Preconditions.checkNotNull(state);
		BakedModel model = getModel(state, extraData);
		return model.getQuads(state, side, rand, extraData, renderType);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
		return getQuads(state, side, rand, ModelData.EMPTY, null);
	}

	protected void onCreateModel(ModelBakerModel model) {
		model.setAmbientOcclusion(true);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return (this.itemModel != null || this.blockModel != null) &&
			(this.blockModel != null ? this.blockModel.useAmbientOcclusion() : this.itemModel.useAmbientOcclusion());
	}

	@Override
	public boolean isGui3d() {
		return this.itemModel != null && this.itemModel.isGui3d();
	}

	@Override
	public boolean isCustomRenderer() {
		return (this.itemModel != null || this.blockModel != null) &&
			(this.blockModel != null ? this.blockModel.isCustomRenderer() : this.itemModel.isCustomRenderer());
	}

	@Override
	public boolean usesBlockLight() {
		return this.itemModel != null && this.itemModel.usesBlockLight();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		if (this.blockModel != null) {
			return this.blockModel.getParticleIcon();
		}
		return ResourceUtil.getMissingTexture();
	}

	@Override
	public ItemTransforms getTransforms() {
		if (this.itemModel == null) {
			return ItemTransforms.NO_TRANSFORMS;
		}
		return this.itemModel.getTransforms();
	}

	protected ItemOverrides createOverrides() {
		return new DefaultItemOverrideList();
	}

	@Override
	public ItemOverrides getOverrides() {
		if (this.overrideList == null) {
            this.overrideList = createOverrides();
		}
		return this.overrideList;
	}

	protected abstract K getInventoryKey(ItemStack stack);

	protected abstract K getWorldKey(BlockState state, ModelData extraData);

	protected abstract void bakeBlock(B block, ModelData extraData, K key, ModelBaker baker, boolean inventory);

	private class DefaultItemOverrideList extends ItemOverrides {
		public DefaultItemOverrideList() {
			super();
		}

		@Nullable
		@Override
		public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int p_173469_) {
			if (world == null) {
				world = Minecraft.getInstance().level;
			}
			return getModel(stack, world);
		}
	}
}
