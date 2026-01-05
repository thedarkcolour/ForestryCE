package forestry.apiculture.inventory;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.hives.IHiveFrame;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.apiculture.InventoryBeeHousing;
import forestry.core.utils.SlotUtil;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

public class InventoryApiary extends InventoryBeeHousing implements IApiaryInventory {
	public static final int SLOT_FRAMES_1 = 9;
	public static final int SLOT_FRAMES_COUNT = 3;

	public InventoryApiary() {
		super(12);
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (SlotUtil.isSlotInRange(slotIndex, SLOT_FRAMES_1, SLOT_FRAMES_COUNT)) {
			return stack.getItem() instanceof IHiveFrame && getItem(slotIndex).isEmpty();
		}

		return super.canSlotAccept(slotIndex, stack);
	}

	// override for pipe automation
	@Override
	public boolean canPlaceItem(int slotIndex, ItemStack itemStack) {
		return !SlotUtil.isSlotInRange(slotIndex, SLOT_FRAMES_1, SLOT_FRAMES_COUNT) &&
			super.canPlaceItem(slotIndex, itemStack);
	}

	public Collection<Tuple<IHiveFrame, ItemStack>> getFrames() {
		Collection<Tuple<IHiveFrame, ItemStack>> hiveFrames = new ArrayList<>(SLOT_FRAMES_COUNT);

		for (int i = SLOT_FRAMES_1; i < SLOT_FRAMES_1 + SLOT_FRAMES_COUNT; i++) {
			ItemStack stackInSlot = getItem(i);
			Item itemInSlot = stackInSlot.getItem();
			if (itemInSlot instanceof IHiveFrame frame) {
				hiveFrames.add(new Tuple<>(frame, stackInSlot.copy()));
			}
		}

		return hiveFrames;
	}

	@Override
	public void wearOutFrames(IBeeHousing beeHousing, int amount) {
		//IBeekeepingMode beekeepingMode = SpeciesUtil.BEE_TYPE.get().getBeekeepingMode(beeHousing.getWorldObj());
		int wear = amount; /* Math.round(amount * beekeepingMode.getWearModifier())*/

		for (int i = SLOT_FRAMES_1; i < SLOT_FRAMES_1 + SLOT_FRAMES_COUNT; i++) {
			ItemStack hiveFrameStack = getItem(i);
			Item hiveFrameItem = hiveFrameStack.getItem();

			if (hiveFrameItem instanceof IHiveFrame hiveFrame) {
				IIndividual queen = IIndividualHandlerItem.getIndividual(getQueen());

				if (queen != null) {
					ItemStack usedFrame = hiveFrame.frameUsed(beeHousing, hiveFrameStack, (IBee) queen, wear);
					setItem(i, usedFrame);
				}
			}
		}
	}
}
