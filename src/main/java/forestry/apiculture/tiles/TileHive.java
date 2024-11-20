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
package forestry.apiculture.tiles;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.authlib.GameProfile;

import forestry.api.IForestryApi;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.hives.IHiveTile;
import forestry.api.core.HumidityType;
import forestry.api.core.IErrorLogic;
import forestry.api.core.ISpectacleBlock;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.util.TickHelper;
import forestry.apiculture.ModuleApiculture;
import forestry.apiculture.WorldgenBeekeepingLogic;
import forestry.apiculture.blocks.BlockBeeHive;
import forestry.apiculture.features.ApicultureTiles;
import forestry.apiculture.genetics.effects.ThrottledBeeEffect;
import forestry.core.damage.CoreDamageTypes;
import forestry.core.inventory.InventoryAdapter;
import forestry.core.network.packets.PacketActiveUpdate;
import forestry.core.tiles.IActivatable;
import forestry.core.utils.InventoryUtil;
import forestry.core.utils.ItemStackUtil;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.SpeciesUtil;

import org.jetbrains.annotations.ApiStatus;

public class TileHive extends BlockEntity implements IHiveTile, IActivatable, IBeeHousing, ISpectacleBlock {
	private final InventoryAdapter contained = new InventoryAdapter(2, "Contained");
	private final HiveBeeHousingInventory inventory;
	private final WorldgenBeekeepingLogic beeLogic;
	private final IErrorLogic errorLogic;
	private final Predicate<LivingEntity> beeTargetPredicate;
	private final TickHelper tickHelper = new TickHelper(0);

	@Nullable
	private IBee containedBee = null;
	private boolean active = false;
	private boolean angry = false;
	private int calmTime;

	// For addons
	public TileHive(BlockEntityType<? extends TileHive> tileType, BlockPos pos, BlockState state) {
		super(tileType, pos, state);

		this.inventory = new HiveBeeHousingInventory(this);
		this.beeLogic = new WorldgenBeekeepingLogic(this);
		this.errorLogic = IForestryApi.INSTANCE.getErrorManager().createErrorLogic();
		this.beeTargetPredicate = new BeeTargetPredicate(this);
	}

	// For Forestry only
	@ApiStatus.Internal
	public TileHive(BlockPos pos, BlockState state) {
		this(ApicultureTiles.HIVE.tileType(), pos, state);
	}

