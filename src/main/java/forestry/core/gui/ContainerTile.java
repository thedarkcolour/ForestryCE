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
package forestry.core.gui;

import javax.annotation.Nullable;
import java.util.Set;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.api.core.IError;
import forestry.api.core.IErrorLogicSource;
import forestry.core.network.packets.PacketErrorUpdate;
import forestry.core.network.packets.PacketGuiEnergy;
import forestry.core.network.packets.PacketGuiStream;
import forestry.core.tiles.IPowerHandler;
import forestry.core.tiles.TilePowered;
import forestry.core.tiles.TileUtil;
import forestry.energy.ForestryEnergyStorage;

public abstract class ContainerTile<T extends BlockEntity> extends ContainerForestry {
	protected final T tile;
	@Nullable
	private Set<IError> previousErrorStates;
	private int previousEnergyManagerData = 0;
	private int previousWorkCounter = 0;
	private int previousTicksPerWorkCycle = 0;

	protected ContainerTile(int windowId, MenuType<?> type, Inventory playerInventory, T tile, int xInv, int yInv) {
		super(windowId, type, playerInventory.player);
		addPlayerInventory(playerInventory, xInv, yInv);
		this.tile = tile;
	}

	protected ContainerTile(int windowId, MenuType<?> type, T tile) {
		super(windowId, type, null);
		this.tile = tile;
	}

	@Override
	protected final boolean canAccess(Player player) {
		return true;
	}

	@Override
	public final boolean stillValid(Player player) {
		return TileUtil.isUsableByPlayer(player, tile);
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		if (tile instanceof IErrorLogicSource errorLogicSource) {
			Set<IError> errorStates = errorLogicSource.getErrorLogic().getErrors();

			if (!errorStates.equals(previousErrorStates)) {
				PacketErrorUpdate packet = new PacketErrorUpdate(tile, errorLogicSource);
				sendPacketToListeners(packet);
			}

			previousErrorStates = Set.copyOf(errorStates);
		}

		if (tile instanceof IPowerHandler) {
			ForestryEnergyStorage energyStorage = ((IPowerHandler) tile).getEnergyManager();
			int energyManagerData = energyStorage.getEnergyStored();
			if (energyManagerData != previousEnergyManagerData) {
				PacketGuiEnergy packet = new PacketGuiEnergy(containerId, energyManagerData);
				sendPacketToListeners(packet);

				previousEnergyManagerData = energyManagerData;
			}
		}

		if (tile instanceof TilePowered tilePowered) {
			boolean guiNeedsUpdate = false;

			int workCounter = tilePowered.getWorkCounter();
			if (workCounter != previousWorkCounter) {
				guiNeedsUpdate = true;
				previousWorkCounter = workCounter;
			}

			int ticksPerWorkCycle = tilePowered.getTicksPerWorkCycle();
			if (ticksPerWorkCycle != previousTicksPerWorkCycle) {
				guiNeedsUpdate = true;
				previousTicksPerWorkCycle = ticksPerWorkCycle;
			}

			if (guiNeedsUpdate) {
				PacketGuiStream packet = new PacketGuiStream(tilePowered);
				sendPacketToListeners(packet);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void onGuiEnergy(int energyStored) {
		if (tile instanceof IPowerHandler handler) {
			ForestryEnergyStorage energyStorage = handler.getEnergyManager();
			energyStorage.setEnergyStored(energyStored);
		}
	}

	public T getTile() {
		return tile;
	}
}
