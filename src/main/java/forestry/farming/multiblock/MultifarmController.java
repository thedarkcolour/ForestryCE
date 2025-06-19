package forestry.farming.multiblock;

import com.google.common.base.MoreObjects;
import forestry.api.ForestryTags;
import forestry.api.IForestryApi;
import forestry.api.circuits.ForestryCircuitSocketTypes;
import forestry.api.circuits.ICircuitBoard;
import forestry.api.core.ForestryError;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.farming.ForestryFarmTypes;
import forestry.api.farming.HorizontalDirection;
import forestry.api.farming.IFarmLogic;
import forestry.api.farming.IFarmable;
import forestry.api.multiblock.IFarmComponent;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.core.config.ForestryConfig;
import forestry.core.features.CoreDataComponents;
import forestry.core.fluids.TankManager;
import forestry.core.inventory.FakeInventoryAdapter;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.inventory.InventoryAdapter;
import forestry.core.multiblock.IMultiblockControllerInternal;
import forestry.core.multiblock.MultiblockSizeLimits;
import forestry.core.multiblock.MultiblockValidationException;
import forestry.core.multiblock.RectangularMultiblockControllerBase;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.utils.PlayerUtil;
import forestry.farming.FarmHelper;
import forestry.farming.FarmManager;
import forestry.farming.FarmTarget;
import forestry.farming.gui.IFarmLedgerDelegate;
import forestry.farming.tiles.MultifarmGearboxBlockEntity;
import forestry.farming.tiles.MultifarmBlockEntity;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.*;

public class MultifarmController extends RectangularMultiblockControllerBase implements IFarmControllerInternal, ILiquidTankTile {
	private static final MultiblockSizeLimits FARM_LIMITS = new MultiblockSizeLimits(3 * 3 * 4, 3, 4, 3, 5, 4, 5);

	// active components are stored with a tick offset so they do not all tick together
	private final Map<IFarmComponent.Active, Integer> farmActiveComponents = new HashMap<>();

	private final Map<Direction, IFarmLogic> farmLogics = new EnumMap<>(Direction.class);

	private final InventoryAdapter sockets;
	private final MultifarmInventory inventory;
	private final FarmManager manager;

	// the number of work ticks that this farm has had no power
	private int noPowerTime = 0;
	private int allowedExtent = 0;
	@Nullable
	private Vec3i offset;
	@Nullable
	private Vec3i area;

	public MultifarmController(Level world) {
		super(world, FARM_LIMITS);

		this.inventory = new MultifarmInventory(this);
		this.manager = new FarmManager(this);
		this.sockets = new InventoryAdapter(1, "sockets");

		refreshFarmLogics();
	}

	@Override
	public IFarmLedgerDelegate getFarmLedgerDelegate() {
		return this.manager.getHydrationManager();
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		if (isAssembled()) {
			return this.inventory;
		} else {
			return FakeInventoryAdapter.INSTANCE;
		}
	}

	@Override
	public TankManager getTankManager() {
		return this.manager.getTankManager();
	}

	@Override
	public void onAttachedPartWithMultiblockData(IMultiblockComponent part, CompoundTag data) {
		read(data, this.level.registryAccess());
	}

	@Override
	protected void onBlockAdded(IMultiblockComponent newPart) {
		if (newPart instanceof IFarmComponent.Listener listenerPart) {
			this.manager.addListener(listenerPart);
		}

		if (newPart instanceof IFarmComponent.Active active) {
			this.farmActiveComponents.put(active, this.level.random.nextInt(256));
		}
	}

	@Override
	protected void onBlockRemoved(IMultiblockComponent oldPart) {
		if (oldPart instanceof IFarmComponent.Listener listenerPart) {
			this.manager.removeListener(listenerPart);
		}

		if (oldPart instanceof IFarmComponent.Active) {
			this.farmActiveComponents.remove(oldPart);
		}
	}

	@Override
	protected void isMachineWhole() throws MultiblockValidationException {
		super.isMachineWhole();

		boolean hasGearbox = false;
		for (IMultiblockComponent part : this.connectedParts) {
			if (part instanceof MultifarmGearboxBlockEntity) {
				hasGearbox = true;
				break;
			}
		}

		if (!hasGearbox) {
			throw new MultiblockValidationException(Component.translatable("for.multiblock.farm.error.needGearbox").getString());
		}
	}

	@Override
	protected void onMachineDisassembled() {
		super.onMachineDisassembled();
		this.manager.clearTargets();
	}

	@Override
	public void onDestroyed(BlockPos lastPos) {
		Containers.dropContents(this.level, lastPos, this.inventory);
		Containers.dropContents(this.level, lastPos, this.sockets);
	}

