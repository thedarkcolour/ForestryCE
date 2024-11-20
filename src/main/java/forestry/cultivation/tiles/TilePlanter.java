package forestry.cultivation.tiles;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import forestry.api.IForestryApi;
import forestry.api.climate.IClimateProvider;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.farming.IFarmLogic;
import forestry.api.farming.IFarmType;
import forestry.api.farming.IFarmable;
import forestry.core.config.ForestryConfig;
import forestry.core.fluids.ITankManager;
import forestry.core.network.IStreamableGui;
import forestry.core.owner.IOwnedTile;
import forestry.core.owner.IOwnerHandler;
import forestry.core.owner.OwnerHandler;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.tiles.TilePowered;
import forestry.core.utils.PlayerUtil;
import forestry.cultivation.IFarmHousingInternal;
import forestry.cultivation.blocks.BlockTypePlanter;
import forestry.cultivation.gui.ContainerPlanter;
import forestry.cultivation.inventory.InventoryPlanter;
import forestry.farming.FarmHelper;
import forestry.farming.FarmManager;
import forestry.farming.FarmTarget;
import forestry.farming.gui.IFarmLedgerDelegate;
import forestry.farming.multiblock.IFarmInventoryInternal;

public abstract class TilePlanter extends TilePowered implements IFarmHousingInternal, IClimateProvider, ILiquidTankTile, IOwnedTile, IStreamableGui {
	private final InventoryPlanter inventory;
	private final OwnerHandler ownerHandler = new OwnerHandler();
	private final FarmManager manager;

	private boolean manual;
	private final IFarmType properties;
	@Nullable
	private IFarmLogic logic;
	@Nullable
	private Vec3i offset;
	@Nullable
	private Vec3i area;

	protected TilePlanter(BlockEntityType type, BlockPos pos, BlockState state, ResourceLocation farmTypeId) {
		super(type, pos, state, 150, 1500);

		this.properties = Preconditions.checkNotNull(IForestryApi.INSTANCE.getFarmingManager().getFarmType(farmTypeId));
		this.manual = false;
		this.inventory = new InventoryPlanter(this);
		setInternalInventory(inventory);
		this.manager = new FarmManager(this);
		setEnergyPerWorkCycle(10);
		setTicksPerWorkCycle(2);
	}

	public void setManual(boolean manual) {
		this.manual = manual;
		this.logic = this.properties.getLogic(manual);
	}

	@Override
	public Component getDisplayName() {
		String name = getBlockType(BlockTypePlanter.ARBORETUM).getSerializedName();
		return Component.translatable("block.forestry.planter." + (manual ? "manual" : "managed"), Component.translatable("block.forestry." + name));
	}

	@Override
	public boolean hasWork() {
		return true;
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);
		manager.getHydrationManager().updateServer();

