package forestry.arboriculture.items;

import forestry.api.arboriculture.genetics.ITree;
import forestry.api.genetics.ISpecies;
import forestry.arboriculture.blocks.BlockAbstractLeaves;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.core.items.ItemBlockForestry;
import forestry.core.items.definitions.IColoredItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.FoliageColor;

public class ItemBlockLeaves extends ItemBlockForestry<BlockAbstractLeaves> implements IColoredItem {
	public ItemBlockLeaves(BlockAbstractLeaves block) {
		super(block);
	}

	@Override
	public Component getName(ItemStack itemstack) {
		if (!itemstack.hasTag()) {
			return Component.translatable("trees.grammar.leaves.type");
		}

		TileLeaves tileLeaves = new TileLeaves(BlockPos.ZERO, getBlock().defaultBlockState());
		tileLeaves.load(itemstack.getTag());

		ITree tree = tileLeaves.getTree();
		if (tree == null) {
			return Component.translatable("for.leaves.corrupted");
		}
		return getDisplayName(tree.getSpecies());
	}

	public static Component getDisplayName(ISpecies<?> species) {
		Component leaves = Component.translatable("for.trees.grammar.leaves.type");
		return Component.translatable("for.trees.grammar.leaves", species.getDisplayName(), leaves);
	}

	@Override
	public int getColorFromItemStack(ItemStack itemStack, int renderPass) {
		if (itemStack.getTag() == null) {
			return FoliageColor.getDefaultColor();
		}

		TileLeaves tileLeaves = new TileLeaves(BlockPos.ZERO, getBlock().defaultBlockState());
		tileLeaves.load(itemStack.getTag());

		if (renderPass == BlockAbstractLeaves.FRUIT_COLOR_INDEX) {
			return tileLeaves.getFruitColour();
		} else {
			Player player = Minecraft.getInstance().player;
			return tileLeaves.getFoliageColour();
		}
	}
}
