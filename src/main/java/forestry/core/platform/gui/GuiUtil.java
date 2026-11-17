package forestry.core.platform.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import forestry.api.core.tooltips.IToolTipProvider;
import forestry.api.core.tooltips.ToolTip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Optional;

// todo this stuff should probably be yeeted
public class GuiUtil {
	// todo replace with AbstractContainerScreen#renderFloatingItem
	public static void drawItemStack(GuiGraphics graphics, ForestryScreen<?> gui, ItemStack stack, int xPos, int yPos) {
		drawItemStack(graphics, gui.font(), stack, xPos, yPos);
	}

	// todo replace with AbstractContainerScreen#renderFloatingItem
	public static void drawItemStack(GuiGraphics graphics, Font font, ItemStack stack, int xPos, int yPos) {
		PoseStack itemRendererStack = graphics.pose();
		itemRendererStack.pushPose();
		graphics.renderItem(stack, xPos, yPos);
		graphics.renderItemDecorations(font, stack, xPos, yPos, null);
		itemRendererStack.popPose();
	}


	public static void drawToolTips(GuiGraphics graphics, ForestryScreen<?> screen, IToolTipProvider provider, ToolTip toolTips, int mouseX, int mouseY) {
		if (!toolTips.isEmpty()) {
			PoseStack pose = graphics.pose();
			pose.pushPose();
			if (provider == null || provider.isRelativeToGui()) {
				pose.translate(-screen.getGuiLeft(), -screen.getGuiTop(), 0);
			}
			Window window = Minecraft.getInstance().getWindow();    //TODO - more resolution stuff to check
			graphics.renderTooltip(screen.font(), toolTips.getLines(), Optional.empty(), mouseX, mouseY);
			//GuiUtils.drawHoveringText(transform, toolTips.getLines(), mouseX, mouseY, window.getGuiScaledWidth(), window.getGuiScaledHeight(), -1, gui.getGameInstance().font);
			pose.popPose();
		}
	}

	public static void drawToolTips(GuiGraphics graphics, ForestryScreen<?> gui, Collection<?> objects, int mouseX, int mouseY) {
		for (Object obj : objects) {
			if (!(obj instanceof IToolTipProvider provider)) {
				continue;
			}
			if (!provider.isToolTipVisible()) {
				continue;
			}
			int mX = mouseX;
			int mY = mouseY;
			if (provider.isRelativeToGui()) {
				mX -= gui.getGuiLeft();
				mY -= gui.getGuiTop();
			}
			ToolTip tips = provider.getToolTip(mX, mY);
			if (tips == null) {
				continue;
			}
			boolean mouseOver = provider.isHovering(mX, mY);
			tips.onTick(mouseOver);
			if (mouseOver && tips.isReady()) {
				tips.refresh();
				drawToolTips(graphics, gui, provider, tips, mouseX, mouseY);
			}
		}
	}

	public static String formatEnergyValue(int energy) {
		NumberFormat format = NumberFormat.getIntegerInstance(Minecraft.getInstance().getLocale());
		return format.format(energy) + " FE";
	}

	/**
	 * Formats an energy rate to at most two decimal places, dropping trailing zeros so a whole
	 * rate still reads "10 FE/t" while a fractional one reads "0.75 FE/t".
	 *
	 * @param rate The energy produced or consumed per tick
	 * @return The formatted rate, including the unit
	 */
	public static String formatRate(double rate) {
		NumberFormat format = NumberFormat.getNumberInstance(Minecraft.getInstance().getLocale());
		format.setMaximumFractionDigits(2);
		return format.format(rate) + " FE/t";
	}
}
