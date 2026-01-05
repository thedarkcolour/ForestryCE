package forestry.apiculture.multiblock;

import forestry.api.apiculture.*;
import forestry.api.climate.IClimateProvider;
import forestry.api.core.HumidityType;
import forestry.api.core.IErrorLogic;
import forestry.api.core.TemperatureType;
import forestry.api.multiblock.IAlvearyComponent;
import forestry.api.multiblock.IMultiblockController;
import forestry.apiculture.blocks.BlockAlveary;
import forestry.apiculture.blocks.BlockAlvearyType;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.gui.ContainerAlveary;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.multiblock.MultiblockTileEntityForestry;
import forestry.core.network.IStreamableGui;
import forestry.core.owner.IOwnedTile;
import forestry.core.owner.IOwnerHandler;
import forestry.core.tiles.ITitled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;

public class TileAlveary extends MultiblockTileEntityForestry<MultiblockLogicAlveary> implements IBeeHousing, IAlvearyComponent<MultiblockLogicAlveary>, IOwnedTile, IStreamableGui, ITitled, IClimateProvider {
	private final String translationKey;

	// For Forestry only
	public TileAlveary(BlockAlvearyType type, BlockPos pos, BlockState state) {
		this(type.getTileType().tileType(), ApicultureBlocks.ALVEARY.get(type).getTranslationKey(), pos, state);
	}

	// For addons
	public TileAlveary(BlockEntityType<?> type, String translationKey, BlockPos pos, BlockState state) {
		super(type, pos, state, new MultiblockLogicAlveary());

		this.translationKey = translationKey;
	}

	@Override
	public void onMachineAssembled(IMultiblockController multiblockController, BlockPos minCoord, BlockPos maxCoord) {
		Block block = getBlockState().getBlock();
		if (block instanceof BlockAlveary alveary) {
			this.level.setBlockAndUpdate(getBlockPos(), alveary.getNewState(this));
		}
	}

	@Override
	public void onMachineBroken() {
		Block block = getBlockState().getBlock();
		if (block instanceof BlockAlveary alveary) {
			this.level.setBlockAndUpdate(getBlockPos(), alveary.getNewState(this));
		}
		setChanged();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == ForgeCapabilities.ITEM_HANDLER) {
			if (facing != null) {
				// TODO why is sided inventory used here? the side is actually ignored, see in InventoryAdapter
				return LazyOptional.of(() -> new SidedInvWrapper(getInternalInventory(), facing)).cast();
			} else {
				return LazyOptional.of(() -> new InvWrapper(getInternalInventory())).cast();
			}
		}
		return super.getCapability(capability, facing);
	}

	/* IHousing */
	@Override
	public Holder<Biome> getBiome() {
		return getMultiblockLogic().getController().getBiome();
	}

	/* IBeeHousing */
	@Override
	public Iterable<IBeeModifier> getBeeModifiers() {
		return getMultiblockLogic().getController().getBeeModifiers();
	}

	@Override
	public Iterable<IBeeListener> getBeeListeners() {
		return getMultiblockLogic().getController().getBeeListeners();
	}

	@Override
	public IBeeHousingInventory getBeeInventory() {
		return getMultiblockLogic().getController().getBeeInventory();
	}

	@Override
	public IBeekeepingLogic getBeekeepingLogic() {
		return getMultiblockLogic().getController().getBeekeepingLogic();
	}

	@Override
	public Vec3 getBeeFXCoordinates() {
		return getMultiblockLogic().getController().getBeeFXCoordinates();
	}

	/* IClimatised */
	@Override
	public TemperatureType temperature() {
		return getMultiblockLogic().getController().temperature();
	}

	@Override
	public HumidityType humidity() {
		return getMultiblockLogic().getController().humidity();
	}

	@Override
	public int getBlockLightValue() {
		return getMultiblockLogic().getController().getBlockLightValue();
	}

	@Override
	public boolean canBlockSeeTheSky() {
		return getMultiblockLogic().getController().canBlockSeeTheSky();
	}

	@Override
	public boolean isRaining() {
		return getMultiblockLogic().getController().isRaining();
	}

	@Override
	public IErrorLogic getErrorLogic() {
		return getMultiblockLogic().getController().getErrorLogic();
	}

	@Override
	public IOwnerHandler getOwnerHandler() {
		return getMultiblockLogic().getController().getOwnerHandler();
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		return getMultiblockLogic().getController().getInternalInventory();
	}

	@Override
	public Component getTitle() {
		return Component.translatable(this.translationKey);
	}

	/* IStreamableGui */
	@Override
	public void writeGuiData(FriendlyByteBuf data) {
		getMultiblockLogic().getController().writeGuiData(data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readGuiData(FriendlyByteBuf data) {
		getMultiblockLogic().getController().readGuiData(data);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerAlveary(windowId, player.getInventory(), this);
	}

	@Override
	public Component getDisplayName() {
		return getTitle();
	}
}
