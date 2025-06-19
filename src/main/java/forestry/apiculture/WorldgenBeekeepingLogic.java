package forestry.apiculture;

import forestry.api.apiculture.bee.IActivityType;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.apiculture.bee.IBee;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.modules.IForestryPacketClient;
import forestry.api.util.TickHelper;
import forestry.apiculture.network.packets.PacketBeeLogicActive;
import forestry.apiculture.tiles.TileHive;
import forestry.core.utils.NetworkUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class WorldgenBeekeepingLogic implements IBeekeepingLogic {
	private final TileHive housing;
	private final IEffectData[] effectData = new IEffectData[2];
	private final HasFlowersCache hasFlowersCache = new HasFlowersCache(2);
	private final TickHelper tickHelper;

	// Client
	private boolean active;

	public WorldgenBeekeepingLogic(TileHive housing) {
		this.housing = housing;
		this.tickHelper = new TickHelper(housing.getBlockPos().hashCode());
	}

	// / SAVING & LOADING
	@Override
	public void read(CompoundTag CompoundNBT, HolderLookup.Provider registries) {
		setActive(CompoundNBT.getBoolean("Active"));
        this.hasFlowersCache.read(CompoundNBT, registries);
	}

	@Override
	public CompoundTag write(CompoundTag CompoundNBT, HolderLookup.Provider registries) {
		CompoundNBT.putBoolean("Active", this.active);
        this.hasFlowersCache.write(CompoundNBT, registries);

		return CompoundNBT;
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf buffer) {
		buffer.writeBoolean(this.active);
		if (this.active) {
            this.hasFlowersCache.writeData(buffer);
		}
	}

	@Override
	public void readData(RegistryFriendlyByteBuf buffer) {
		boolean active = buffer.readBoolean();
		setActive(active);
		if (active) {
            this.hasFlowersCache.readData(buffer);
		}
	}

	/* Activatable */
	private void setActive(boolean active) {
		if (this.active == active) {
			return;
		}
		this.active = active;

		syncToClient();
	}

	/* UPDATING */

	@Override
	public boolean canWork() {
        this.tickHelper.onTick();

		if (this.tickHelper.updateOnInterval(200)) {
			IBee queen = this.housing.getContainedBee();
            this.hasFlowersCache.update(queen, this.housing);
			Level level = this.housing.getLevel();
			IGenome genome = queen.getGenome();
			boolean canWork = genome.getActiveValue(BeeChromosomes.ACTIVITY).isActive(level.getGameTime(), IActivityType.getBeeDayTime(level), this.housing.getBlockPos()) &&
				(!this.housing.isRaining() || genome.getActiveValue(BeeChromosomes.TOLERATES_RAIN));
			boolean flowerCacheNeedsSync = this.hasFlowersCache.needsSync();

			if (this.active != canWork) {
				setActive(canWork);
			} else if (flowerCacheNeedsSync) {
				syncToClient();
			}
		}

		return this.active;
	}

	@Override
	public void doWork() {

	}

	@Override
	public void clearCachedValues() {

	}

	/* CLIENT */

	@Override
	public void syncToClient() {
		if (this.housing.getLevel() instanceof ServerLevel level) {
			NetworkUtil.sendToPlayersTrackingPos(new PacketBeeLogicActive(this.housing), this.housing.getBlockPos(), level);
		}
	}

	@Override
	public void syncToClient(ServerPlayer player) {
		Level world = this.housing.getLevel();
		if (world != null && !world.isClientSide) {
			IForestryPacketClient packet = new PacketBeeLogicActive(this.housing);
			PacketDistributor.sendToPlayer(player, packet);
		}
	}

	@Override
	public int getBeeProgressPercent() {
		return 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canDoBeeFX() {
		return !Minecraft.getInstance().isPaused() && this.active;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void doBeeFX() {
		IBee queen = this.housing.getContainedBee();
		queen.doFX(this.effectData, this.housing);
	}

	@Override
	public List<BlockPos> getFlowerPositions() {
		return this.hasFlowersCache.getFlowerCoords();
	}

}
