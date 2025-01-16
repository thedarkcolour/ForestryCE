package forestry.arboriculture.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureEntities;
import forestry.arboriculture.features.ArboricultureItems;

public class ForestryBoat extends Boat {
	private static final EntityDataAccessor<Integer> DATA_ID_WOOD_TYPE = SynchedEntityData.defineId(ForestryBoat.class, EntityDataSerializers.INT);

	public ForestryBoat(EntityType<? extends Boat> type, Level level) {
		super(type, level);
	}

	public ForestryBoat(Level level, double x, double y, double z) {
		this(ArboricultureEntities.BOAT.entityType(), level);

		setPos(x, y, z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_ID_WOOD_TYPE, ForestryWoodType.ACACIA_DESERT.ordinal());
	}

	@Override
	public double getPassengersRidingOffset() {
		return -0.1;
	}

	@Override
	public Item getDropItem() {
		return ArboricultureItems.BOAT.item(getWoodType());
	}

	// Copied from vanilla
	@Override
	protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
		this.lastYd = getDeltaMovement().y;
		if (!isPassenger()) {
			Level level = level();

			if (onGround) {
				if (this.fallDistance > 3f) {
					if (this.status != Boat.Status.ON_LAND) {
						resetFallDistance();
						return;
					}

					causeFallDamage(this.fallDistance, 1f, damageSources().fall());
					if (!level.isClientSide && !isRemoved()) {
						this.kill();
						if (level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
							for (int i = 0; i < 3; ++i) {
								spawnAtLocation(ArboricultureBlocks.PLANKS.get(getWoodType()));
							}

							for (int j = 0; j < 2; ++j) {
								spawnAtLocation(Items.STICK);
							}
						}
					}
				}

				resetFallDistance();
			} else if (!canBoatInFluid(level.getFluidState(blockPosition().below())) && y < 0) {
				this.fallDistance -= (float) y;
			}
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt) {
		nbt.putString("type", getWoodType().name());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		if (nbt.contains("type", 8)) {
			try {
				setWoodType(ForestryWoodType.valueOf(nbt.getString("type")));
			} catch (IllegalArgumentException e) {
				setWoodType(ForestryWoodType.ACACIA_DESERT);
			}
		}
	}

	public void setWoodType(ForestryWoodType woodType) {
		this.entityData.set(DATA_ID_WOOD_TYPE, woodType.ordinal());
	}

	public ForestryWoodType getWoodType() {
		return ForestryWoodType.VALUES[this.entityData.get(DATA_ID_WOOD_TYPE)];
	}
}
