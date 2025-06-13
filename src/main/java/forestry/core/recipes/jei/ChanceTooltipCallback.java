package forestry.core.recipes.jei;

import forestry.compat.jei.JeiUtil;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ChanceTooltipCallback implements IRecipeSlotTooltipCallback {
	private final float chance;

	public ChanceTooltipCallback(float chance) {
		if (chance < 0) {
			chance = 0;
		} else if (chance > 1.0) {
			chance = 1.0f;
		}
		this.chance = chance;
	}

	@Override
	public void onTooltip(IRecipeSlotView recipeSlotView, List<Component> tooltip) {
		tooltip.add(JeiUtil.formatChance(chance));
	}
}
