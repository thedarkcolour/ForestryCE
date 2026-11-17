package forestry.core.content.energy.tiles;

import forestry.api.agriculture.HorizontalDirection;
import forestry.api.core.ForestryError;
import forestry.core.content.energy.blocks.SolarPanelBlock;
import forestry.core.content.energy.features.EnergyBlocks;
import forestry.core.content.energy.features.EnergyBlockEntities;
import forestry.core.content.energy.menu.SolarEngineMenu;
import forestry.core.platform.config.Constants;
import forestry.core.platform.config.ForestryConfig;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class SolarEngineBlockEntity extends EngineBlockEntity {
	// level.getSkyDarken() must be less than 7 for efficiency to be nonzero
	public static final int MAX_SKY_DARKEN = 7;
	// panels must be within 16 blocks of the engine (square range)
	public static final int MAX_PANEL_RANGE = 16;

	// number of panels checked per tick during a scan
	private static final int PANELS_PER_TICK = 5;
	private static final int EMPTY_SCAN_INTERVAL = 20;

	// panels counted by the last completed rescan pass
	private final ArrayList<BlockPos> array = new ArrayList<>();

	@VisibleForTesting
	public int activePanels;
	// fractional part of generated energy from previous ticks
	// ex. if we generate 2.5 FE, 2 is added to the energy buffer and 0.5 FE is kept here, rolling over to next tick
	private double energyRemainder;

	// positions the pass in progress has yet to check, empty between passes
	private final ArrayDeque<BlockPos> pendingRescan = new ArrayDeque<>();
	// panels owned when the current scan began
	private final LongOpenHashSet oldPanels = new LongOpenHashSet();
	// panels the current scan has discovered
	private final ArrayList<BlockPos> newPanels = new ArrayList<>();
	// number of active (exposed to daylight) panels the current scan has discovered
	private int newActivePanels;
	// positions already seen this pass, so the scan never rechecks a position twice (avoids duplicates in newPanels)
	private final LongOpenHashSet seen = new LongOpenHashSet();

	// only sent to client for displaying in GUI
	private double clientOutputRate;
	private int clientActiveCount;
	private int clientTotalCount;
	private int clientSkyDarken;

	public SolarEngineBlockEntity(BlockPos pos, BlockState state) {
		super(EnergyBlockEntities.SOLAR_ENGINE.tileType(), pos, state, "engine.tin", Constants.ENGINE_COPPER_HEAT_MAX, 10000);
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		super.serverTick(level, pos, state);

		getErrorLogic().setCondition(this.array.isEmpty(), ForestryError.NO_SOLAR_PANELS);
		getErrorLogic().setCondition(!this.array.isEmpty() && insolation(level) <= 0.0, ForestryError.NO_SUNLIGHT);

		rescanPanels(level, pos);
	}

	private void rescanPanels(Level level, BlockPos pos) {
		if (this.pendingRescan.isEmpty() && !beginScan(pos)) {
			return;
		}
		for (int i = 0; i < PANELS_PER_TICK && !this.pendingRescan.isEmpty(); i++) {
			scanNextPanel(level);
		}
		if (this.pendingRescan.isEmpty()) {
			finishScan(level);
		}
	}

	private boolean beginScan(BlockPos pos) {
		// empty panels attempt to scan less often
		if (this.array.isEmpty() && !updateOnInterval(EMPTY_SCAN_INTERVAL)) {
			return false;
		}

		// start scan
		this.seen.clear();
		this.oldPanels.clear();
		this.newPanels.clear();
		this.newActivePanels = 0;
		for (BlockPos panel : this.array) {
			this.oldPanels.add(panel.asLong());
		}
		BlockPos seed = pos.above();
		this.seen.add(seed.asLong());
		this.pendingRescan.addLast(seed);
		return true;
	}

	private void scanNextPanel(Level level) {
		BlockPos nextPos = this.pendingRescan.pollFirst();
		boolean wasOwned = this.oldPanels.contains(nextPos.asLong());

		// an unloaded panel keeps its claim and keeps its neighbors reachable, but never counts as active
		if (!level.hasChunkAt(nextPos)) {
			if (wasOwned) {
				this.newPanels.add(nextPos);
				addNeighborsToScan(nextPos);
			}
			return;
		}

		// update CONNECTED state
		BlockState state = level.getBlockState(nextPos);
		if (!state.is(EnergyBlocks.SOLAR_PANEL.block())) {
			return;
		}
		if (state.getValue(SolarPanelBlock.CONNECTED)) {
			// skip connected panels not owned by this engine
			if (!wasOwned) {
				return;
			}
		} else {
			// if unconnected, connect the panel to this engine
			state = state.setValue(SolarPanelBlock.CONNECTED, true);
			level.setBlock(nextPos, state, Block.UPDATE_CLIENTS);
		}

		// update IN_DAYLIGHT state
		boolean canSeeSky = level.canSeeSky(nextPos);
		if (state.getValue(SolarPanelBlock.IN_DAYLIGHT) != canSeeSky) {
			// skip neighbor update but still sync to client (might want to make server-only instead)
			level.setBlock(nextPos, state.setValue(SolarPanelBlock.IN_DAYLIGHT, canSeeSky), Block.UPDATE_CLIENTS);
		}
		if (canSeeSky) {
			this.newActivePanels++;
		}

		this.newPanels.add(nextPos);
		addNeighborsToScan(nextPos);
	}

	private void addNeighborsToScan(BlockPos panelPos) {
		for (Direction dir : HorizontalDirection.VALUES) {
			BlockPos next = panelPos.relative(dir);
			if (inRange(next) && this.seen.add(next.asLong())) {
				this.pendingRescan.addLast(next);
			}
		}
	}

	private boolean inRange(BlockPos pos) {
		return Math.abs(pos.getX() - this.worldPosition.getX()) <= MAX_PANEL_RANGE && Math.abs(pos.getZ() - this.worldPosition.getZ()) <= MAX_PANEL_RANGE;
	}

	private void finishScan(Level level) {
		// use set for fast contains
		LongOpenHashSet newPanels = new LongOpenHashSet(this.newPanels.size());
		for (BlockPos panel : this.newPanels) {
			newPanels.add(panel.asLong());
		}

		// update disconnected panels from the old array
		for (BlockPos old : this.array) {
			if (newPanels.contains(old.asLong())) {
				continue;
			}
			// ignore unloaded panels
			if (!level.hasChunkAt(old)) {
				// never drop a claim that cannot also be cleared, or the panel is orphaned for good
				newPanels.add(old.asLong());
				this.newPanels.add(old);
				continue;
			}
			BlockState state = level.getBlockState(old);
			if (state.is(EnergyBlocks.SOLAR_PANEL.block()) && state.getValue(SolarPanelBlock.CONNECTED)) {
				level.setBlock(old, state.setValue(SolarPanelBlock.CONNECTED, false), Block.UPDATE_CLIENTS);
			}
		}

		// replace current panels array with (deduped) scan results and mark as dirty if needed
		boolean changed = this.activePanels != this.newActivePanels || !this.array.equals(this.newPanels);
		this.array.clear();
		this.array.addAll(this.newPanels);
		this.activePanels = this.newActivePanels;
		if (changed) {
			setChanged();
		}
	}

	@Override
	public void onDropContents(ServerLevel level) {
		this.array.forEach(pos -> {
			BlockState state = level.getBlockState(pos);
			if (state.getBlock() == EnergyBlocks.SOLAR_PANEL.block())
				level.setBlock(pos, state.setValue(SolarPanelBlock.CONNECTED, false), 3);
		});
	}

	@Override
	protected void dissipateHeat() {
	}

	@Override
	protected void generateHeat() {
		if (this.activePanels >= 1024) {
			this.heat = 9000;
		} else if (this.activePanels >= 256) {
			this.heat = 8000;
		} else if (this.activePanels >= 64) {
			this.heat = 6000;
		} else if (this.activePanels >= 16) {
			this.heat = 4000;
		} else {
			this.heat = 0;
		}
	}

	@Override
	protected void burn() {
		if (!isRedstoneActivated()) {
			this.currentOutput = 0;
			this.clientOutputRate = 0.0;
			return;
		}
		double currentOutput = calculateOutput(this.level, this.activePanels);
		if (currentOutput <= 0.0) {
			this.currentOutput = 0;
			this.clientOutputRate = 0.0;
			return;
		} else {
			this.clientOutputRate = currentOutput;
		}

		// add float output to remainder (which can have energy from previous ticks)
		this.energyRemainder += currentOutput;
		this.currentOutput = (int) this.energyRemainder;

		this.energyStorage.generateEnergy(this.currentOutput);

		// subtract the integer component of what was generated, leaving the fractional remainder
		this.energyRemainder -= this.currentOutput;
	}

	private static boolean isTwilightForest(Level level) {
		// todo use constant instead of calling toString each tick
		return level.dimension().location().toString().equals("twilightforest:twilight_forest");
	}

	/**
	 * Used to get the fraction of a panel's rated output that the current sky delivers.
	 *
	 * @param level The level the engine is in
	 * @return Insolation as a fraction between 0 and 1
	 */
	public static double insolation(Level level) {
		return insolation(level, level.getSkyDarken());
	}

	/**
	 * Used to get the fraction of a panel's rated output that a given sky darkening level delivers.
	 *
	 * @param level     The level the engine is in
	 * @param skyDarken The sky darkening level to evaluate
	 * @return Insolation as a fraction between 0 and 1
	 */
	public static double insolation(Level level, int skyDarken) {
		// todo replace with config map
		if (isTwilightForest(level)) {
			return 1.0;
		}
		return skyDarken > MAX_SKY_DARKEN ? 0.0 : Math.pow(2.0, -skyDarken);
	}

	/**
	 * Used to get the rated output of a single panel, before the array size bonus and insolation.
	 *
	 * @param level The level the engine is in
	 * @return FE/t produced by one panel
	 */
	public static double panelOutput(Level level) {
		// todo replace with config map
		return isTwilightForest(level) ? ForestryConfig.SERVER.twilightSolarFE.get() : ForestryConfig.SERVER.solarFE.get();
	}

	/**
	 * Used to get the bonus FE/t that the size of an array adds on top of its flat output.
	 * Uses bluffcon's formula, so the bonus grows with the square of the array.
	 *
	 * @param panels The number of active and connected solar panels
	 * @return Bonus FE produced per tick, before insolation
	 */
	public static double calculateArraySizeBonus(int panels) {
		return panels <= 0 ? 0.0 : ForestryConfig.SERVER.solarArrayBonusFactor.get() * Math.pow(panels - 1, 2);
	}

	/**
	 * Used to get the multiplier that the array size bonus applies to the flat per-panel output.
	 * Insolation scales the flat output and the bonus equally, so it cancels out and does not appear here.
	 *
	 * @param level  The level the engine is in
	 * @param panels The number of active and connected solar panels
	 * @return Multiplier of 1 or more, or 0 when there are no active panels
	 */
	public static double calculateMult(Level level, int panels) {
		if (panels <= 0) {
			return 0.0;
		}
		double base = panels * panelOutput(level);
		return base <= 0.0 ? 0.0 : (base + calculateArraySizeBonus(panels)) / base;
	}

	/**
	 * Used to get the total energy an engine generates per tick, including the array size bonus
	 * and the current insolation.
	 *
	 * @param level  The level the engine is in
	 * @param panels The number of active and connected solar panels
	 * @return FE produced per tick, as a decimal amount
	 */
	public static double calculateOutput(Level level, int panels) {
		if (panels <= 0) {
			return 0.0;
		}
		return (panels * panelOutput(level) + calculateArraySizeBonus(panels)) * insolation(level);
	}

	@Override
	protected boolean isBurning() {
		return mayBurn();
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new SolarEngineMenu(containerId, playerInventory, this);
	}

	// Deviation from 1.20.1: 1.21.1 block entities save through saveAdditional(CompoundTag, HolderLookup.Provider)
	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("active", this.activePanels);

		// save array as longs
		long[] positions = new long[this.array.size()];
		int i = 0;
		for (BlockPos pos : this.array) {
			positions[i++] = pos.asLong();
		}
		nbt.putLongArray("array", positions);
	}

	// Deviation from 1.20.1: load(CompoundTag) became loadAdditional(CompoundTag, HolderLookup.Provider)
	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.activePanels = nbt.getInt("active");
		this.array.clear();
		for (long pos : nbt.getLongArray("array")) {
			this.array.add(BlockPos.of(pos));
		}

		// restart in-progress scans with new array
		this.pendingRescan.clear();
		this.seen.clear();
		this.oldPanels.clear();
		this.newPanels.clear();
		this.newActivePanels = 0;
	}

	@Override
	public void writeGuiData(RegistryFriendlyByteBuf data) {
		super.writeGuiData(data);
		data.writeVarInt(this.activePanels);
		data.writeVarInt(this.array.size());
		data.writeVarInt(this.level.getSkyDarken());
		data.writeFloat((float) this.clientOutputRate);
	}

	@Override
	public void readGuiData(RegistryFriendlyByteBuf data) {
		super.readGuiData(data);
		this.clientActiveCount = data.readVarInt();
		this.clientTotalCount = data.readVarInt();
		this.clientSkyDarken = data.readVarInt();
		this.clientOutputRate = data.readFloat();
	}

	@Override
	public double getCurrentOutputRate() {
		return canOutput() ? this.clientOutputRate : 0.0;
	}

	public int getClientActivePanelCount() {
		return this.clientActiveCount;
	}

	public int getClientPanelCount() {
		return this.clientTotalCount;
	}

	public int getClientSkyDarken() {
		return this.clientSkyDarken;
	}
}
