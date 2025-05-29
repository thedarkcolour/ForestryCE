package forestry.arboriculture.items;

import forestry.api.arboriculture.IWoodType;
import forestry.arboriculture.WoodHelper;
import forestry.arboriculture.blocks.BlockForestrySlab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nullable;

public class ItemBlockWoodSlab extends BlockItem {
	public ItemBlockWoodSlab(BlockForestrySlab block) {
		super(block, new Item.Properties());
	}

	@Override
	public Component getName(ItemStack itemstack) {
		BlockForestrySlab wood = (BlockForestrySlab) getBlock();
		IWoodType woodType = wood.getWoodType();
		return WoodHelper.getDisplayName(wood, woodType);
	}

	@Override
	public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
		return ((BlockForestrySlab) getBlock()).isFireproof() ? 0 : 150;
	}
}
