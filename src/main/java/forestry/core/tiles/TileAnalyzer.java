package forestry.core.tiles;

import forestry.api.core.ForestryError;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.core.config.Constants;
import forestry.core.features.CoreTiles;
import forestry.core.fluids.FilteredTank;
import forestry.core.fluids.FluidHelper;
import forestry.core.fluids.FluidTagFilter;
import forestry.core.fluids.TankManager;
import forestry.core.gui.ContainerAnalyzer;
import forestry.core.inventory.InventoryAnalyzer;
import forestry.core.inventory.wrappers.InventoryMapper;
import forestry.core.network.packets.PacketItemStackDisplay;
import forestry.core.utils.GeneticsUtil;
import forestry.core.utils.InventoryUtil;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class TileAnalyzer extends TilePowered implements WorldlyContainer, ILiquidTankTile, IItemStackDisplay {
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
	public TileAnalyzer(BlockPos pos, BlockState state) {
		super(CoreTiles.ANALYZER.tileType(), pos, state, 800, Constants.MACHINE_MAX_ENERGY);
		setInternalInventory(new InventoryAnalyzer(this));
        this.resourceTank = new FilteredTank(Constants.PROCESSOR_TANK_CAPACITY).setFilter(FluidTagFilter.HONEY);
        this.tankManager = new TankManager(this, this.resourceTank);
        this.invInput = new InventoryMapper(getInternalInventory(), InventoryAnalyzer.SLOT_INPUT_1, InventoryAnalyzer.SLOT_INPUT_COUNT);
        this.invOutput = new InventoryMapper(getInternalInventory(), InventoryAnalyzer.SLOT_OUTPUT_1, InventoryAnalyzer.SLOT_OUTPUT_COUNT);
	}

	/* SAVING & LOADING */

	@Override
	public void saveAdditional(CompoundTag compoundNBT) {
		super.saveAdditional(compoundNBT);
        this.tankManager.write(compoundNBT);
	}

	@Override
	public void load(CompoundTag compoundNBT) {
		super.load(compoundNBT);
        this.tankManager.read(compoundNBT);

		ItemStack stackToAnalyze = getItem(InventoryAnalyzer.SLOT_ANALYZE);
		if (!stackToAnalyze.isEmpty()) {
            this.specimenToAnalyze = IIndividualHandlerItem.getIndividual(stackToAnalyze);
		}
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		if (updateOnInterval(20)) {
			// Check if we have suitable items waiting in the can slot
			FluidHelper.drainContainers(this.tankManager, this, InventoryAnalyzer.SLOT_CAN);
		}
	}

	/* WORKING */
	@Override
	public boolean workCycle() {
		ItemStack stackToAnalyze = getItem(InventoryAnalyzer.SLOT_ANALYZE);
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

		setItem(InventoryAnalyzer.SLOT_ANALYZE, ItemStack.EMPTY);
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
	public void writeData(FriendlyByteBuf data) {
		super.writeData(data);
		ItemStack displayStack = getIndividualOnDisplay();
		data.writeItem(displayStack);
        this.tankManager.writeData(data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(FriendlyByteBuf data) {
		super.readData(data);
        this.individualOnDisplayClient = data.readItem();
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

		ItemStack specimen = getItem(InventoryAnalyzer.SLOT_ANALYZE);

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
		if (!getItem(InventoryAnalyzer.SLOT_ANALYZE).isEmpty()) {
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

		setItem(InventoryAnalyzer.SLOT_ANALYZE, inputStack);
        this.invInput.setItem(slotIndex, ItemStack.EMPTY);

		if (this.specimenToAnalyze.isAnalyzed()) {
			setTicksPerWorkCycle(1);
			setEnergyPerWorkCycle(0);
		} else {
			setTicksPerWorkCycle(TIME_TO_ANALYZE);
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
		return getItem(InventoryAnalyzer.SLOT_ANALYZE);
	}

	/* ILiquidTankTile */

	@Override
	public TankManager getTankManager() {
		return this.tankManager;
	}


	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == ForgeCapabilities.FLUID_HANDLER) {
			return LazyOptional.of(() -> this.tankManager).cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerAnalyzer(windowId, player.getInventory(), this);
	}
}
