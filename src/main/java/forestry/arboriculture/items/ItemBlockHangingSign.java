package forestry.arboriculture.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.ItemStack;

import forestry.api.arboriculture.WoodBlockKind;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.WoodHelper;
import forestry.arboriculture.blocks.BlockForestryHangingSign;
import forestry.arboriculture.features.ArboricultureBlocks;

public class ItemBlockHangingSign extends HangingSignItem {
	private final ForestryWoodType type;

	public ItemBlockHangingSign(BlockForestryHangingSign block) {
		super(block, ArboricultureBlocks.WALL_HANGING_SIGN.get(block.getWoodType()).block(), new Properties());

		this.type = block.getWoodType();
	}

	@Override
	public Component getName(ItemStack itemstack) {
		// todo use vanilla names and data generation instead of this
		return WoodHelper.getDisplayName(WoodBlockKind.HANGING_SIGN, false, this.type);
	}
}