	@Override
	public void isGoodForExteriorLevel(IMultiblockComponent part, int level) throws MultiblockValidationException {
		if (level == 2 && !(part instanceof MultifarmBlockEntity)) {
			throw new MultiblockValidationException(Component.translatable("for.multiblock.farm.error.needPlainBand").getString());
		}
	}

	@Override
	public void isGoodForInterior(IMultiblockComponent part) throws MultiblockValidationException {
		if (!(part instanceof MultifarmBlockEntity)) {
			throw new MultiblockValidationException(Component.translatable("for.multiblock.farm.error.needPlainInterior").getString());
		}
	}

	@Override
	public void onAssimilate(IMultiblockControllerInternal assimilated) {

	}

	@Override
	public void onAssimilated(IMultiblockControllerInternal assimilator) {

	}

	@Override
	protected boolean serverTick(int tickCount) {
		this.manager.getHydrationManager().updateServer();

		if (updateOnInterval(20)) {
			this.inventory.drainCan(this.manager.getTankManager());
		}

		boolean hasPower = false;
		for (Map.Entry<IFarmComponent.Active, Integer> entry : this.farmActiveComponents.entrySet()) {
			IFarmComponent.Active farmComponent = entry.getKey();
			if (farmComponent instanceof MultifarmGearboxBlockEntity gearbox) {
				hasPower |= gearbox.getEnergyManager().getEnergyStored() > 0;
			}

			int tickOffset = entry.getValue();
			farmComponent.updateServer(tickCount + tickOffset);
		}

		if (hasPower) {
			this.noPowerTime = 0;
			getErrorLogic().setCondition(false, ForestryError.NO_POWER);
		} else {
			if (this.noPowerTime <= 4) {
				this.noPowerTime++;
			} else {
				getErrorLogic().setCondition(true, ForestryError.NO_POWER);
			}
		}

		//FIXME: be smarter about the farm needing to save
		return true;
	}

	@Override
	protected void clientTick(int tickCount) {
		for (Map.Entry<IFarmComponent.Active, Integer> entry : this.farmActiveComponents.entrySet()) {
			IFarmComponent.Active farmComponent = entry.getKey();
			int tickOffset = entry.getValue();
			farmComponent.updateClient(tickCount + tickOffset);
		}
	}

	@Override
	public CompoundTag write(CompoundTag data, HolderLookup.Provider registries) {
		data = super.write(data, registries);
		this.sockets.write(data, registries);
		this.manager.write(data, registries);
		this.inventory.write(data, registries);
		return data;
	}

	@Override
	public void read(CompoundTag data, HolderLookup.Provider registries) {
		super.read(data, registries);
		this.sockets.read(data, registries);
		this.manager.read(data, registries);
		this.inventory.read(data, registries);

		refreshFarmLogics();
	}

	@Override
	public void encodeUpdatePacket(CompoundTag data, HolderLookup.Provider registries) {
		this.sockets.write(data, registries);
		this.manager.write(data, registries);
	}

	@Override
	public void decodeUpdatePacket(CompoundTag data, HolderLookup.Provider registries) {
		this.sockets.read(data, registries);
		this.manager.read(data, registries);

		refreshFarmLogics();
	}

	@Override
	public BlockPos getBlockPos() {
		return getReferenceCoord();
	}

	@Override
	public BlockPos getTopCoord() {
		return getTopCenterCoord();
	}

	@Override
	public void writeGuiData(RegistryFriendlyByteBuf buffer) {
		this.manager.writeData(buffer);
		this.sockets.writeData(buffer);
	}

	@Override
	public void readGuiData(RegistryFriendlyByteBuf buffer) {
		this.manager.readData(buffer);
		this.sockets.readData(buffer);

		refreshFarmLogics();
	}

	private void refreshFarmLogics() {
		for (Direction direction : HorizontalDirection.VALUES) {
			resetFarmLogic(direction);
		}

		// See whether we have socketed stuff.
		ItemStack chip = this.sockets.getItem(0);
		if (!chip.isEmpty()) {
			IForestryApi.INSTANCE.getCircuitManager();
			ICircuitBoard chipset = chip.get(CoreDataComponents.CIRCUIT_BOARD);
			if (chipset != null) {
				chipset.onLoad(this);
			}
		}
	}

	@Override
	public TemperatureType temperature() {
		return IForestryApi.INSTANCE.getClimateManager().getTemperature(getBiome());
	}

	@Override
	public HumidityType humidity() {
		return IForestryApi.INSTANCE.getClimateManager().getHumidity(getBiome());
	}

