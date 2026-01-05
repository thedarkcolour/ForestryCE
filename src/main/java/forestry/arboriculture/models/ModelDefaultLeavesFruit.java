package forestry.arboriculture.models;

import forestry.arboriculture.blocks.BlockDefaultLeavesFruit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

public class ModelDefaultLeavesFruit extends ModelDecorativeLeaves<BlockDefaultLeavesFruit> {
	public ModelDefaultLeavesFruit() {
		super(BlockDefaultLeavesFruit.class);
	}

	@Override
	protected ModelDefaultLeaves.Key getInventoryKey(ItemStack stack) {
		Block block = Block.byItem(stack.getItem());
		return new ModelDefaultLeaves.Key(((BlockDefaultLeavesFruit) block).getSpeciesId(), Minecraft.useFancyGraphics());
	}

	@Override
	protected ModelDefaultLeaves.Key getWorldKey(BlockState state, ModelData extraData) {
		Block block = state.getBlock();
		return new ModelDefaultLeaves.Key(((BlockDefaultLeavesFruit) block).getSpeciesId(), Minecraft.useFancyGraphics());
	}

	@Override
	public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
		return ChunkRenderTypeSet.of(RenderType.cutoutMipped());
	}
}
