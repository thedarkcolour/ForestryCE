package forestry.arboriculture.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignItem;

import forestry.api.arboriculture.WoodBlockKind;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.WoodHelper;
import forestry.arboriculture.blocks.BlockForestryStandingSign;
import forestry.arboriculture.features.ArboricultureBlocks;

public class ItemBlockSign extends SignItem {
	private final ForestryWoodType type;

	public ItemBlockSign(BlockForestryStandingSign block) {
		super(new Properties(), block, ArboricultureBlocks.WALL_SIGN.get(block.getWoodType()).block());

		this.type = block.getWoodType();
	}

	@Override
	public Component getName(ItemStack itemstack) {
		// todo use vanilla names and data generation instead of this
		return WoodHelper.getDisplayName(WoodBlockKind.SIGN, false, this.type);
	}
}
