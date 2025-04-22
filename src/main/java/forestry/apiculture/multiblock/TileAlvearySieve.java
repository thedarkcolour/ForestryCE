/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.apiculture.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.apiculture.IBeeListener;
import forestry.api.genetics.pollen.IPollen;
import forestry.api.multiblock.IAlvearyComponent;
import forestry.apiculture.blocks.BlockAlvearyType;
import forestry.apiculture.gui.ContainerAlvearySieve;
import forestry.apiculture.inventory.InventoryAlvearySieve;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.inventory.watchers.ISlotPickupWatcher;

public class TileAlvearySieve extends TileAlveary implements IAlvearyComponent.BeeListener<MultiblockLogicAlveary>, IAlvearyComponent.HasInventory {
	private final IBeeListener beeListener;
	private final InventoryAlvearySieve inventory;

	public TileAlvearySieve(BlockPos pos, BlockState state) {
		super(BlockAlvearyType.SIEVE, pos, state);
		this.inventory = new InventoryAlvearySieve(this);
		this.beeListener = new AlvearySieveBeeListener(inventory);
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		return this.inventory;
	}

	public ISlotPickupWatcher getCrafter() {
		return this.inventory;
	}

	@Override
	public IBeeListener getBeeListener() {
		return this.beeListener;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerAlvearySieve(windowId, inv, this);
	}

	static class AlvearySieveBeeListener implements IBeeListener {
		private final InventoryAlvearySieve inventory;

		public AlvearySieveBeeListener(InventoryAlvearySieve inventory) {
			this.inventory = inventory;
		}

		@Override
		public boolean onPollenRetrieved(IPollen<?> pollen) {
			if (!this.inventory.canStorePollen()) {
				return false;
			}

			ItemStack pollenStack = pollen.createStack();
			if (!pollenStack.isEmpty()) {
				this.inventory.storePollenStack(pollenStack);
				return true;
			}
			return false;
		}
	}
}
