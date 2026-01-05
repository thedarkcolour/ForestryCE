package forestry.farming.logic.farmables;

import forestry.core.utils.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;

public class FarmableCocoa extends FarmableAgingCrop {
	public FarmableCocoa() {
		super(Items.COCOA_BEANS, Blocks.COCOA, new ItemStack(Items.COCOA_BEANS), CocoaBlock.AGE, 2, 0);
	}

	@Override
	public boolean plantSaplingAt(Player player, ItemStack germling, Level level, BlockPos pos) {
		return BlockUtil.tryPlantCocoaPod(level, pos);
	}
}
