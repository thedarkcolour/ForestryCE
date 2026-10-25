package forestry.core.content.analyzer;

import forestry.api.core.ForestryError;
import forestry.api.core.genetics.IIndividual;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import forestry.core.platform.config.Constants;
import forestry.core.features.CoreTiles;
import forestry.core.platform.fluids.FilteredTank;
import forestry.core.platform.fluids.FluidHelper;
import forestry.core.platform.fluids.FluidTagFilter;
import forestry.core.platform.fluids.TankManager;
import forestry.core.platform.inventory.AnalyzerInventory;
import forestry.core.platform.inventory.wrappers.InventoryMapper;
import forestry.core.platform.network.packets.PacketItemStackDisplay;
import forestry.core.platform.util.GeneticsUtil;
import forestry.core.platform.util.InventoryUtil;
import forestry.core.platform.util.NetworkUtil;
import forestry.core.platform.util.SpeciesUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import forestry.core.platform.tile.IItemStackDisplay;
import forestry.core.platform.tile.ILiquidTankTile;
import forestry.core.platform.tile.PoweredBlockEntity;

public class AnalyzerBlockEntity extends PoweredBlockEntity implements WorldlyContainer, ILiquidTankTile, IItemStackDisplay {
	private static final int TIME_TO_ANALYZE = 125;
	private static final int HONEY_REQUIRED = 100;
	// Genetics
	public static int analyzerEnergyPerWork = 20320;

	private final FilteredTank resourceTank;
	private final TankManager tankManager;
	private final Container invInput;
	private final Container invOutput;

	@Nullable
	private IIndividual specimenToAnalyze;
	private ItemStack individualOnDisplayClient = ItemStack.EMPTY;

	/* CONSTRUCTOR */
	public AnalyzerBlockEntity(BlockPos pos, BlockState state) {
		super(CoreTiles.ANALYZER.tileType(), pos, state, 800, Constants.MACHINE_MAX_ENERGY);
		setInternalInventory(new AnalyzerInventory(this));
        this.resourceTank = new FilteredTank(Constants.PROCESSOR_TANK_CAPACITY).setFilter(FluidTagFilter.HONEY);
        this.tankManager = new TankManager(this, this.resourceTank);
        this.invInput = new InventoryMapper(getInternalInventory(), AnalyzerInventory.SLOT_INPUT_1, AnalyzerInventory.SLOT_INPUT_COUNT);
        this.invOutput = new InventoryMapper(getInternalInventory(), AnalyzerInventory.SLOT_OUTPUT_1, AnalyzerInventory.SLOT_OUTPUT_COUNT);
	}

	/* SAVING & LOADING */

	@Override
	public void saveAdditional(CompoundTag compoundNBT, HolderLookup.Provider registries) {
		super.saveAdditional(compoundNBT, registries);
        this.tankManager.write(compoundNBT, registries);
	}

	@Override
	public void loadAdditional(CompoundTag compoundNBT, HolderLookup.Provider registries) {
		super.loadAdditional(compoundNBT, registries);
        this.tankManager.read(compoundNBT, registries);

		ItemStack stackToAnalyze = getItem(AnalyzerInventory.SLOT_ANALYZE);
		if (!stackToAnalyze.isEmpty()) {
            this.specimenToAnalyze = IIndividualHandlerItem.getIndividual(stackToAnalyze);
		}
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		if (updateOnInterval(20)) {
			// Check if we have suitable items waiting in the can slot
			FluidHelper.drainContainers(this.tankManager, this, AnalyzerInventory.SLOT_CAN);
		}
	}

	/* WORKING */
	@Override
	public boolean workCycle() {
		ItemStack stackToAnalyze = getItem(AnalyzerInventory.SLOT_ANALYZE);
		if (stackToAnalyze.isEmpty() || this.specimenToAnalyze == null) {
			return false;
		}

		if (!this.specimenToAnalyze.isAnalyzed()) {
			FluidStack drained = this.resourceTank.drain(HONEY_REQUIRED, IFluidHandler.FluidAction.SIMULATE);
			if (drained.isEmpty() || drained.getAmount() != HONEY_REQUIRED) {
				return false;
			}
            this.resourceTank.drain(HONEY_REQUIRED, IFluidHandler.FluidAction.EXECUTE);

            this.specimenToAnalyze.analyze();

            this.specimenToAnalyze.saveToStack(stackToAnalyze);
		}

		boolean added = InventoryUtil.tryAddStack(this.invOutput, stackToAnalyze, true);
		if (!added) {
			return false;
		}

		setItem(AnalyzerInventory.SLOT_ANALYZE, ItemStack.EMPTY);
		PacketItemStackDisplay packet = new PacketItemStackDisplay(this, getIndividualOnDisplay());
		NetworkUtil.sendNetworkPacket(packet, this.worldPosition, this.level);

		return true;
	}

