package forestry.api.client.arboriculture;

import forestry.api.ForestryConstants;
import forestry.api.client.IForestryClientApi;
import forestry.api.client.plugin.IClientHelper;

/**
 * Default leaf sprites used by base Forestry.
 */
public class ForestryLeafSprites {
	private static final IClientHelper HELPER = IForestryClientApi.INSTANCE.getHelper();

	public static final ILeafSprite OAK = HELPER.createLeafSprite(ForestryConstants.forestry("oak"));
	public static final ILeafSprite BIRCH = HELPER.createLeafSprite(ForestryConstants.forestry("birch"));
	public static final ILeafSprite SPRUCE = HELPER.createLeafSprite(ForestryConstants.forestry("spruce"));
	public static final ILeafSprite JUNGLE = HELPER.createLeafSprite(ForestryConstants.forestry("jungle"));
	public static final ILeafSprite ACACIA = HELPER.createLeafSprite(ForestryConstants.forestry("acacia"));
	public static final ILeafSprite MANGROVE = HELPER.createLeafSprite(ForestryConstants.forestry("mangrove"));
	public static final ILeafSprite CHERRY = HELPER.createLeafSprite(ForestryConstants.forestry("cherry"));
	public static final ILeafSprite WILLOW = HELPER.createLeafSprite(ForestryConstants.forestry("willow"));
	public static final ILeafSprite MAPLE = HELPER.createLeafSprite(ForestryConstants.forestry("maple"));
	public static final ILeafSprite PALM = HELPER.createLeafSprite(ForestryConstants.forestry("palm"));
}
