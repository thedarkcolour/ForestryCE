package forestry.apiculture.alveary.multiblock;

import com.mojang.authlib.GameProfile;
import forestry.api.IForestryApi;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.core.climate.IClimateControlled;
import forestry.api.core.climate.IClimateProvider;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.core.genetics.IGenome;
import forestry.api.core.multiblock.IAlvearyComponent;
import forestry.api.core.multiblock.IMultiblockComponent;
import forestry.api.core.multiblock.IMultiblockInventoryProbe;
import forestry.apiculture.bees.BeeHousingInventory;
import forestry.core.platform.inventory.FakeInventoryAdapter;
import forestry.api.core.IInventoryAdapter;
import forestry.core.platform.multiblock.MultiblockController;
import forestry.core.platform.render.ParticleRender;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlvearyController extends MultiblockController implements IAlvearyControllerInternal, IClimateControlled, IMultiblockInventoryProbe {
	private final BeeHousingInventory inventory;
	private final IBeekeepingLogic beekeepingLogic;
	private IClimateProvider climate = IForestryApi.INSTANCE.getClimateManager().createDummyClimateProvider();

	private byte temperatureSteps;
	private byte humiditySteps;

	// PARTS
	private final Set<IBeeModifier> beeModifiers = new HashSet<>();
	private final Set<IBeeListener> beeListeners = new HashSet<>();
	private final Set<IAlvearyComponent.Climatiser> climatisers = new HashSet<>();
	private final Set<IAlvearyComponent.Active> activeComponents = new HashSet<>();

	// CLIENT
	private int breedingProgressPercent = 0;

	public AlvearyController(Level world) {
		super(world);
		// item handlers write here directly without going through any member's setChanged
		this.inventory = new BeeHousingInventory(9) {
			@Override
			public void setChanged() {
				super.setChanged();
				AlvearyController.this.setChanged();
			}
		};
		this.beekeepingLogic = IForestryApi.INSTANCE.getHiveManager().createBeekeepingLogic(this);

		this.beeModifiers.add(new AlvearyBeeModifier());
	}

	@Override
	public IBeeHousingInventory getBeeInventory() {
		return this.inventory;
	}

	@Override
	public List<ItemStack> snapshotSharedInventory() {
		return IMultiblockInventoryProbe.snapshotContainer(this.inventory);
	}

	@Override
	public IBeekeepingLogic getBeekeepingLogic() {
		return this.beekeepingLogic;
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
	public Iterable<IBeeListener> getBeeListeners() {
		return this.beeListeners;
	}

	@Override
	public Iterable<IBeeModifier> getBeeModifiers() {
		return this.beeModifiers;
	}

	/**
	 * Rebuilds the component buckets from the validated member set (spec 8.2; ports the old
	 * {@code onBlockAdded} logic).
	 *
	 * The constructor-seeded {@link AlvearyBeeModifier} is re-added on every rebuild (spec 8.2). It feeds
	 * production and the hellish temperature path.
	 */
	@Override
	protected void bucketComponents() {
		this.beeModifiers.clear();
		this.beeListeners.clear();
		this.climatisers.clear();
		this.activeComponents.clear();

		// Re-seed the constructor modifier on every re-bucket (spec 8.2)
		this.beeModifiers.add(new AlvearyBeeModifier());

		for (BlockPos pos : getMembers()) {
			IMultiblockComponent part = TileUtil.getTile(this.level, pos, IMultiblockComponent.class);
			if (!(part instanceof IAlvearyComponent)) {
				continue;
			}
			if (part instanceof IAlvearyComponent.BeeModifier alvearyBeeModifier) {
				this.beeModifiers.add(alvearyBeeModifier.getBeeModifier());
			}
			if (part instanceof IAlvearyComponent.BeeListener beeListenerSource) {
				this.beeListeners.add(beeListenerSource.getBeeListener());
			}
			if (part instanceof IAlvearyComponent.Climatiser climatiser) {
				this.climatisers.add(climatiser);
			}
			if (part instanceof IAlvearyComponent.Active active) {
				this.activeComponents.add(active);
			}
		}
	}

	@Override
	public void onDestroyed(BlockPos lastPos) {
		Containers.dropContents(this.level, lastPos, this.inventory);
	}

	@Override
	public void onAssembled() {
		this.climate = IForestryApi.INSTANCE.getClimateManager().createClimateProvider(this.level, getCenterCoord());
	}

	@Override
	public void onBroken() {
	}

	@Override
	public boolean serverTick(int tickCount) {
		for (IAlvearyComponent.Active activeComponent : this.activeComponents) {
			activeComponent.updateServer(tickCount);
		}

		final boolean canWork = this.beekeepingLogic.canWork();
		if (canWork) {
            this.beekeepingLogic.doWork();
		}

		// the old equalizeChange would cap out the climate increases from the climate blocks
		this.temperatureSteps = 0;
		this.humiditySteps = 0;
		// climate blocks will increase climate every tick and must go before the canWork check
		for (IAlvearyComponent.Climatiser climatiser : this.climatisers) {
			climatiser.changeClimate(tickCount, this);
		}

		// every 64 ticks, update the climate state in case of changed biome or climate (& is faster than modulus).
		// Uses the staggered tickCount (game time + per-controller phase, MINOR 7) so alvearies refresh climate
		// on different ticks rather than all on the same game-time boundary.
		if ((tickCount & 63) == 0) {
			this.climate = IForestryApi.INSTANCE.getClimateManager().createClimateProvider(this.level, getCenterCoord());
		}

		return canWork;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientTick(int tickCount) {
		for (IAlvearyComponent.Active activeComponent : this.activeComponents) {
			activeComponent.updateClient(tickCount);
		}

		if (this.beekeepingLogic.canDoBeeFX() && updateOnInterval(2, tickCount)) {
            this.beekeepingLogic.doBeeFX();

			if (updateOnInterval(50, tickCount)) {
				BlockPos center = getCenterCoord();
				float fxX = center.getX() + 0.5F;
				float fxY = center.getY() + 1.0F;
				float fxZ = center.getZ() + 0.5F;
				float distanceFromCenter = 1.6F;

				float leftRightSpreadFromCenter = distanceFromCenter * (this.level.random.nextFloat() - 0.5F);
				float upSpread = this.level.random.nextFloat() * 0.8F;
				fxY += upSpread;

				// display fx on all 4 sides
				ParticleRender.addEntityHoneyDustFX(this.level, fxX - distanceFromCenter, fxY, fxZ + leftRightSpreadFromCenter);
				ParticleRender.addEntityHoneyDustFX(this.level, fxX + distanceFromCenter, fxY, fxZ + leftRightSpreadFromCenter);
				ParticleRender.addEntityHoneyDustFX(this.level, fxX + leftRightSpreadFromCenter, fxY, fxZ - distanceFromCenter);
				ParticleRender.addEntityHoneyDustFX(this.level, fxX + leftRightSpreadFromCenter, fxY, fxZ + distanceFromCenter);
			}
		}
	}

	@Override
	public CompoundTag writePayload(CompoundTag data) {
		writeOwner(data);

		data.putByte("temperatureSteps", this.temperatureSteps);
		data.putByte("humiditySteps", this.humiditySteps);

        this.beekeepingLogic.write(data, this.level.registryAccess());
        this.inventory.write(data, this.level.registryAccess());
		return data;
	}

	@Override
	public void readPayload(CompoundTag data) {
		readOwner(data);

		this.temperatureSteps = data.getByte("temperatureSteps");
		this.humiditySteps = data.getByte("humiditySteps");

        this.beekeepingLogic.read(data, this.level.registryAccess());
        this.inventory.read(data, this.level.registryAccess());
	}

	@Override
	public void writeDescriptionPayload(CompoundTag data) {
		writePayload(data);
        this.beekeepingLogic.write(data, this.level.registryAccess());
	}

	@Override
	public void readDescriptionPayload(CompoundTag data) {
		readPayload(data);
        this.beekeepingLogic.read(data, this.level.registryAccess());
	}

	/* IActivatable */

	@Override
	public BlockPos getBlockPos() {
		BlockPos coord = getCenterCoord();
		return coord.offset(0, 1, 0);
	}

	@Override
	public Vec3 getBeeFXCoordinates() {
		BlockPos coord = getCenterCoord();
		return new Vec3(coord.getX() + 0.5, coord.getY() + 1.5, coord.getZ() + 0.5);
	}

	@Override
	public HumidityType humidity() {
		return this.climate.humidity().up(this.humiditySteps);
	}

	@Override
	public TemperatureType temperature() {
		IBeeModifier beeModifier = IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(this);
		if (beeModifier.isHellish() || getBiome().is(BiomeTags.IS_NETHER)) {
			if (this.temperatureSteps >= 0) {
				return TemperatureType.HELLISH;
			}
		}

		return this.climate.temperature().up(this.temperatureSteps);
	}

	@Override
	public GameProfile getOwner() {
		return getOwnerHandler().getOwner();
	}

	@Override
	public String getUnlocalizedType() {
		return "for.multiblock.alveary.type";
	}

	@Override
	public Holder<Biome> getBiome() {
		// Reference coord is nullable before a structure is installed (spec 6.1); guard like FarmController
		BlockPos coords = getReferenceCoord();
		if (coords == null) {
			return this.level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
		}
		return this.level.getBiome(coords);
	}

	@Override
	public int getBlockLightValue() {
		BlockPos topCenter = getTopCenterCoord();
		return this.level.getMaxLocalRawBrightness(topCenter.above());
	}

	@Override
	public boolean canBlockSeeTheSky() {
		BlockPos topCenter = getTopCenterCoord();
		return this.level.getBrightness(LightLayer.SKY,topCenter.offset(0,2,0))>=10;
		//return this.level.canSeeSkyFromBelowWater(topCenter.offset(0, 2, 0));
	}

	@Override
	public boolean isRaining() {
		BlockPos topCenter = getTopCenterCoord();
		return this.level.isRaining() && this.level.getBrightness(LightLayer.SKY,topCenter.offset(0,2,0))>7;
		//return this.level.isRainingAt(topCenter.offset(0, 2, 0));
	}

	@Override
	public void addTemperatureChange(byte steps) {
		this.temperatureSteps += steps;
	}

	@Override
	public void addHumidityChange(byte steps) {
		this.humiditySteps += steps;
	}

	/* GUI */
	@Override
	public int getHealthScaled(int i) {
		return this.breedingProgressPercent * i / 100;
	}

	@Override
	public void writeGuiData(RegistryFriendlyByteBuf data) {
		data.writeVarInt(this.beekeepingLogic.getBeeProgressPercent());
		NetworkUtil.writeClimateState(data, this.climate.temperature(), this.climate.humidity());
		data.writeByte(this.temperatureSteps);
		data.writeByte(this.humiditySteps);
	}

	@Override
	public void readGuiData(RegistryFriendlyByteBuf data) {
		this.breedingProgressPercent = data.readVarInt();
		this.climate = NetworkUtil.readClimateState(data);
		this.temperatureSteps = data.readByte();
		this.humiditySteps = data.readByte();
	}

    public static class AlvearyBeeModifier implements IBeeModifier {
        @Override
        public Vec3i modifyTerritory(IGenome genome, Vec3i currentModifier) {
            return currentModifier.multiply(2);
        }
    }
}
