package forestry.core.gui;

import forestry.core.tiles.ILiquidTankTile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;

public abstract class ContainerLiquidTanks<T extends BlockEntity & ILiquidTankTile> extends ContainerTile<T> implements IContainerLiquidTanks {
	private final ContainerLiquidTanksHelper<T> helper;

	protected ContainerLiquidTanks(int windowId, MenuType<?> type, Inventory playerInventory, T tile, int xInv, int yInv) {
		super(windowId, type, playerInventory, tile, xInv, yInv);

		this.helper = new ContainerLiquidTanksHelper<>(tile);

		if (this.player != null) {
			this.tile.getTankManager().sendAllTanks(this, this.player);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handlePipetteClickClient(int slot, Player player) {
        this.helper.handlePipetteClickClient(slot, player);
	}

	@Override
	public void handlePipetteClick(int slot, ServerPlayer player) {
        this.helper.handlePipetteClick(slot, player);
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		if (this.player != null) {
            this.tile.getTankManager().broadcastChanges(this, this.player);
		}
	}

	@Override
	public void removed(Player PlayerEntity) {
		super.removed(PlayerEntity);
        this.tile.getTankManager().onClosed(this);
	}

	@Nullable
	@Override
	public IFluidTank getTank(int slot) {
		return this.tile.getTankManager().getTank(slot);
	}
}
