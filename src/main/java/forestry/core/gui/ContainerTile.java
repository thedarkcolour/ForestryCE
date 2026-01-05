package forestry.core.gui;

import forestry.api.core.IError;
import forestry.api.core.IErrorLogicSource;
import forestry.core.network.packets.PacketErrorUpdate;
import forestry.core.network.packets.PacketGuiEnergy;
import forestry.core.network.packets.PacketGuiStream;
import forestry.core.tiles.IPowerHandler;
import forestry.core.tiles.TilePowered;
import forestry.core.tiles.TileUtil;
import forestry.energy.ForestryEnergyStorage;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Set;

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

	@Deprecated(forRemoval = true)
	protected ContainerTile(int windowId, MenuType<?> type, T tile) {
		super(windowId, type, null);
		this.tile = tile;
	}

	protected ContainerTile(int windowId, MenuType<?> type, T tile, Player player) {
		super(windowId, type, player);
		this.tile = tile;
	}

	@Override
	protected final boolean canAccess(Player player) {
		return true;
	}

	@Override
	public final boolean stillValid(Player player) {
		return TileUtil.isUsableByPlayer(player, this.tile);
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		if (this.tile instanceof IErrorLogicSource errorLogicSource) {
			Set<IError> errorStates = errorLogicSource.getErrorLogic().getErrors();

			if (!errorStates.equals(this.previousErrorStates)) {
				PacketErrorUpdate packet = new PacketErrorUpdate(this.tile, errorLogicSource);
				sendPacketToListeners(packet);
			}

            this.previousErrorStates = Set.copyOf(errorStates);
		}

		if (this.tile instanceof IPowerHandler) {
			ForestryEnergyStorage energyStorage = ((IPowerHandler) this.tile).getEnergyManager();
			int energyManagerData = energyStorage.getEnergyStored();
			if (energyManagerData != this.previousEnergyManagerData) {
				PacketGuiEnergy packet = new PacketGuiEnergy(this.containerId, energyManagerData);
				sendPacketToListeners(packet);

                this.previousEnergyManagerData = energyManagerData;
			}
		}

		if (this.tile instanceof TilePowered tilePowered) {
			boolean guiNeedsUpdate = false;

			int workCounter = tilePowered.getWorkCounter();
			if (workCounter != this.previousWorkCounter) {
				guiNeedsUpdate = true;
                this.previousWorkCounter = workCounter;
			}

			int ticksPerWorkCycle = tilePowered.getTicksPerWorkCycle();
			if (ticksPerWorkCycle != this.previousTicksPerWorkCycle) {
				guiNeedsUpdate = true;
                this.previousTicksPerWorkCycle = ticksPerWorkCycle;
			}

			if (guiNeedsUpdate) {
				PacketGuiStream packet = new PacketGuiStream(tilePowered);
				sendPacketToListeners(packet);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void onGuiEnergy(int energyStored) {
		if (this.tile instanceof IPowerHandler handler) {
			ForestryEnergyStorage energyStorage = handler.getEnergyManager();
			energyStorage.setEnergyStored(energyStored);
		}
	}

	public T getTile() {
		return this.tile;
	}
}
