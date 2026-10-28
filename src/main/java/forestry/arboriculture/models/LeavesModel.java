package forestry.arboriculture.models;

import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.arboriculture.ILeafSprite;
import forestry.arboriculture.leaves.AbstractLeavesBlock;
import forestry.arboriculture.leaves.ForestryLeavesBlock;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.leaves.LeavesBlockEntity;
import forestry.core.platform.models.ModelBlockCached;
import forestry.core.platform.models.ModelTransforms;
import forestry.core.platform.models.baker.ModelBaker;
import forestry.core.platform.util.NBTUtilForestry;
import forestry.core.platform.util.ResourceUtil;
import forestry.core.platform.util.SpeciesUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class LeavesModel extends ModelBlockCached<ForestryLeavesBlock, LeavesModel.Key> {

	public LeavesModel() {
		super(ForestryLeavesBlock.class);
	}

	public static class Key {
		public final TextureAtlasSprite leafSprite;
		@Nullable
		public final TextureAtlasSprite fruitSprite;
		public final boolean fancy;
		private final int hashCode;

		public Key(TextureAtlasSprite leafSprite, @Nullable TextureAtlasSprite fruitSprite, boolean fancy) {
			this.leafSprite = leafSprite;
			this.fruitSprite = fruitSprite;
			this.fancy = fancy;
			this.hashCode = Objects.hash(leafSprite, fruitSprite, fancy);
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Key otherKey)) {
				return false;
			} else {
				return otherKey.leafSprite == this.leafSprite && otherKey.fruitSprite == this.fruitSprite && otherKey.fancy == this.fancy;
			}
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}
	}

	@Override
	protected Key getInventoryKey(ItemStack stack) {
		LeavesBlockEntity leaves = new LeavesBlockEntity(BlockPos.ZERO, ArboricultureBlocks.LEAVES.defaultState());
		CompoundTag tag = NBTUtilForestry.getItemStackTag(stack);
		if (tag != null) {
			leaves.loadAdditional(tag, net.minecraft.core.RegistryAccess.EMPTY);
		} else {
			leaves.setTree(SpeciesUtil.TREE_TYPE.get().getDefaultSpecies().createIndividual());
		}
		return getKey(leaves.getModelData());
	}

	@Override
	protected Key getWorldKey(BlockState state, ModelData extraData) {
		return getKey(extraData);
	}

	private Key getKey(ModelData extraData) {
		boolean fancy = Minecraft.useFancyGraphics();

		ITreeSpecies species = extraData.get(LeavesBlockEntity.PROPERTY_SPECIES);
		if (species == null) {
			species = SpeciesUtil.TREE_TYPE.get().getDefaultSpecies();
		}
		ILeafSprite sprite = IForestryClientApi.INSTANCE.getTreeManager().getLeafSprite(species);
		if (sprite == null) {
			species = SpeciesUtil.TREE_TYPE.get().getDefaultSpecies();
			sprite = IForestryClientApi.INSTANCE.getTreeManager().getLeafSprite(species);
		}
		ResourceLocation leafLocation = sprite.get(Boolean.TRUE.equals(extraData.get(LeavesBlockEntity.PROPERTY_POLLINATED)), fancy);
		ResourceLocation fruitLocation = extraData.get(LeavesBlockEntity.PROPERTY_FRUIT_TEXTURE);

		return new Key(ResourceUtil.getBlockSprite(leafLocation), fruitLocation != null ? ResourceUtil.getBlockSprite(fruitLocation) : null, fancy);
	}

	@Override
	protected void bakeBlock(ForestryLeavesBlock block, ModelData extraData, Key key, ModelBaker baker, boolean inventory) {
		// Render the plain leaf block.
		baker.addBlockModel(key.leafSprite, AbstractLeavesBlock.FOLIAGE_COLOR_INDEX);

		if (key.fruitSprite != null) {
			baker.addBlockModel(key.fruitSprite, AbstractLeavesBlock.FRUIT_COLOR_INDEX);
		}

		// Set the particle sprite
		baker.setParticleSprite(key.leafSprite);
	}

	@Override
	public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
		return getKey(data).fruitSprite != null ? ChunkRenderTypeSet.of(RenderType.cutoutMipped()) : super.getRenderTypes(state, rand, data);
	}

	@Override
	public ItemTransforms getTransforms() {
		return ModelTransforms.BLOCK;
	}
}
