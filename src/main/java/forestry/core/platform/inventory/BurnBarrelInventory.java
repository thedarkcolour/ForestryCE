package forestry.core.platform.inventory;

import forestry.api.ForestryTags;
import forestry.core.content.burnbarrel.BurnBarrelBlock;
import forestry.core.content.burnbarrel.BurnBarrelBlockEntity;
import forestry.core.features.CoreItems;
import forestry.core.platform.util.SlotUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BurnBarrelInventory extends BlockEntityInventoryAdapter<BurnBarrelBlockEntity> {
	public static final int SLOT_INPUT_1 = 0;
	public static final int SLOT_INPUT_COUNT = 4;
	public static final int SLOT_OUTPUT_1 = SLOT_INPUT_1 + SLOT_INPUT_COUNT;
	public static final int SLOT_OUTPUT_COUNT = 3;

	public BurnBarrelInventory(BurnBarrelBlockEntity tile) {
		super(tile, SLOT_INPUT_COUNT + SLOT_OUTPUT_COUNT, "Items");
	}

	// Deviation from 1.20.1: burn times came from ForgeHooks.getBurnTime, which NeoForge moved onto the stack
	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (SlotUtil.isSlotInRange(slotIndex, SLOT_INPUT_1, SLOT_INPUT_COUNT)) {
			return !stack.is(ForestryTags.Items.BURN_BARREL_BLACKLIST) && stack.getBurnTime(null) > 0;
		}
		return false;
	}

	// Thought about restricting to only the bottom face, but like, go for gold I guess
	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack stack, Direction side) {
		return SlotUtil.isSlotInRange(slotIndex, SLOT_OUTPUT_1, SLOT_OUTPUT_COUNT);
	}

	// Deviation from 1.20.1: that tree counted the ash before writing the slot, so taking the last ash out left
	// HAS_ASH set until the next write, and it dereferenced the level unguarded, which threw while a chunk loaded
	// a barrel holding ash, because a block entity has no level yet when its NBT is read
	@Override
	public void setItem(int slotId, ItemStack itemstack) {
		super.setItem(slotId, itemstack);

		if (!SlotUtil.isSlotInRange(slotId, SLOT_OUTPUT_1, SLOT_OUTPUT_COUNT)) {
			return;
		}

		Level level = this.tile.getLevel();
		if (level == null || countItem(CoreItems.ASH.item()) > 0) {
			return;
		}

		BlockPos pos = this.tile.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if (state.hasProperty(BurnBarrelBlock.HAS_ASH) && state.getValue(BurnBarrelBlock.HAS_ASH)) {
			level.setBlock(pos, state.setValue(BurnBarrelBlock.HAS_ASH, false), 3);
		}
	}
}
