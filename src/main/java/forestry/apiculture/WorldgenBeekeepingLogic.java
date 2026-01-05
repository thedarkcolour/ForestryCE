package forestry.apiculture;

import forestry.api.apiculture.IActivityType;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.util.TickHelper;
import forestry.apiculture.network.packets.PacketBeeLogicActive;
import forestry.apiculture.tiles.TileHive;
import forestry.core.utils.NetworkUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
	public void read(CompoundTag CompoundNBT) {
		setActive(CompoundNBT.getBoolean("Active"));
        this.hasFlowersCache.read(CompoundNBT);
	}

	@Override
	public CompoundTag write(CompoundTag CompoundNBT) {
		CompoundNBT.putBoolean("Active", this.active);
        this.hasFlowersCache.write(CompoundNBT);

		return CompoundNBT;
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		data.writeBoolean(this.active);
		if (this.active) {
            this.hasFlowersCache.writeData(data);
		}
	}

	@Override
	public void readData(FriendlyByteBuf data) {
		boolean active = data.readBoolean();
		setActive(active);
		if (active) {
            this.hasFlowersCache.readData(data);
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
			Level level = this.housing.getWorldObj();
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
		Level world = this.housing.getWorldObj();
		if (world != null && !world.isClientSide) {
			NetworkUtil.sendNetworkPacket(new PacketBeeLogicActive(this.housing), this.housing.getCoordinates(), world);
		}
	}

	@Override
	public void syncToClient(ServerPlayer player) {
		Level world = this.housing.getWorldObj();
		if (world != null && !world.isClientSide) {
			NetworkUtil.sendToPlayer(new PacketBeeLogicActive(this.housing), player);
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
