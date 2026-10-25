package forestry.agriculture.minifarm.tiles;

import com.google.common.base.Preconditions;
import forestry.api.IForestryApi;
import forestry.api.core.climate.IClimateProvider;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.agriculture.IFarmLogic;
import forestry.api.agriculture.IFarmType;
import forestry.api.agriculture.IFarmable;
import forestry.core.platform.config.ForestryConfig;
import forestry.core.platform.fluids.ITankManager;
import forestry.core.platform.network.IStreamableGui;
import forestry.core.platform.owner.IOwnedTile;
import forestry.core.platform.owner.IOwnerHandler;
import forestry.core.platform.owner.OwnerHandler;
import forestry.core.platform.tile.ILiquidTankTile;
import forestry.core.platform.tile.PoweredBlockEntity;
import forestry.core.platform.util.PlayerUtil;
import forestry.agriculture.farmlogic.IFarmHousingInternal;
import forestry.agriculture.minifarm.blocks.MinifarmBlockType;
import forestry.agriculture.minifarm.gui.MinifarmMenu;
import forestry.agriculture.minifarm.inventory.MinifarmInventory;
import forestry.agriculture.farmlogic.FarmHelper;
import forestry.agriculture.farmlogic.FarmManager;
import forestry.agriculture.farmlogic.FarmTarget;
import forestry.agriculture.multifarm.gui.IFarmLedgerDelegate;
import forestry.agriculture.multifarm.multiblock.IFarmInventoryInternal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractMinifarmBlockEntity extends PoweredBlockEntity implements IFarmHousingInternal, IClimateProvider, ILiquidTankTile, IOwnedTile, IStreamableGui {
	private final MinifarmInventory inventory;
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

	protected AbstractMinifarmBlockEntity(BlockEntityType type, BlockPos pos, BlockState state, ResourceLocation farmTypeId) {
		super(type, pos, state, 150, 1500);

		this.properties = Preconditions.checkNotNull(IForestryApi.INSTANCE.getFarmingManager().getFarmType(farmTypeId));
		this.manual = false;
		this.inventory = new MinifarmInventory(this);
		setInternalInventory(this.inventory);
		this.manager = new FarmManager(this);
		setEnergyPerWorkCycle(10);
		setStepsPerWorkCycle(2);
	}

	public void setManual(boolean manual) {
		this.manual = manual;
		this.logic = this.properties.getLogic(manual);
	}

	@Override
	public Component getDisplayName() {
		String name = getBlockType(MinifarmBlockType.ARBORETUM).getSerializedName();
		return Component.translatable("block.forestry.planter." + (this.manual ? "manual" : "managed"), Component.translatable("block.forestry." + name));
	}

	@Override
	public boolean hasWork() {
		return true;
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);
        this.manager.getHydrationManager().updateServer();

		if (updateOnInterval(20)) {
            this.inventory.drainCan(this.manager.getTankManager());
		}
	}

	@Override
	protected boolean workCycle() {
		// the return value resets the work counter, which is what makes the next cycle draw energy again
		return this.manager.doWork();
	}

	@Override
	public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
		super.saveAdditional(data, registries);
        this.manager.write(data, registries);
        this.ownerHandler.write(data, registries);
		data.putBoolean("manual", this.manual);
	}

	@Override
	public void loadAdditional(CompoundTag data, HolderLookup.Provider registries) {
		super.loadAdditional(data, registries);
        this.manager.read(data, registries);
        this.ownerHandler.read(data, registries);
		setManual(data.getBoolean("manual"));
	}

	@Override
	public void writeGuiData(RegistryFriendlyByteBuf data) {
		super.writeGuiData(data);
        this.manager.writeData(data);
	}

	@Override
	public void readGuiData(RegistryFriendlyByteBuf data) {
		super.readGuiData(data);
        this.manager.readData(data);

	}

	@Override
	public void setUpFarmlandTargets(Map<Direction, List<FarmTarget>> targets) {
		BlockPos targetStart = getCoords();
		BlockPos minPos = this.worldPosition;
		BlockPos maxPos = this.worldPosition;
		int size = 1;
		int extend = ForestryConfig.SERVER.legacyFarmsPlanterRings.get();

		if (ForestryConfig.SERVER.legacyFarmsUseRings.get()) {
			int ringSize = ForestryConfig.SERVER.legacyFarmsRingSize.get();
			minPos = this.worldPosition.offset(-ringSize, 0, -ringSize);
			maxPos = this.worldPosition.offset(ringSize, 0, ringSize);
			size = 1 + ringSize * 2;
			extend--;
		}

		FarmHelper.createTargets(this.level, this, targets, targetStart, extend, size, size, minPos, maxPos);
		FarmHelper.setExtents(this.level, this, targets);
	}

	@Override
	public BlockPos getCoords() {
		return this.worldPosition;
	}

	@Override
	public BlockPos getTopCoord() {
		return this.worldPosition;
	}

	@Override
	public Vec3i getArea() {
		if (this.area == null) {
			int basisArea = 5;
			if (ForestryConfig.SERVER.legacyFarmsUseRings.get()) {
				basisArea = basisArea + 1 + ForestryConfig.SERVER.legacyFarmsRingSize.get() * 2;
			}
            this.area = new Vec3i(basisArea + ForestryConfig.SERVER.legacyFarmsPlanterRings.get(), 13, basisArea + ForestryConfig.SERVER.legacyFarmsPlanterRings.get());
		}
		return this.area;
	}

	@Override
	public Vec3i getOffset() {
		if (this.offset == null) {
			Vec3i area = getArea();
            this.offset = new Vec3i(-area.getX() / 2, -2, -area.getZ() / 2);
		}
		return this.offset;
	}

	@Override
	public boolean doWork() {
		return false;
	}

	@Override
	public boolean hasLiquid(FluidStack liquid) {
		FluidStack drained = this.manager.getResourceTank().drainInternal(liquid, IFluidHandler.FluidAction.SIMULATE);
		return FluidStack.matches(liquid, drained);
	}

	@Override
	public void removeLiquid(FluidStack liquid) {
        this.manager.getResourceTank().drain(liquid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
	}

	@Override
	public IOwnerHandler getOwnerHandler() {
		return this.ownerHandler;
	}

	@Override
	public boolean plantGermling(IFarmable farmable, Level world, BlockPos pos, Direction direction) {
		Player player = PlayerUtil.getFakePlayer(world, getOwnerHandler().getOwner());
		return player != null && this.inventory.plantGermling(farmable, player, pos, direction);
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
		return this.inventory;
	}

	@Override
	public void addPendingProduct(ItemStack stack) {
        this.manager.addPendingProduct(stack);
	}

	@Override
	public void markMissingSoil() {
		this.manager.markMissingSoil();
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
		return this.logic;
	}

	@Override
	public Collection<IFarmLogic> getFarmLogics() {
		return Collections.singleton(this.logic);
	}

	@Override
	public int getStoredFertilizerScaled(int scale) {
		return this.manager.getFertilizerManager().getStoredFertilizerScaled(this.inventory, scale);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
        this.manager.clearTargets();
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag data = super.getUpdateTag(registries);
        this.manager.write(data, registries);
		return data;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new MinifarmMenu(windowId, inv, this);
	}

	public IFarmLedgerDelegate getFarmLedgerDelegate() {
		return this.manager.getHydrationManager();
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
		return this.manager.getTankManager();
	}

	@Nullable
	public IFluidHandler getFluidHandler(@Nullable Direction facing) {
		return getTankManager();
	}

	public abstract List<ItemStack> createGermlingStacks();

	public abstract List<ItemStack> createResourceStacks();

	public abstract List<ItemStack> createProductionStacks();

	@Override
	public BlockPos getFarmCorner(Direction direction) {
		return this.worldPosition.below(2);
	}

	@Override
	public int getExtents(Direction direction, BlockPos pos) {
		return this.manager.getExtents(direction, pos);
	}

	@Override
	public void setExtents(Direction direction, BlockPos pos, int extend) {
        this.manager.setExtents(direction, pos, extend);
	}

	@Override
	public void cleanExtents(Direction direction) {
        this.manager.cleanExtents(direction);
	}
}
