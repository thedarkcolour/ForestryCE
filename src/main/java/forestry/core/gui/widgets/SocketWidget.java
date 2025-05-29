package forestry.core.gui.widgets;

import forestry.api.core.tooltips.ToolTip;
import forestry.core.circuits.ISocketable;
import forestry.core.circuits.ISolderingIron;
import forestry.core.circuits.ItemCircuitBoard;
import forestry.core.gui.GuiUtil;
import forestry.core.gui.IContainerSocketed;
import forestry.core.utils.ItemTooltipUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

public class SocketWidget extends Widget {

	private final ISocketable tile;
	private final int slot;

	public SocketWidget(WidgetManager manager, int xPos, int yPos, ISocketable tile, int slot) {
		super(manager, xPos, yPos);
		this.tile = tile;
		this.slot = slot;
	}

	@Override
	public void draw(GuiGraphics graphics, int startX, int startY) {
		ItemStack socketStack = this.tile.getSocket(this.slot);
		if (!socketStack.isEmpty()) {
			GuiUtil.drawItemStack(graphics, Minecraft.getInstance().font, socketStack, this.xPos, this.yPos);
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		return this.toolTip;
	}

	private final ToolTip toolTip = new ToolTip(250) {
		@Override
		@OnlyIn(Dist.CLIENT)
		public void refresh() {
            SocketWidget.this.toolTip.clear();
			ItemStack stack = SocketWidget.this.tile.getSocket(SocketWidget.this.slot);
			if (!stack.isEmpty()) {
                SocketWidget.this.toolTip.addAll(ItemTooltipUtil.getInformation(stack));
                SocketWidget.this.toolTip.add(Component.translatable("for.gui.socket.remove").withStyle(ChatFormatting.ITALIC));
			} else {
                SocketWidget.this.toolTip.add(Component.translatable("for.gui.emptysocket"));
			}
		}
	};

	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton) {
		ItemStack itemstack = Minecraft.getInstance().player.containerMenu.getCarried();

		if (itemstack.isEmpty()) {
			if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {

			}
			return;
		}

		Item held = itemstack.getItem();

		AbstractContainerMenu container = this.manager.gui.getMenu();
		if (!(container instanceof IContainerSocketed containerSocketed)) {
			return;
		}

		// Insert chipsets
		if (held instanceof ItemCircuitBoard) {
			containerSocketed.handleChipsetClick(this.slot);
		} else if (held instanceof ISolderingIron) {
			containerSocketed.handleSolderingIronClick(this.slot);
		}
	}
}
