package forestry.arboriculture.compat;

import forestry.api.ForestryConstants;
import forestry.api.arboriculture.ForestryFruits;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.arboriculture.blocks.BlockDefaultLeavesFruit;
import forestry.arboriculture.tiles.TileLeaves;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import javax.annotation.Nullable;

public enum LeafFruitProvider implements IBlockComponentProvider {
	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		ResourceLocation fruitId = getFruitId(accessor);

		if (fruitId == null || fruitId.equals(ForestryFruits.NONE)) {
			return;
		}

		Component fruitName = Component.translatable(
			"allele." + fruitId.getNamespace() + ".fruits." + fruitId.getPath()
		);

		tooltip.add(Component.translatable(
			"jade.forestry.fruit",
			fruitName
		));
	}

	@Nullable
	private static ResourceLocation getFruitId(BlockAccessor accessor) {
		if (accessor.getBlockEntity() instanceof TileLeaves leaves) {
			if (!leaves.hasFruit()) {
				return null;
			}

			ITree tree = leaves.getTree();
			if (tree != null) {
				return tree.getGenome().getActiveValue(TreeChromosomes.FRUIT);
			}
		}

		if (accessor.getBlock() instanceof BlockDefaultLeavesFruit leaves) {
			return leaves.getType()
				.getIndividual()
				.getGenome()
				.getActiveValue(TreeChromosomes.FRUIT);
		}

		return null;
	}

	@Override
	public ResourceLocation getUid() {
		return ForestryConstants.forestry("leaf_fruit");
	}
}