	public void tick(Level level) {
		tickHelper.onTick();

		if (level.isClientSide) {
			if (active && tickHelper.updateOnInterval(4)) {
				if (beeLogic.canDoBeeFX()) {
					beeLogic.doBeeFX();
				}
			}
		} else {
			boolean canWork = beeLogic.canWork(); // must be called every tick to stay updated

			if (tickHelper.updateOnInterval(angry ? 10 : 200)) {
				if (calmTime == 0) {
					if (canWork) {
						if (angry && ModuleApiculture.hiveDamageOnAttack && (level.getLevelData().getDifficulty() != Difficulty.PEACEFUL || ModuleApiculture.hivesDamageOnPeaceful)) {
							AABB boundingBox = ThrottledBeeEffect.getBounding(this, getContainedBee().getGenome());
							List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, boundingBox, beeTargetPredicate);
							if (!entities.isEmpty()) {
								Collections.shuffle(entities);
								LivingEntity entity = entities.get(0);
								if ((entity instanceof Player || !ModuleApiculture.hivesDamageOnlyPlayers) && (!entity.isInWater() || ModuleApiculture.hivesDamageUnderwater)) {
									attack(entity, 2);
								}
							}
						}
						beeLogic.doWork();
					}
				} else {
					calmTime--;
				}
			}

			setActive(calmTime == 0);
		}
	}

	public IBee getContainedBee() {
		if (this.containedBee == null) {
			ItemStack containedBee = contained.getItem(0);
			if (!containedBee.isEmpty()) {
				if (IIndividualHandlerItem.getIndividual(containedBee) instanceof IBee bee) {
					return this.containedBee = bee;
				}
			}
			IBeeSpecies primarySpecies = SpeciesUtil.BEE_TYPE.get().getSpeciesSafe(((BlockBeeHive) getBlockState().getBlock()).getSpeciesId());
			if (primarySpecies != null) {
				return this.containedBee = primarySpecies.createIndividual();
			}
			return this.containedBee = SpeciesUtil.getBeeSpecies(ForestryBeeSpecies.FOREST).createIndividual();
		} else {
			return this.containedBee;
		}
	}

	public void setContained(List<ItemStack> bees) {
		for (ItemStack itemstack : bees) {
			InventoryUtil.addStack(contained, itemstack, true);
		}
	}

	@Override
	public void load(CompoundTag compoundNBT) {
		super.load(compoundNBT);
		contained.read(compoundNBT);
		beeLogic.read(compoundNBT);
	}


	@Override
	public void saveAdditional(CompoundTag compoundNBT) {
		super.saveAdditional(compoundNBT);
		contained.write(compoundNBT);
		beeLogic.write(compoundNBT);
	}

	@Override
	public void calmBees() {
		calmTime = 5;
		angry = false;
		setActive(false);
	}

	@Override
	public boolean isAngry() {
		return angry;
	}

	@Override
	public void onAttack(Level world, BlockPos pos, Player player) {
		if (calmTime == 0) {
			angry = true;
		}
	}

	@Override
	public void onBroken(Level world, BlockPos pos, Player player, boolean canHarvest) {
		if (calmTime == 0) {
			attack(player, 10);
		}

		if (canHarvest) {
			for (ItemStack beeStack : InventoryUtil.getStacks(contained)) {
				if (beeStack != null) {
					ItemStackUtil.dropItemStackAsEntity(beeStack, world, pos);
				}
			}
		}
	}

	private static void attack(LivingEntity entity, int maxDamage) {
		Level level = entity.level();
		double attackAmount = level.random.nextDouble() / 2.0 + 0.5;
		int damage = (int) (attackAmount * maxDamage);
		if (damage > 0) {
			// Entities are not attacked if they wear a full set of apiarist's armor.
			int count = BeeManager.armorApiaristHelper.wearsItems(entity, null, true);
			if (level.random.nextInt(4) >= count) {
				entity.hurt(new DamageSource(CoreDamageTypes.HIVE.getHolder().get()), damage);
			}
		}
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	@Override
	public void setActive(boolean active) {
		if (this.active == active) {
			return;
		}
		this.active = active;

		if (!this.level.isClientSide) {
			NetworkUtil.sendNetworkPacket(new PacketActiveUpdate(this), this.worldPosition, this.level);
		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag nbt = super.getUpdateTag();
		nbt.putBoolean("active", calmTime == 0);
		this.beeLogic.write(nbt);
		return nbt;
	}

	// todo wtf are these two methods (loading from NBT several times per packet)
	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		setActive(tag.getBoolean("active"));
		this.beeLogic.read(tag);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		CompoundTag nbt = pkt.getTag();
		handleUpdateTag(nbt);
	}

	@Override
	public Iterable<IBeeModifier> getBeeModifiers() {
		return List.of();
	}

	@Override
	public Iterable<IBeeListener> getBeeListeners() {
		return List.of();
	}

	@Override
	public IBeeHousingInventory getBeeInventory() {
		return this.inventory;
	}

	@Override
	public IBeekeepingLogic getBeekeepingLogic() {
		return this.beeLogic;
	}

	@Override
	public TemperatureType temperature() {
		return IForestryApi.INSTANCE.getClimateManager().getTemperature(getBiome());
	}

	@Override
	public HumidityType humidity() {
		return IForestryApi.INSTANCE.getClimateManager().getHumidity(getBiome());
	}

	@Override
	public int getBlockLightValue() {
		return this.level.isDay() ? 15 : 0; // hives may have the sky obstructed but should still be active
	}

	@Override
	public boolean canBlockSeeTheSky() {
		return true; // hives may have the sky obstructed but should still be active
	}

	@Override
	public boolean isRaining() {
		return this.level.isRainingAt(this.worldPosition.above());
	}

	@Override
	public @Nullable Level getWorldObj() {
		return this.level;
	}

	@Override
	public Holder<Biome> getBiome() {
		return this.level.getBiome(this.worldPosition);
	}

	@Override
	@Nullable
	public GameProfile getOwner() {
		return null;
	}

	@Override
	public Vec3 getBeeFXCoordinates() {
		BlockPos pos = this.worldPosition;
		return new Vec3(pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5);
	}

	@Override
	public IErrorLogic getErrorLogic() {
		return errorLogic;
	}

	@Override
	public BlockPos getCoordinates() {
		return this.worldPosition;
	}

	private record BeeTargetPredicate(IHiveTile hive) implements Predicate<LivingEntity> {
		@Override
		public boolean apply(@Nullable LivingEntity input) {
			if (input != null && input.isAlive() && !input.isInvisible()) {
				if (input instanceof Player) {
					return EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(input);
				} else if (hive.isAngry()) {
					return true;
				} else if (input instanceof Enemy) {
					// don't attack semi-passive vanilla mobs
					return !(input instanceof EnderMan) && !(input instanceof ZombifiedPiglin);
				}
			}
			return false;
		}
	}
}
