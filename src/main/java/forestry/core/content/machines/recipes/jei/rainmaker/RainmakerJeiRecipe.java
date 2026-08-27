package forestry.core.content.machines.recipes.jei.rainmaker;

import net.minecraft.world.item.ItemStack;

import forestry.api.core.machines.fuels.RainmakerFuel;

/**
 * One row of the Rainmaker JEI page. The substrate is the data map key and the fuel is its value, so
 * the two are paired here to give JEI a single recipe object.
 *
 * @param substrate The item that activates the rainmaker
 * @param fuel      The weather effect that item triggers
 */
public record RainmakerJeiRecipe(ItemStack substrate, RainmakerFuel fuel) {
}