	protected Holder<Biome> getBiome() {
		BlockPos coords = getReferenceCoord();
		if (coords == null) {
			return this.level.registryAccess().holderOrThrow(Biomes.PLAINS);
		}
		return this.level.getBiome(coords);
	}

	@Override
	public BlockPos getCoords() {
		return getCenterCoord();
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
	public Vec3i getArea() {
		if (this.area == null) {
			this.area = new Vec3i(7 + this.allowedExtent * 2, 13, 7 + this.allowedExtent * 2);
		}
		return this.area;
	}

	@Override
	public String getUnlocalizedType() {
		return "for.multiblock.farm.type";
	}

	@Override
	public boolean doWork() {
		return this.manager.doWork();
	}

	@Override
	public void setUpFarmlandTargets(Map<Direction, List<FarmTarget>> targets) {
		BlockPos targetStart = getCoords();

		BlockPos max = getMaximumCoord();
		BlockPos min = getMinimumCoord();

		int sizeNorthSouth = Math.abs(max.getZ() - min.getZ()) + 1;
		int sizeEastWest = Math.abs(max.getX() - min.getX()) + 1;

		// Set the maximum allowed extent.
		this.allowedExtent = Math.max(sizeNorthSouth, sizeEastWest) * ForestryConfig.SERVER.multiFarmSize.get() + 1;

		FarmHelper.createTargets(this.level, this, targets, targetStart, this.allowedExtent, sizeNorthSouth, sizeEastWest, min, max);
		FarmHelper.setExtents(this.level, this, targets);
	}

	@Override
	public int getStoredFertilizerScaled(int scale) {
		return this.manager.getFertilizerManager().getStoredFertilizerScaled(this.inventory, scale);
	}

	@Override
	public BlockPos getFarmCorner(Direction direction) {
		return this.manager.getFarmCorner(direction);
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
	public boolean plantGermling(IFarmable germling, Level world, BlockPos pos, Direction direction) {
		Player player = PlayerUtil.getFakePlayer(world, getOwnerHandler().getOwner());
		return player != null && this.inventory.plantGermling(germling, player, pos);
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
	public void setFarmLogic(Direction direction, IFarmLogic logic) {
		this.farmLogics.put(direction, logic);
		cleanExtents(direction);
	}

	@Override
	public void resetFarmLogic(Direction direction) {
		setFarmLogic(direction, IForestryApi.INSTANCE.getFarmingManager().getFarmType(ForestryFarmTypes.ARBOREAL).getLogic(false));
	}

	@Override
	public IFarmLogic getFarmLogic(Direction direction) {
		return this.farmLogics.get(direction);
	}

	@Override
	public Collection<IFarmLogic> getFarmLogics() {
		return this.farmLogics.values();
	}

	@Override
	public int getSocketCount() {
		return this.sockets.getContainerSize();
	}

	@Override
	public ItemStack getSocket(int slot) {
		return this.sockets.getItem(slot);
	}

	@Override
	public void setSocket(int slot, ItemStack stack) {
		if (IForestryApi.INSTANCE.getCircuitManager().isCircuitBoard(stack) || stack.isEmpty()) {
			// Dispose old chipsets correctly
			if (!this.sockets.getItem(slot).isEmpty()) {
				if (IForestryApi.INSTANCE.getCircuitManager().isCircuitBoard(this.sockets.getItem(slot))) {
					IForestryApi.INSTANCE.getCircuitManager();
					ItemStack stack1 = this.sockets.getItem(slot);
					ICircuitBoard chipset = stack1.get(CoreDataComponents.CIRCUIT_BOARD);
					if (chipset != null) {
						chipset.onRemoval(this);
					}
				}
			}

			this.sockets.setItem(slot, stack);
			refreshFarmLogics();

			if (!stack.isEmpty()) {
				IForestryApi.INSTANCE.getCircuitManager();
				ICircuitBoard chipset = stack.get(CoreDataComponents.CIRCUIT_BOARD);
				if (chipset != null) {
					chipset.onInsertion(this);
				}
			}
		}
	}

	@Override
	public ResourceLocation getSocketType() {
		return ForestryCircuitSocketTypes.FARM;
	}

	@Override
	public boolean canPlantSoil(boolean manual) {
		return true;
	}

	@Override
	public boolean isValidPlatform(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		return state.is(ForestryTags.Blocks.VALID_FARM_BASE);
	}

	@Override
	public boolean isSquare() {
		return ForestryConfig.SERVER.squareMultiFarms.get();
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

	// for debugging
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("logic", this.farmLogics.toString()).toString();
	}
}
