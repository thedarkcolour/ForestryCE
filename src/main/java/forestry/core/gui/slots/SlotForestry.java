package forestry.core.gui.slots;

import forestry.api.core.tooltips.IToolTipProvider;
import forestry.api.core.tooltips.ToolTip;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class SlotForestry extends Slot implements IToolTipProvider {

	private boolean isPhantom;
	private boolean canAdjustPhantom = true;
	private boolean canShift = true;
	private int stackLimit;
	@Nullable
	private ToolTip toolTips;

	public SlotForestry(Container inventory, int slotIndex, int xPos, int yPos) {
		super(inventory, slotIndex, xPos, yPos);
		this.stackLimit = -1;
	}

	public SlotForestry setPhantom() {
        this.isPhantom = true;
		return this;
	}

	public SlotForestry blockShift() {
        this.canShift = false;
		return this;
	}

	@Override
	public void set(ItemStack itemStack) {
		if (!isPhantom() || canAdjustPhantom()) {
			super.set(itemStack);
		}
	}

	public SlotForestry setCanAdjustPhantom(boolean canAdjust) {
		this.canAdjustPhantom = canAdjust;
		return this;
	}

	public SlotForestry setStackLimit(int limit) {
		this.stackLimit = limit;
		return this;
	}

	public boolean isPhantom() {
		return this.isPhantom;
	}

	public boolean canAdjustPhantom() {
		return this.canAdjustPhantom;
	}

	@Override
	public boolean mayPickup(Player stack) {
		return !isPhantom();
	}

	public boolean canShift() {
		return this.canShift;
	}

	@Override
	public int getMaxStackSize() {
		if (this.stackLimit < 0) {
			return super.getMaxStackSize();
		} else {
			return this.stackLimit;
		}
	}

	public void setToolTips(ToolTip toolTips) {
		this.toolTips = toolTips;
	}

	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		return this.toolTips;
	}

	@Override
	public boolean isToolTipVisible() {
		return getItem().isEmpty();
	}

	@Override
	public boolean isHovering(double mouseX, double mouseY) {
		return mouseX >= this.x && mouseX <= this.x + 16 && mouseY >= this.y && mouseY <= this.y + 16;
	}
}
