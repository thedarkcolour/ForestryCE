package forestry.agriculture.multifarm.tiles;

import forestry.agriculture.multifarm.multiblock.MultifarmController;
import forestry.api.core.IErrorLogic;
import forestry.api.core.IErrorLogicSource;
import forestry.api.core.multiblock.IFarmComponent;
import forestry.api.core.multiblock.IMultiblockController;
import forestry.core.engine.circuits.ISocketable;
import forestry.api.core.IInventoryAdapter;
import forestry.core.platform.multiblock.MultiblockTileEntityForestry;
import forestry.core.platform.network.IStreamableGui;
import forestry.core.platform.owner.IOwnedTile;
import forestry.core.platform.owner.IOwnerHandler;
import forestry.core.platform.tile.ITitled;
import forestry.agriculture.multifarm.gui.MultifarmMenu;
import forestry.agriculture.multifarm.multiblock.MultiblockLogicFarm;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MultifarmBlockEntity extends MultiblockTileEntityForestry<MultiblockLogicFarm> implements IFarmComponent, ISocketable, IStreamableGui, IErrorLogicSource, IOwnedTile, ITitled {
	protected MultifarmBlockEntity(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state) {
		super(tileEntityType, pos, state, new MultiblockLogicFarm());
	}

	@Override
	public forestry.core.platform.multiblock.MultiblockController createController(net.minecraft.world.level.Level level) {
		return new MultifarmController(level);
	}

	@Override
	public forestry.core.platform.multiblock.pattern.MultiblockPattern getPattern() {
		return forestry.agriculture.multifarm.multiblock.FarmPattern.FARM_PATTERN;
	}

	@Override
	public String patternTypeId() {
		return forestry.agriculture.multifarm.multiblock.FarmPattern.PART;
	}

	@Override
	public void onMachineAssembled(IMultiblockController multiblockController, BlockPos minCoord, BlockPos maxCoord) {
        this.level.updateNeighborsAt(getBlockPos(), this.level.getBlockState(this.worldPosition).getBlock());    //TODO - removing false OK?
		setChanged();
	}

	@Override
	public void onMachineBroken() {
        this.level.updateNeighborsAt(getBlockPos(), this.level.getBlockState(this.worldPosition).getBlock());
		setChanged();
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		return getMultiblockLogic().getController().getInternalInventory();
	}

	/* ISocketable */
	@Override
	public int getSocketCount() {
		return getMultiblockLogic().getController().getSocketCount();
	}

	@Override
	public ItemStack getSocket(int slot) {
		return getMultiblockLogic().getController().getSocket(slot);
	}

	@Override
	public void setSocket(int slot, ItemStack stack) {
		getMultiblockLogic().getController().setSocket(slot, stack);
	}

	@Override
	public ResourceLocation getSocketType() {
		return getMultiblockLogic().getController().getSocketType();
	}

	/* IStreamableGui */
	@Override
	public void writeGuiData(RegistryFriendlyByteBuf data) {
		getMultiblockLogic().getController().writeGuiData(data);
	}

	@Override
	public void readGuiData(RegistryFriendlyByteBuf data) {
		getMultiblockLogic().getController().readGuiData(data);
	}

	/* IErrorLogicSource */
	@Override
	public IErrorLogic getErrorLogic() {
		return getMultiblockLogic().getController().getErrorLogic();
	}

	@Override
	public IOwnerHandler getOwnerHandler() {
		return getMultiblockLogic().getController().getOwnerHandler();
	}

	/* ITitled */
	@Override
	public Component getTitle() {
		return Component.translatable("for.gui.farm.title");
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new MultifarmMenu(windowId, inv, this);
	}

	@Override
	public Component getDisplayName() {
		return getTitle();
	}
}