		if (updateOnInterval(20)) {
			inventory.drainCan(manager.getTankManager());
		}
	}

	@Override
	protected boolean workCycle() {
		manager.doWork();
		return false;
	}

	@Override
	public void saveAdditional(CompoundTag data) {
		super.saveAdditional(data);
		manager.write(data);
		ownerHandler.write(data);
		data.putBoolean("manual", this.manual);
	}

	@Override
	public void load(CompoundTag data) {
		super.load(data);
		manager.read(data);
		ownerHandler.read(data);
		setManual(data.getBoolean("manual"));
	}

	@Override
	public void writeGuiData(FriendlyByteBuf data) {
		super.writeGuiData(data);
		manager.writeData(data);
	}

	@Override
	public void readGuiData(FriendlyByteBuf data) {
		super.readGuiData(data);
		manager.readData(data);

	}

	@Override
	public void setUpFarmlandTargets(Map<Direction, List<FarmTarget>> targets) {
		BlockPos targetStart = getCoords();
		BlockPos minPos = worldPosition;
		BlockPos maxPos = worldPosition;
		int size = 1;
		int extend = ForestryConfig.SERVER.legacyFarmsPlanterRings.get();

		if (ForestryConfig.SERVER.legacyFarmsUseRings.get()) {
			int ringSize = ForestryConfig.SERVER.legacyFarmsRingSize.get();
			minPos = worldPosition.offset(-ringSize, 0, -ringSize);
			maxPos = worldPosition.offset(ringSize, 0, ringSize);
			size = 1 + ringSize * 2;
			extend--;
		}

		FarmHelper.createTargets(level, this, targets, targetStart, extend, size, size, minPos, maxPos);
		FarmHelper.setExtents(level, this, targets);
	}

	@Override
	public BlockPos getCoords() {
		return worldPosition;
	}

	@Override
	public BlockPos getTopCoord() {
		return worldPosition;
	}

	@Override
	public Vec3i getArea() {
		if (area == null) {
			int basisArea = 5;
			if (ForestryConfig.SERVER.legacyFarmsUseRings.get()) {
				basisArea = basisArea + 1 + ForestryConfig.SERVER.legacyFarmsRingSize.get() * 2;
			}
			area = new Vec3i(basisArea + ForestryConfig.SERVER.legacyFarmsPlanterRings.get(), 13, basisArea + ForestryConfig.SERVER.legacyFarmsPlanterRings.get());
		}
		return area;
	}

	@Override
	public Vec3i getOffset() {
		if (offset == null) {
			Vec3i area = getArea();
			offset = new Vec3i(-area.getX() / 2, -2, -area.getZ() / 2);
		}
		return offset;
	}

	@Override
	public boolean doWork() {
		return false;
	}

	@Override
	public boolean hasLiquid(FluidStack liquid) {
		FluidStack drained = manager.getResourceTank().drainInternal(liquid, IFluidHandler.FluidAction.SIMULATE);
		return liquid.isFluidStackIdentical(drained);
	}

	@Override
	public void removeLiquid(FluidStack liquid) {
		manager.getResourceTank().drain(liquid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
	}

	@Override
	public IOwnerHandler getOwnerHandler() {
		return ownerHandler;
	}

	@Override
	public boolean plantGermling(IFarmable farmable, Level world, BlockPos pos, Direction direction) {
		Player player = PlayerUtil.getFakePlayer(world, getOwnerHandler().getOwner());
		return player != null && inventory.plantGermling(farmable, player, pos, direction);
	}

	@Override
	public boolean isValidPlatform(Level world, BlockPos pos) {
		return pos.getY() == getBlockPos().getY() - 2;
	}

	@Override
	public boolean isSquare() {
		return true;
	}

	@Override
	public boolean canPlantSoil(boolean manual) {
		return !this.manual;
	}

	@Override
	public IFarmInventoryInternal getFarmInventory() {
		return inventory;
	}

	@Override
	public void addPendingProduct(ItemStack stack) {
		manager.addPendingProduct(stack);
	}

	@Override
	public void setFarmLogic(Direction direction, IFarmLogic logic) {
	}

	@Override
	public void resetFarmLogic(Direction direction) {
	}

	@Override
	public IFarmLogic getFarmLogic(Direction direction) {
		return getFarmLogic();
	}

	public IFarmLogic getFarmLogic() {
		return logic;
	}

	@Override
	public Collection<IFarmLogic> getFarmLogics() {
		return Collections.singleton(logic);
	}

	@Override
	public int getStoredFertilizerScaled(int scale) {
		return manager.getFertilizerManager().getStoredFertilizerScaled(inventory, scale);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		manager.clearTargets();
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag data = super.getUpdateTag();
		manager.write(data);
		return data;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerPlanter(windowId, inv, this);
	}

	public IFarmLedgerDelegate getFarmLedgerDelegate() {
		return manager.getHydrationManager();
	}

	@Override
	public TemperatureType temperature() {
		return IForestryApi.INSTANCE.getClimateManager().getTemperature(this.level.getBiome(this.worldPosition));
	}

	@Override
	public HumidityType humidity() {
		return IForestryApi.INSTANCE.getClimateManager().getHumidity(this.level.getBiome(this.worldPosition));
	}

	@Override
	public ITankManager getTankManager() {
		return manager.getTankManager();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == ForgeCapabilities.FLUID_HANDLER) {
			return LazyOptional.of(this::getTankManager).cast();
		}
		return super.getCapability(capability, facing);
	}

	public abstract List<ItemStack> createGermlingStacks();

	public abstract List<ItemStack> createResourceStacks();

	public abstract List<ItemStack> createProductionStacks();

	@Override
	public BlockPos getFarmCorner(Direction direction) {
		return worldPosition.below(2);
	}

	@Override
	public int getExtents(Direction direction, BlockPos pos) {
		return manager.getExtents(direction, pos);
	}

	@Override
	public void setExtents(Direction direction, BlockPos pos, int extend) {
		manager.setExtents(direction, pos, extend);
	}

	@Override
	public void cleanExtents(Direction direction) {
		manager.cleanExtents(direction);
	}
}
