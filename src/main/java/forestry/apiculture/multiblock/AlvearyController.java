/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.apiculture.multiblock;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.mojang.authlib.GameProfile;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.api.IForestryApi;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.climate.IClimateControlled;
import forestry.api.climate.IClimateProvider;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.multiblock.IAlvearyComponent;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.apiculture.AlvearyBeeModifier;
import forestry.apiculture.InventoryBeeHousing;
import forestry.core.inventory.FakeInventoryAdapter;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.multiblock.IMultiblockControllerInternal;
import forestry.core.multiblock.MultiblockValidationException;
import forestry.core.multiblock.RectangularMultiblockControllerBase;
import forestry.core.render.ParticleRender;
import forestry.core.utils.NetworkUtil;

public class AlvearyController extends RectangularMultiblockControllerBase implements IAlvearyControllerInternal, IClimateControlled {
	private final InventoryBeeHousing inventory;
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
		super(world, AlvearyMultiblockSizeLimits.instance);
		this.inventory = new InventoryBeeHousing(9);
		this.beekeepingLogic = IForestryApi.INSTANCE.getHiveManager().createBeekeepingLogic(this);

		this.beeModifiers.add(new AlvearyBeeModifier());
	}

	@Override
	public IBeeHousingInventory getBeeInventory() {
		return inventory;
	}

	@Override
	public IBeekeepingLogic getBeekeepingLogic() {
		return beekeepingLogic;
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		if (isAssembled()) {
			return inventory;
		} else {
			return FakeInventoryAdapter.INSTANCE;
		}
	}

	@Override
	public Iterable<IBeeListener> getBeeListeners() {
		return beeListeners;
	}

	@Override
	public Iterable<IBeeModifier> getBeeModifiers() {
		return beeModifiers;
	}

	@Override
	public void onAttachedPartWithMultiblockData(IMultiblockComponent part, CompoundTag data) {
		this.read(data);
	}

	@Override
	protected void onBlockAdded(IMultiblockComponent newPart) {
		if (newPart instanceof IAlvearyComponent) {
			if (newPart instanceof IAlvearyComponent.BeeModifier alvearyBeeModifier) {
				IBeeModifier beeModifier = alvearyBeeModifier.getBeeModifier();
				beeModifiers.add(beeModifier);
			}

			if (newPart instanceof IAlvearyComponent.BeeListener beeListenerSource) {
				IBeeListener beeListener = beeListenerSource.getBeeListener();
				beeListeners.add(beeListener);
			}

			if (newPart instanceof IAlvearyComponent.Climatiser) {
				climatisers.add((IAlvearyComponent.Climatiser) newPart);
			}

			if (newPart instanceof IAlvearyComponent.Active) {
				activeComponents.add((IAlvearyComponent.Active) newPart);
			}
		}
	}

	@Override
	protected void onBlockRemoved(IMultiblockComponent oldPart) {
		if (oldPart instanceof IAlvearyComponent) {
			if (oldPart instanceof IAlvearyComponent.BeeModifier alvearyBeeModifier) {
				IBeeModifier beeModifier = alvearyBeeModifier.getBeeModifier();
				beeModifiers.remove(beeModifier);
			}

			if (oldPart instanceof IAlvearyComponent.BeeListener beeListenerSource) {
				IBeeListener beeListener = beeListenerSource.getBeeListener();
				beeListeners.remove(beeListener);
			}

			if (oldPart instanceof IAlvearyComponent.Climatiser) {
				climatisers.remove(oldPart);
			}

			if (oldPart instanceof IAlvearyComponent.Active) {
				activeComponents.remove(oldPart);
			}
		}
	}

	@Override
	public void onDestroyed(BlockPos lastPos) {
		Containers.dropContents(this.level, lastPos, this.inventory);
	}

	@Override
	protected void onMachineAssembled() {
		super.onMachineAssembled();

		this.climate = IForestryApi.INSTANCE.getClimateManager().createClimateProvider(this.level, getCenterCoord());
	}

	@Override
	protected void isMachineWhole() throws MultiblockValidationException {
		super.isMachineWhole();

		final BlockPos maximumCoord = getMaximumCoord();
		final BlockPos minimumCoord = getMinimumCoord();

		// check that the top is covered in wood slabs

		final int slabY = maximumCoord.getY() + 1;
		for (int slabX = minimumCoord.getX(); slabX <= maximumCoord.getX(); slabX++) {
			for (int slabZ = minimumCoord.getZ(); slabZ <= maximumCoord.getZ(); slabZ++) {
				BlockPos pos = new BlockPos(slabX, slabY, slabZ);
				BlockState state = level.getBlockState(pos);
				if (!state.is(BlockTags.WOODEN_SLABS)) {
					throw new MultiblockValidationException(Component.translatable("for.multiblock.alveary.error.needSlabs").getString());
				}
			}
		}

		// check that there is space all around the alveary entrances

		int airY = maximumCoord.getY();
		for (int airX = minimumCoord.getX() - 1; airX <= maximumCoord.getX() + 1; airX++) {
			for (int airZ = minimumCoord.getZ() - 1; airZ <= maximumCoord.getZ() + 1; airZ++) {
				if (isCoordInMultiblock(airX, airY, airZ)) {
					continue;
				}
				BlockPos pos = new BlockPos(airX, airY, airZ);
				BlockState blockState = level.getBlockState(pos);
				if (blockState.isSolidRender(level, pos)) {
					throw new MultiblockValidationException(Component.translatable("for.multiblock.alveary.error.needSpace").getString());
				}
			}
		}
	}

	@Override
	protected void isGoodForExteriorLevel(IMultiblockComponent part, int level) throws MultiblockValidationException {
		if (level == 2 && !(part instanceof TileAlvearyPlain)) {
			throw new MultiblockValidationException(Component.translatable("for.multiblock.alveary.error.needPlainOnTop").getString());
		}
	}

	@Override
	protected void isGoodForInterior(IMultiblockComponent part) throws MultiblockValidationException {
		if (!(part instanceof TileAlvearyPlain)) {
			throw new MultiblockValidationException(Component.translatable("for.multiblock.alveary.error.needPlainInterior").getString());
		}
	}

	@Override
	protected void onAssimilate(IMultiblockControllerInternal assimilated) {
	}

	@Override
	public void onAssimilated(IMultiblockControllerInternal assimilator) {
	}

	@Override
	protected boolean serverTick(int tickCount) {
		for (IAlvearyComponent.Active activeComponent : activeComponents) {
			activeComponent.updateServer(tickCount);
		}

		final boolean canWork = beekeepingLogic.canWork();
		if (canWork) {
			beekeepingLogic.doWork();
		}

		// the old equalizeChange would cap out the climate increases from the climate blocks
		this.temperatureSteps = 0;
		this.humiditySteps = 0;
		// climate blocks will increase climate every tick and must go before the canWork check
		for (IAlvearyComponent.Climatiser climatiser : this.climatisers) {
			climatiser.changeClimate(tickCount, this);
		}

		// every 64 ticks, update the climate state in case of changed biome or climate (& is faster than modulus)
		if ((level.getGameTime() & 63L) == 0L) {
			this.climate = IForestryApi.INSTANCE.getClimateManager().createClimateProvider(level, getCenterCoord());
		}

		return canWork;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void clientTick(int tickCount) {
		for (IAlvearyComponent.Active activeComponent : activeComponents) {
			activeComponent.updateClient(tickCount);
		}

		if (beekeepingLogic.canDoBeeFX() && updateOnInterval(2)) {
			beekeepingLogic.doBeeFX();

			if (updateOnInterval(50)) {
				BlockPos center = getCenterCoord();
				float fxX = center.getX() + 0.5F;
				float fxY = center.getY() + 1.0F;
				float fxZ = center.getZ() + 0.5F;
				float distanceFromCenter = 1.6F;

				float leftRightSpreadFromCenter = distanceFromCenter * (level.random.nextFloat() - 0.5F);
				float upSpread = level.random.nextFloat() * 0.8F;
				fxY += upSpread;

				// display fx on all 4 sides
				ParticleRender.addEntityHoneyDustFX(level, fxX - distanceFromCenter, fxY, fxZ + leftRightSpreadFromCenter);
				ParticleRender.addEntityHoneyDustFX(level, fxX + distanceFromCenter, fxY, fxZ + leftRightSpreadFromCenter);
				ParticleRender.addEntityHoneyDustFX(level, fxX + leftRightSpreadFromCenter, fxY, fxZ - distanceFromCenter);
				ParticleRender.addEntityHoneyDustFX(level, fxX + leftRightSpreadFromCenter, fxY, fxZ + distanceFromCenter);
			}
		}
	}

	@Override
	public CompoundTag write(CompoundTag data) {
		data = super.write(data);

		data.putByte("temperatureSteps", this.temperatureSteps);
		data.putByte("humiditySteps", this.humiditySteps);

		beekeepingLogic.write(data);
		inventory.write(data);
		return data;
	}

	@Override
	public void read(CompoundTag data) {
		super.read(data);

		this.temperatureSteps = data.getByte("temperatureSteps");
		this.humiditySteps = data.getByte("humiditySteps");

		beekeepingLogic.read(data);
		inventory.read(data);
	}

	@Override
	public void formatDescriptionPacket(CompoundTag data) {
		this.write(data);
		beekeepingLogic.write(data);
	}

	@Override
	public void decodeDescriptionPacket(CompoundTag data) {
		this.read(data);
		beekeepingLogic.read(data);
	}

	/* IActivatable */

	@Override
	public BlockPos getCoordinates() {
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
		return level.getBiome(getReferenceCoord());
	}

	@Override
	public int getBlockLightValue() {
		BlockPos topCenter = getTopCenterCoord();
		return level.getMaxLocalRawBrightness(topCenter.above());
	}

	@Override
	public boolean canBlockSeeTheSky() {
		BlockPos topCenter = getTopCenterCoord();
		return level.canSeeSkyFromBelowWater(topCenter.offset(0, 2, 0));
	}

	@Override
	public boolean isRaining() {
		BlockPos topCenter = getTopCenterCoord();
		return level.isRainingAt(topCenter.offset(0, 2, 0));
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
		return breedingProgressPercent * i / 100;
	}

	@Override
	public void writeGuiData(FriendlyByteBuf data) {
		data.writeVarInt(this.beekeepingLogic.getBeeProgressPercent());
		NetworkUtil.writeClimateState(data, climate.temperature(), climate.humidity());
		data.writeByte(this.temperatureSteps);
		data.writeByte(this.humiditySteps);
	}

	@Override
	public void readGuiData(FriendlyByteBuf data) {
		this.breedingProgressPercent = data.readVarInt();
		this.climate = NetworkUtil.readClimateState(data);
		this.temperatureSteps = data.readByte();
		this.humiditySteps = data.readByte();
	}

	// REQUIRED TO AVOID AbstractMethodError crash (issue #129)
	@Override
	public boolean isEmpty() {
		return super.isEmpty();
	}
}
