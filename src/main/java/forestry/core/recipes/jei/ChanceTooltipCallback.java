package forestry.core.recipes.jei;

import java.util.List;

import net.minecraft.network.chat.Component;

import forestry.core.utils.JeiUtil;

import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;

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
