package forestry.arboriculture.wood;

import forestry.core.platform.item.ForestryBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

public class ForestryWoodBlockItem<B extends Block & IWoodTyped> extends ForestryBlockItem<B> {
	private final IWoodTyped wood;

	public ForestryWoodBlockItem(B block) {
		super(block, new Item.Properties());

		// Safeguard against Diagonal Fence's registry replacements causing crashes
		this.wood = block;
	}

	@Override
	public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
		if (this.wood.isFireproof()) {
			return 0;
		} else {
			return 300;
		}
	}
}