	@Nullable
	private Integer getInputSlotIndex() {
		for (int slotIndex = 0; slotIndex < this.invInput.getContainerSize(); slotIndex++) {
			ItemStack stack = this.invInput.getItem(slotIndex);
			if (IIndividualHandlerItem.isIndividual(stack)) {
				return slotIndex;
			}
		}
		return null;
	}

	/* Network */
	@Override
	public void writeData(RegistryFriendlyByteBuf data) {
		super.writeData(data);
		ItemStack displayStack = getIndividualOnDisplay();
		ItemStack.OPTIONAL_STREAM_CODEC.encode(data, displayStack);
        this.tankManager.writeData(data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(RegistryFriendlyByteBuf data) {
		super.readData(data);
        this.individualOnDisplayClient = ItemStack.OPTIONAL_STREAM_CODEC.decode(data);
        this.tankManager.readData(data);
	}

	@Override
	public void handleItemStackForDisplay(ItemStack itemStack) {
		if (!ItemStack.matches(itemStack, this.individualOnDisplayClient)) {
            this.individualOnDisplayClient = itemStack;
			//TODO
			BlockPos pos = getBlockPos();
			Minecraft.getInstance().levelRenderer.setSectionDirty(pos.getX(), pos.getY(), pos.getZ());
			//			world.markForRerender(getPos());
		}
	}

	/* STATE INFORMATION */
	@Override
	public boolean hasWork() {
		moveSpecimenToAnalyzeSlot();

		ItemStack specimen = getItem(AnalyzerInventory.SLOT_ANALYZE);

		boolean hasSpecimen = !specimen.isEmpty();
		boolean hasResource = true;
		boolean hasSpace = true;

		if (hasSpecimen) {
			hasSpace = InventoryUtil.tryAddStack(this.invOutput, specimen, true, false);

			if (this.specimenToAnalyze != null && !this.specimenToAnalyze.isAnalyzed()) {
				FluidStack drained = this.resourceTank.drain(HONEY_REQUIRED, IFluidHandler.FluidAction.SIMULATE);
				hasResource = !drained.isEmpty() && drained.getAmount() == HONEY_REQUIRED;
			}
		}

		getErrorLogic().setCondition(!hasSpecimen, ForestryError.NO_SPECIMEN);
		getErrorLogic().setCondition(!hasResource, ForestryError.NO_RESOURCE_LIQUID);
		getErrorLogic().setCondition(!hasSpace, ForestryError.NO_SPACE_INVENTORY);

		return hasSpecimen && hasResource && hasSpace;
	}

	private void moveSpecimenToAnalyzeSlot() {
		if (!getItem(AnalyzerInventory.SLOT_ANALYZE).isEmpty()) {
			return;
		}

		Integer slotIndex = getInputSlotIndex();
		if (slotIndex == null) {
			return;
		}

		ItemStack inputStack = this.invInput.getItem(slotIndex);
		if (inputStack.isEmpty()) {
			return;
		}

		if (!SpeciesUtil.TREE_TYPE.get().isMember(inputStack)) {
			inputStack = GeneticsUtil.convertToGeneticEquivalent(inputStack);
		}

        this.specimenToAnalyze = IIndividualHandlerItem.getIndividual(inputStack);
		if (this.specimenToAnalyze == null) {
			return;
		}

		setItem(AnalyzerInventory.SLOT_ANALYZE, inputStack);
        this.invInput.setItem(slotIndex, ItemStack.EMPTY);

		if (this.specimenToAnalyze.isAnalyzed()) {
			setStepsPerWorkCycle(1);
			setEnergyPerWorkCycle(0);
		} else {
			setStepsPerWorkCycle(TIME_TO_ANALYZE);
			setEnergyPerWorkCycle(analyzerEnergyPerWork);
		}

		PacketItemStackDisplay packet = new PacketItemStackDisplay(this, getIndividualOnDisplay());
		NetworkUtil.sendNetworkPacket(packet, this.worldPosition, this.level);
	}

	public ItemStack getIndividualOnDisplay() {
		// null in BEWLR
		if (this.level == null || this.level.isClientSide) {
			return this.individualOnDisplayClient;
		}
		return getItem(AnalyzerInventory.SLOT_ANALYZE);
	}

	/* ILiquidTankTile */

	@Override
	public TankManager getTankManager() {
		return this.tankManager;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new AnalyzerMenu(windowId, player.getInventory(), this);
	}
}
