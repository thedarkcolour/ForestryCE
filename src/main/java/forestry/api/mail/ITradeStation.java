/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.mail;

import forestry.core.inventory.IInventoryAdapter;
import forestry.mail.IWatchable;
import net.minecraft.world.Container;

import javax.annotation.Nullable;

public interface ITradeStation extends ILetterHandler, Container, IWatchable, IInventoryAdapter {

	@Nullable
	IMailAddress getAddress();

	boolean isValid();

	void invalidate();

	void setVirtual(boolean isVirtual);

	boolean isVirtual();

	ITradeStationInfo getTradeInfo();

}
