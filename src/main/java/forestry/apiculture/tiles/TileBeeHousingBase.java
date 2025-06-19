package forestry.apiculture.tiles;

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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

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

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.beeLogic.write(nbt, registries);
		this.ownerHandler.write(nbt, registries);
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.beeLogic.read(nbt, registries);
		this.ownerHandler.read(nbt, registries);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag updateTag = super.getUpdateTag(registries);
		this.beeLogic.write(updateTag, registries);
		this.ownerHandler.write(updateTag, registries);
		return updateTag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		super.handleUpdateTag(tag, registries);
		this.beeLogic.read(tag, registries);
		this.ownerHandler.read(tag, registries);
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

	public static void doPollenFX(Level level, double xCoord, double yCoord, double zCoord) {
		double fxX = xCoord + 0.5F;
		double fxY = yCoord + 0.25F;
		double fxZ = zCoord + 0.5F;
		float distanceFromCenter = 0.6F;
		float leftRightSpreadFromCenter = distanceFromCenter * (level.random.nextFloat() - 0.5F);
		float upSpread = level.random.nextFloat() * 6F / 16F;
		fxY += upSpread;

		ParticleRender.addEntityHoneyDustFX(level, fxX - distanceFromCenter, fxY, fxZ + leftRightSpreadFromCenter);
		ParticleRender.addEntityHoneyDustFX(level, fxX + distanceFromCenter, fxY, fxZ + leftRightSpreadFromCenter);
		ParticleRender.addEntityHoneyDustFX(level, fxX + leftRightSpreadFromCenter, fxY, fxZ - distanceFromCenter);
		ParticleRender.addEntityHoneyDustFX(level, fxX + leftRightSpreadFromCenter, fxY, fxZ + distanceFromCenter);
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
	public void writeGuiData(RegistryFriendlyByteBuf buffer) {
		buffer.writeVarInt(this.beeLogic.getBeeProgressPercent());
		NetworkUtil.writeClimateState(buffer, this.climate);
	}

	@Override
	public void readGuiData(RegistryFriendlyByteBuf buffer) {
		this.breedingProgressPercent = buffer.readVarInt();
		this.climate = NetworkUtil.readClimateState(buffer);
	}

	// / IBEEHOUSING
	@Override
	public Holder<Biome> getBiome(HolderLookup.Provider registries) {
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
	public @Nullable ResolvableProfile getOwner() {
		return getOwnerHandler().getOwner();
	}

	@Override
	public Vec3 getBeeFXCoordinates() {
		return new Vec3(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5);
	}
}
