package forestry.core.gui;

import forestry.core.circuits.ISocketable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ContainerSocketed<T extends BlockEntity & ISocketable> extends ContainerTile<T> implements IContainerSocketed {

	private final ContainerSocketedHelper<T> helper;

	protected ContainerSocketed(int windowId, MenuType<?> type, Inventory playerInventory, T tile, int xInv, int yInv) {
		super(windowId, type, playerInventory, tile, xInv, yInv);
		this.helper = new ContainerSocketedHelper<>(this.tile);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleChipsetClick(int slot) {
        this.helper.handleChipsetClick(slot);
	}

	@Override
	public void handleChipsetClickServer(int slot, ServerPlayer player, ItemStack itemstack) {
        this.helper.handleChipsetClickServer(slot, player, itemstack);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleSolderingIronClick(int slot) {
        this.helper.handleSolderingIronClick(slot);
	}

	@Override
	public void handleSolderingIronClickServer(int slot, ServerPlayer player, ItemStack itemstack) {
        this.helper.handleSolderingIronClickServer(slot, player, itemstack);
	}
}
