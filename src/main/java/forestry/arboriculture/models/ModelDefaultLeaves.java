package forestry.arboriculture.models;

import com.google.common.base.Preconditions;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.arboriculture.ILeafSprite;
import forestry.arboriculture.blocks.BlockAbstractLeaves;
import forestry.arboriculture.blocks.BlockDefaultLeaves;
import forestry.core.models.ModelBlockCached;
import forestry.core.models.baker.ModelBaker;
import forestry.core.utils.ResourceUtil;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class ModelDefaultLeaves extends ModelBlockCached<BlockDefaultLeaves, ModelDefaultLeaves.Key> {
	public ModelDefaultLeaves() {
		super(BlockDefaultLeaves.class);
	}

	public static final class Key {
		public final ResourceLocation speciesId;
		public final boolean fancy;
		private final int hashCode;

		public Key(ResourceLocation speciesId, boolean fancy) {
			this.speciesId = speciesId;
			this.fancy = fancy;
			this.hashCode = Objects.hash(speciesId, fancy);
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ModelDefaultLeaves.Key otherKey)) {
				return false;
			} else {
				// species IDs are passed around so == is fine
				return otherKey.speciesId == this.speciesId && otherKey.fancy == this.fancy;
			}
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}
	}

	@Override
	protected ModelDefaultLeaves.Key getInventoryKey(ItemStack stack) {
		Block block = Block.byItem(stack.getItem());
		Preconditions.checkArgument(block instanceof BlockDefaultLeaves, "ItemStack must be for default leaves.");
		BlockDefaultLeaves bBlock = (BlockDefaultLeaves) block;
		return new Key(bBlock.getSpeciesId(), Minecraft.useFancyGraphics());
	}

	@Override
	protected ModelDefaultLeaves.Key getWorldKey(BlockState state, ModelData extraData) {
		if (state.getBlock() instanceof BlockDefaultLeaves block) {
			ResourceLocation treeDefinition = block.getSpeciesId();
			return new ModelDefaultLeaves.Key(treeDefinition, Minecraft.useFancyGraphics());
		} else {
			throw new IllegalArgumentException("state must be for default leaves.");
		}
	}

	@Override
	protected void bakeBlock(BlockDefaultLeaves block, ModelData extraData, Key key, ModelBaker baker, boolean inventory) {
		ResourceLocation speciesId = key.speciesId;

		ITreeSpecies species = SpeciesUtil.getTreeSpecies(speciesId);
		ILeafSprite leafSpriteProvider = IForestryClientApi.INSTANCE.getTreeManager().getLeafSprite(species);

		ResourceLocation leafSpriteLocation = leafSpriteProvider.get(false, key.fancy);
		TextureAtlasSprite leafSprite = ResourceUtil.getBlockSprite(leafSpriteLocation);

		// Render the plain leaf block.
		baker.addBlockModel(leafSprite, BlockAbstractLeaves.FOLIAGE_COLOR_INDEX);

		// Set the particle sprite
		baker.setParticleSprite(leafSprite);
	}


	@Override
	protected BakedModel bakeModel(BlockState state, Key key, BlockDefaultLeaves block, ModelData extraData) {
		ModelBaker baker = new ModelBaker();

		bakeBlock(block, extraData, key, baker, false);

        this.blockModel = baker.bake(false);
		onCreateModel(this.blockModel);
		return this.blockModel;
	}

	@Override
	public ItemTransforms getTransforms() {
		return ModelLeaves.TRANSFORMS;
	}
}
