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
package forestry.lepidopterology.entities;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;

import forestry.api.ForestryTags;
import forestry.api.IForestryApi;
import forestry.api.client.IForestryClientApi;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.ButterflyChromosomes;
import forestry.api.genetics.pollen.IPollen;
import forestry.api.genetics.pollen.IPollenType;
import forestry.api.lepidopterology.IEntityButterfly;
import forestry.api.lepidopterology.ILepidopteristTracker;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.api.lepidopterology.genetics.IButterflySpeciesType;
import forestry.core.config.ForestryConfig;
import forestry.core.utils.ItemStackUtil;
import forestry.core.utils.SpeciesUtil;
import forestry.lepidopterology.ModuleLepidopterology;

public class EntityButterfly extends PathfinderMob implements IEntityButterfly {
	private static final String NBT_BUTTERFLY = "BTFLY";
	private static final String NBT_POLLEN_TYPE = "PLNTP";
	private static final String NBT_POLLEN = "PLN";
	private static final String NBT_STATE = "STATE";
	private static final String NBT_EXHAUSTION = "EXH";
	private static final String NBT_HOME = "HOME";

	/* CONSTANTS */
	public static final int COOLDOWNS = 1500;

	private static final EntityDataAccessor<String> DATAWATCHER_ID_SPECIES = SynchedEntityData.defineId(EntityButterfly.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Integer> DATAWATCHER_ID_SIZE = SynchedEntityData.defineId(EntityButterfly.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Byte> DATAWATCHER_ID_STATE = SynchedEntityData.defineId(EntityButterfly.class, EntityDataSerializers.BYTE);

	private static final float DEFAULT_BUTTERFLY_SIZE = 0.75f;
	private static final EnumButterflyState DEFAULT_STATE = EnumButterflyState.FLYING;

	public static final int EXHAUSTION_REST = 1000;
	public static final int EXHAUSTION_CONSUMPTION = 100 * EXHAUSTION_REST;
	public static final int MAX_LIFESPAN = 24000 * 7; // one minecraft week in ticks

	@Nullable
	private Vec3 flightTarget;
	private int exhaustion;
	private IButterfly contained = IForestryApi.INSTANCE.getGeneticManager().createDefaultIndividual(ForestrySpeciesTypes.BUTTERFLY);
	@Nullable
	private IPollen<?> pollen;

	public int cooldownPollination = 0;
	public int cooldownEgg = 0;
	public int cooldownMate = 0;
	private boolean isImmuneToFire;

	// Client Rendering
	@Nullable
	private IButterflySpecies species;
	private float size = DEFAULT_BUTTERFLY_SIZE;
	private EnumButterflyState state = DEFAULT_STATE;
	@OnlyIn(Dist.CLIENT)
	private ResourceLocation textureResource;

	public EntityButterfly(EntityType<EntityButterfly> type, Level world) {
		super(type, world);
	}

	public static EntityButterfly create(EntityType<EntityButterfly> type, Level world, IButterfly butterfly, BlockPos homePos) {
		EntityButterfly bf = new EntityButterfly(type, world);
		bf.setIndividual(butterfly);
		bf.restrictTo(homePos, ModuleLepidopterology.maxDistance);
		return bf;
	}

	// Returns true if too many butterflies are in the same area according to config values
	public static boolean isMaxButterflyCluster(Vec3 center, Level level) {
		return level.getEntities(null, AABB.ofSize(center, ForestryConfig.SERVER.butterflyClusterWidth.get(), ForestryConfig.SERVER.butterflyClusterHeight.get(), ForestryConfig.SERVER.butterflyClusterWidth.get())).size() > ForestryConfig.SERVER.butterflyClusterLimit.get();
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();

		entityData.define(DATAWATCHER_ID_SPECIES, "");
		entityData.define(DATAWATCHER_ID_SIZE, (int) (DEFAULT_BUTTERFLY_SIZE * 100));
		entityData.define(DATAWATCHER_ID_STATE, (byte) DEFAULT_STATE.ordinal());
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(8, new AIButterflyFlee(this));
		this.goalSelector.addGoal(9, new AIButterflyMate(this));
		this.goalSelector.addGoal(10, new AIButterflyPollinate(this));
		this.goalSelector.addGoal(11, new AIButterflyRest(this));
		this.goalSelector.addGoal(12, new AIButterflyRise(this));
		this.goalSelector.addGoal(13, new AIButterflyWander(this));
	}

	@Override
	public PathfinderMob getEntity() {
		return this;
	}

	/* SAVING & LOADING */
	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);

		// Individual data
		Tag containedNbt = SpeciesUtil.serializeIndividual(this.contained);
		if (containedNbt != null) {
			nbt.put(NBT_BUTTERFLY, containedNbt);
		}

		// Pollen data
		if (pollen != null) {
			Tag pollenNbt = this.pollen.writeNbt();
			if (pollenNbt != null) {
				nbt.putString(NBT_POLLEN_TYPE, pollen.getType().id().toString());
				nbt.put(NBT_POLLEN, pollenNbt);
			}
		}

		nbt.putByte(NBT_STATE, (byte) getState().ordinal());
		nbt.putInt(NBT_EXHAUSTION, exhaustion);

		nbt.putLong(NBT_HOME, getRestrictCenter().asLong());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);

