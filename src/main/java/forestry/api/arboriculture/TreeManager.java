/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.arboriculture;

import forestry.api.IForestryApi;

/**
 * @deprecated Use {@link IForestryApi#getTreeManager}
 */
@Deprecated(forRemoval = true)
public class TreeManager {
	/**
	 * Convenient access to wood items.
	 *
	 * @deprecated Use {@link ITreeManager#getWoodAccess}
	 */
	@Deprecated(forRemoval = true)
	public static IWoodAccess woodAccess;

	/**
	 * Can be used to add new charcoal pile walls.
	 *
	 * @deprecated Use {@link ITreeManager#getCharcoalManager}
	 */
	@Deprecated(forRemoval = true)
	public static ICharcoalManager charcoalManager;
}
