package forestry.core.utils;

import forestry.api.core.tooltips.ToolTip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.List;

public class ItemTooltipUtil {
	// todo this should probably be removed
	public static void addInformation(ItemStack stack, List<Component> tooltip) {
		String translationKey = stack.getDescriptionId();
		String tooltipKey = translationKey + ".tooltip";
		if (Translator.canTranslateToLocal(tooltipKey)) {
			tooltip.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
		}
	}

	public static void addShiftInformation(List<Component> tooltip) {
		tooltip.add(Component.translatable("for.gui.tooltip.tmi", "< %s >").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
	}

	@Nullable
	public static ToolTip getInformation(ItemStack stack, @Nullable Player player, TooltipFlag flag) {
		if (stack.isEmpty()) {
			return null;
		}
		ToolTip toolTip = new ToolTip();
		Item.TooltipContext context = Item.TooltipContext.EMPTY;
		toolTip.addAll(stack.getTooltipLines(context, player, flag));
		return toolTip;
	}
}