		IButterfly butterfly = null;
		if (nbt.contains(NBT_BUTTERFLY)) {
			butterfly = SpeciesUtil.deserializeIndividual(SpeciesUtil.BUTTERFLY_TYPE.get(), nbt.getCompound(NBT_BUTTERFLY));
		}
		setIndividual(butterfly);

		Tag pollenNbt = nbt.get(NBT_POLLEN);
		if (pollenNbt != null && nbt.contains(NBT_POLLEN_TYPE)) {
			IPollenType<?> type = IForestryApi.INSTANCE.getPollenManager().getPollenType(new ResourceLocation(nbt.getString(NBT_POLLEN_TYPE)));

			if (type != null) {
				this.pollen = type.readNbt(pollenNbt);
			}
		}

		EnumButterflyState butterflyState = EnumButterflyState.VALUES[nbt.getByte(NBT_STATE)];
		setState(butterflyState);
		exhaustion = nbt.getInt(NBT_EXHAUSTION);
		BlockPos home = BlockPos.of(nbt.getLong(NBT_HOME));
		restrictTo(home, ModuleLepidopterology.maxDistance);
	}

	public float getWingFlap(float partialTickTime) {
		int offset = species != null ? species.id().toString().hashCode() : level().random.nextInt();
		return getState().getWingFlap(this, offset, partialTickTime);
	}

	/* STATE - Used for AI and rendering */
	public void setState(EnumButterflyState state) {
		if (this.state != state) {
			this.state = state;
			if (!level().isClientSide) {
				entityData.set(DATAWATCHER_ID_STATE, (byte) state.ordinal());
			}
		}
	}

	public EnumButterflyState getState() {
		return state;
	}

	public float getSize() {
		return size;
	}

	@Override
	public float getSpeed() {
		return contained.getGenome().getActiveValue(ButterflyChromosomes.SPEED);
	}

	@Override
	public boolean fireImmune() {
		return isImmuneToFire;
	}

	/* DESTINATION */
	@Nullable
	public Vec3 getDestination() {
		return flightTarget;
	}

	public void setDestination(@Nullable Vec3 destination) {
		flightTarget = destination;
	}

	@Override
	public float getWalkTargetValue(BlockPos pos) {
		Level level = level();
		if (!level.hasChunkAt(pos)) {
			return -100f;
		}

		float weight = 0.0f;
		double distanceToHome = getRestrictCenter().distSqr(pos);

		if (!isWithinHomeDistanceFromPosition(distanceToHome)) {
			weight -= 7.5f + 0.005 * (distanceToHome / 4);
		}

		if (!getButterfly().isAcceptedEnvironment(level, pos.getX(), pos.getY(), pos.getZ())) {
			weight -= 15.0f;
		}

		if (!level.getEntitiesOfClass(EntityButterfly.class, new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)).isEmpty()) {
			weight -= 1.0f;
		}

		int depth = getFluidDepth(pos);
		if (depth > 0) {
			weight -= 0.1f * depth;
		} else {
			BlockState blockState = level.getBlockState(pos);
			Block block = blockState.getBlock();
			if (blockState.is(BlockTags.FLOWERS)) {
				weight += 2.0f;
			} else if (block instanceof IPlantable) {
				weight += 1.5f;
			} else if (block instanceof BonemealableBlock) {
				weight += 1.0f;
			} else if (blockState.is(BlockTags.LEAVES)) {
				weight += 1.0f;
			}

			BlockPos posBelow = pos.below();
			BlockState blockStateBelow = level.getBlockState(posBelow);
			Block blockBelow = blockStateBelow.getBlock();
			if (blockState.is(BlockTags.LEAVES)) {
				weight += 5.0f;
			} else if (blockBelow instanceof FenceBlock) {
				weight += 1.0f;
			} else if (blockBelow instanceof WallBlock) {
				weight += 1.0f;
			}
		}

		weight += level.getBrightness(LightLayer.SKY, pos);
		return weight;
	}

	private boolean isWithinHomeDistanceFromPosition(double distanceToHome) {
		return distanceToHome < this.getRestrictRadius() * this.getRestrictRadius();
	}

	// todo address the deprecated function
	private int getFluidDepth(BlockPos pos) {
		ChunkAccess chunk = level().getChunk(pos);
		int xx = pos.getX() & 15;
		int zz = pos.getZ() & 15;
		int depth = 0;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(xx, 0, zz);

		for (int y = chunk.getHighestSectionPosition() + 15; y > 0; --y) {
			BlockState blockState = chunk.getBlockState(cursor.setY(y));
			if (blockState.liquid()) {
				depth++;
			} else if (!blockState.isAir()) {
				break;
			}
		}

		return depth;
	}

	/* POLLEN */
	@Override
	@Nullable
	public IPollen<?> getPollen() {
		return pollen;
	}

	@Override
	public void setPollen(@Nullable IPollen<?> pollen) {
		this.pollen = pollen;
	}

	/* EXHAUSTION */
	@Override
	public void changeExhaustion(int change) {
		exhaustion = Math.max(exhaustion + change, 0);
	}

	@Override
	public int getExhaustion() {
		return exhaustion;
	}

	/* FLYING ABILITY */
	public boolean canFly() {
		return this.contained.canTakeFlight(level(), getX(), getY(), getZ());
	}

	public void setIndividual(@Nullable IButterfly butterfly) {
		if (butterfly == null) {
			butterfly = IForestryApi.INSTANCE.getGeneticManager().createDefaultIndividual(ForestrySpeciesTypes.BUTTERFLY);
		}
		contained = butterfly;

		IGenome genome = contained.getGenome();

		this.isImmuneToFire = genome.getActiveValue(ButterflyChromosomes.FIREPROOF);
		this.size = genome.getActiveValue(ButterflyChromosomes.SIZE);
		this.species = genome.getActiveValue(ButterflyChromosomes.SPECIES);

		if (!level().isClientSide) {
			entityData.set(DATAWATCHER_ID_SIZE, (int) (size * 100));
			entityData.set(DATAWATCHER_ID_SPECIES, this.species.id().toString());
		} else {
			textureResource = IForestryClientApi.INSTANCE.getButterflyManager().getTextures(this.species).getSecond();
		}
	}

	@Override
	public IButterfly getButterfly() {
		return contained;
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
		if (!level().isClientSide) {
			setIndividual(contained);
		}
		return spawnDataIn;
	}

	@Override
	public Component getName() {
		if (species == null) {
			return super.getName();
		}
		return species.getDisplayName();
	}

	@Override
	public boolean checkSpawnRules(LevelAccessor worldIn, MobSpawnType spawnReasonIn) {
		return true;
	}

	@Override
	public int getPortalWaitTime() {
		return 1000;
	}

	public boolean isRenderable() {
		return species != null;
	}

	@OnlyIn(Dist.CLIENT)
	public ResourceLocation getTexture() {
		return textureResource;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void doPush(Entity other) {
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return tickCount > MAX_LIFESPAN;
	}

	/* INTERACTION */

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (dead) {
			return InteractionResult.FAIL;
		}
		ItemStack stack = player.getItemInHand(hand);
		if (stack.is(ForestryTags.Items.SCOOPS)) {
			Level level = level();
			if (!level.isClientSide) {
				IButterflySpeciesType type = SpeciesUtil.BUTTERFLY_TYPE.get();
				ILepidopteristTracker tracker = (ILepidopteristTracker) type.getBreedingTracker(level, player.getGameProfile());
				ItemStack itemStack = contained.createStack(ButterflyLifeStage.BUTTERFLY);

				tracker.registerCatch(contained);
				ItemStackUtil.dropItemStackAsEntity(itemStack, level, getX(), getY(), getZ());
				remove(RemovalReason.KILLED);
			} else {
				player.swing(hand);
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}

		return super.mobInteract(player, hand);
	}

	/* LOOT */
	@Override
	protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
		Level level = level();
		for (ItemStack stack : contained.getLootDrop(this, recentlyHitIn, looting)) {
			ItemStackUtil.dropItemStackAsEntity(stack, level, getX(), getY(), getZ());
		}

		// Drop pollen if any
		IPollen<?> pollen = this.pollen;

		if (pollen != null) {
			ItemStack pollenStack = pollen.createStack();
			ItemStackUtil.dropItemStackAsEntity(pollenStack, level, getX(), getY(), getZ());
		}
	}

	/* UPDATING */
	@Override
	public void tick() {
		super.tick();

		// Update stuff client side
		if (level().isClientSide) {
			if (species == null) {
				String speciesUid = entityData.get(DATAWATCHER_ID_SPECIES);
				IButterflySpecies species = SpeciesUtil.BUTTERFLY_TYPE.get().getSpeciesSafe(new ResourceLocation(speciesUid));

				if (species != null) {
					this.species = species;
					this.textureResource = IForestryClientApi.INSTANCE.getButterflyManager().getTextures(this.species).getSecond();
					this.size = entityData.get(DATAWATCHER_ID_SIZE) / 100f;
				}
			}

			byte stateOrdinal = entityData.get(DATAWATCHER_ID_STATE);
			if (state.ordinal() != stateOrdinal) {
				setState(EnumButterflyState.VALUES[stateOrdinal]);
			}
		}

		Vec3 motion = getDeltaMovement();
		if (state == EnumButterflyState.FLYING && flightTarget != null && flightTarget.y > position().y) {
			setDeltaMovement(motion.x, motion.y * 0.6 + 0.15, motion.z);
		} else {
			setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
		}

		// Make sure we die if the butterfly hasn't rested in a long, long time.
		if (exhaustion > EXHAUSTION_CONSUMPTION && random.nextInt(20) == 0) {
			hurt(damageSources().generic(), 1);
		}

		if (tickCount > MAX_LIFESPAN) {
			hurt(damageSources().generic(), 1);
		}

		// Reduce cooldowns
		if (cooldownEgg > 0) {
			cooldownEgg--;
		}
		if (cooldownPollination > 0) {
			cooldownPollination--;
		}
		if (cooldownMate > 0) {
			cooldownMate--;
		}
	}

	@Override
	protected void customServerAiStep() {
		Vec3 flightTarget = this.flightTarget;
		if (getState().doesMovement && flightTarget != null) {
			Vec3 position = position();
			double diffX = flightTarget.x + 0.5 - position.x;
			double diffY = flightTarget.y + 0.1 - position.y;
			double diffZ = flightTarget.z + 0.5 - position.z;

			Vec3 motion = getDeltaMovement();
			double newX = motion.x + (Math.signum(diffX) * 0.5 - motion.x) * 0.1;
			double newY = motion.y + (Math.signum(diffY) * 0.7 - motion.y) * 0.1;
			double newZ = motion.z + (Math.signum(diffZ) * 0.5 - motion.z) * 0.1;

			setDeltaMovement(newX, newY, newZ);

			float horizontal = (float) (Mth.atan2(newZ, newX) * Mth.RAD_TO_DEG) - 90f;
			setYRot(getYRot() + Mth.wrapDegrees(horizontal - getYRot()));

			setZza(contained.getGenome().getActiveValue(ButterflyChromosomes.SPEED));
		} else {
			setDeltaMovement(getDeltaMovement().multiply(1, 0.6, 1));
		}
	}

	@Override
	public boolean causeFallDamage(float p_147187_, float p_147188_, DamageSource p_147189_) {
		return false;
	}

	@Override
	protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Override
	protected float getSoundVolume() {
		return 0.1F;
	}

	@Override
	public ItemStack getPickedResult(HitResult target) {
		if (this.species == null) {
			return ItemStack.EMPTY;
		}
		return this.species.createStack(ButterflyLifeStage.BUTTERFLY);
	}

	@Override
	public boolean canMateWith(IEntityButterfly butterfly) {
		if (butterfly.getButterfly().getMate() != null) {
			return false;
		}
		if (getButterfly().getMate() != null) {
			return false;
		}
		return !getButterfly().getGenome().isSameAlleles(butterfly.getButterfly().getGenome());
	}

	@Override
	public boolean canMate() {
		return cooldownMate <= 0;
	}
}
