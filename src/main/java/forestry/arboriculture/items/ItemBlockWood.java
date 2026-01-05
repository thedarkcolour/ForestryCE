package forestry.arboriculture.items;

import forestry.api.arboriculture.IWoodType;
import forestry.arboriculture.IWoodTyped;
import forestry.arboriculture.WoodHelper;
import forestry.core.items.ItemBlockForestry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

public class ItemBlockWood<B extends Block & IWoodTyped> extends ItemBlockForestry<B> {
	private final IWoodTyped wood;
	private final IWoodType woodType;

	public ItemBlockWood(B block) {
		super(block, new Item.Properties());

		// Safeguard against Diagonal Fence's registry replacements causing crashes
		this.wood = block;
		this.woodType = block.getWoodType();
	}

	@Override
	public Component getName(ItemStack itemstack) {
		// todo use vanilla names and data generation instead of this
		return WoodHelper.getDisplayName(this.wood, this.woodType);
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
