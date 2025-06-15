package forestry.api;

import forestry.api.apiculture.IBeeProtection;
import forestry.api.core.ISpectacleVision;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import net.neoforged.neoforge.capabilities.ItemCapability;

import static forestry.api.ForestryConstants.forestry;

/**
 * All capabilities added by base Forestry.
 */
public class ForestryCapabilities {
	/**
	 * Items with this capability can protect the wearer from harmful bee effects.
	 */
	public static ItemCapability<IBeeProtection, Void> BEE_PROTECTION = ItemCapability.createVoid(forestry("bee_protection"), IBeeProtection.class);

	/**
	 * Grants the wearer the ability to see wild bee hives and pollinated leaves more easily.
	 */
	public static ItemCapability<ISpectacleVision, Void> SPECTACLE_VISION = ItemCapability.createVoid(forestry("spectacle_vision"), ISpectacleVision.class);

	/**
	 * Items with this capability support Forestry's genetic data.
	 */
	public static ItemCapability<IIndividualHandlerItem, Void> INDIVIDUAL_HANDLER_ITEM = ItemCapability.createVoid(forestry("individual"), IIndividualHandlerItem.class);
}
