package forestry.apiculture.tiles;

import com.mojang.authlib.GameProfile;
import forestry.api.IForestryApi;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.climate.IClimateProvider;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.apiculture.gui.IGuiBeeHousingDelegate;
import forestry.core.network.IStreamableGui;
import forestry.core.owner.IOwnedTile;
import forestry.core.owner.IOwnerHandler;
import forestry.core.owner.OwnerHandler;
import forestry.core.render.ParticleRender;
import forestry.core.tiles.TileBase;
import forestry.core.utils.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TileBeeHousingBase extends TileBase implements IBeeHousing, IOwnedTile, IClimateProvider, IGuiBeeHousingDelegate, IStreamableGui {
	private final String hintKey;
	private final OwnerHandler ownerHandler = new OwnerHandler();
	private final IBeekeepingLogic beeLogic;
	protected IClimateProvider climate = IForestryApi.INSTANCE.getClimateManager().createDummyClimateProvider();

	// CLIENT
	private int breedingProgressPercent = 0;

	protected TileBeeHousingBase(BlockEntityType<?> type, BlockPos pos, BlockState state, String hintKey) {
		super(type, pos, state);
		this.hintKey = hintKey;
		this.beeLogic = IForestryApi.INSTANCE.getHiveManager().createBeekeepingLogic(this);
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		this.climate = IForestryApi.INSTANCE.getClimateManager().createClimateProvider(level, this.worldPosition);
	}

	@Override
	public String getHintKey() {
		return this.hintKey;
	}

	@Override
	public IBeekeepingLogic getBeekeepingLogic() {
		return this.beeLogic;
	}

	/* LOADING & SAVING */
	@Override
	public void saveAdditional(CompoundTag compoundNBT) {
		super.saveAdditional(compoundNBT);
        this.beeLogic.write(compoundNBT);
        this.ownerHandler.write(compoundNBT);
	}

	@Override
	public void load(CompoundTag compoundNBT) {
		super.load(compoundNBT);
        this.beeLogic.read(compoundNBT);
        this.ownerHandler.read(compoundNBT);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag updateTag = super.getUpdateTag();
        this.beeLogic.write(updateTag);
        this.ownerHandler.write(updateTag);
		return updateTag;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
        this.beeLogic.read(tag);
        this.ownerHandler.read(tag);
	}

	@Override
	public IOwnerHandler getOwnerHandler() {
		return this.ownerHandler;
	}

	/* ICLIMATISED */
	@Override
	public TemperatureType temperature() {
		return this.climate.temperature();
	}

	@Override
	public HumidityType humidity() {
		return this.climate.humidity();
	}

	/* UPDATING */
	@Override
	public void clientTick(Level level, BlockPos pos, BlockState state) {
		if (this.beeLogic.canDoBeeFX() && updateOnInterval(4)) {
            this.beeLogic.doBeeFX();

			if (updateOnInterval(50)) {
				doPollenFX(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void doPollenFX(Level world, double xCoord, double yCoord, double zCoord) {
		double fxX = xCoord + 0.5F;
		double fxY = yCoord + 0.25F;
		double fxZ = zCoord + 0.5F;
		float distanceFromCenter = 0.6F;
		float leftRightSpreadFromCenter = distanceFromCenter * (world.random.nextFloat() - 0.5F);
		float upSpread = world.random.nextFloat() * 6F / 16F;
		fxY += upSpread;

		ParticleRender.addEntityHoneyDustFX(world, fxX - distanceFromCenter, fxY, fxZ + leftRightSpreadFromCenter);
		ParticleRender.addEntityHoneyDustFX(world, fxX + distanceFromCenter, fxY, fxZ + leftRightSpreadFromCenter);
		ParticleRender.addEntityHoneyDustFX(world, fxX + leftRightSpreadFromCenter, fxY, fxZ - distanceFromCenter);
		ParticleRender.addEntityHoneyDustFX(world, fxX + leftRightSpreadFromCenter, fxY, fxZ + distanceFromCenter);
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		if (this.beeLogic.canWork()) {
            this.beeLogic.doWork();
		}

		// every 64 ticks, update the climate state in case of changed biome or climate (& is faster than modulus)
		if ((level.getGameTime() & 63L) == 0L) {
			this.climate = IForestryApi.INSTANCE.getClimateManager().createClimateProvider(level, pos);
		}
	}

	@Override
	public int getHealthScaled(int i) {
		return this.breedingProgressPercent * i / 100;
	}

	@Override
	public void writeGuiData(FriendlyByteBuf data) {
		data.writeVarInt(this.beeLogic.getBeeProgressPercent());
		NetworkUtil.writeClimateState(data, this.climate);
	}

	@Override
	public void readGuiData(FriendlyByteBuf data) {
        this.breedingProgressPercent = data.readVarInt();
		this.climate = NetworkUtil.readClimateState(data);
	}

	// / IBEEHOUSING
	@Override
	public Holder<Biome> getBiome() {
		return this.level.getBiome(getBlockPos());
	}

	//TODO check this call
	@Override
	public int getBlockLightValue() {
		return this.level.getMaxLocalRawBrightness(getBlockPos().above());
	}

	@Override
	public boolean canBlockSeeTheSky() {
		return this.level.canSeeSky(getBlockPos().above());
	}

	@Override
	public boolean isRaining() {
		return this.level.isRainingAt(getBlockPos().above());
	}

	@Override
	public GameProfile getOwner() {
		return getOwnerHandler().getOwner();
	}

	@Override
	public Vec3 getBeeFXCoordinates() {
		return new Vec3(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5);
	}
}
