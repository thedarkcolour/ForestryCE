package forestry.arboriculture.leaves;

import forestry.api.arboriculture.genetics.ITree;
import forestry.api.client.IForestryClientApi;
import forestry.core.platform.item.ForestryBlockItem;
import forestry.core.platform.item.ITintedItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class DefaultLeavesBlockItem extends ForestryBlockItem<AbstractLeavesBlock> implements ITintedItem {
	public DefaultLeavesBlockItem(AbstractLeavesBlock block, Item.Properties properties) {
		super(block, properties);
	}

	private ForestryLeafType leafType() {
		return ((ILeafTypeBlock) getBlock()).getType();
	}

	@Override
	public Component getName(ItemStack itemStack) {
		return LeavesBlockItem.getDisplayName(leafType().getIndividual().getSpecies());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getColorFromItemStack(ItemStack itemStack, int renderPass) {
		ForestryLeafType type = leafType();
		if (renderPass == AbstractLeavesBlock.FRUIT_COLOR_INDEX) {
			return type.getFruit().getDecorativeColor();
		}
		ITree individual = type.getIndividual();
		return IForestryClientApi.INSTANCE.getTreeManager().getTint(individual.getSpecies()).get(null, null);
	}
}
