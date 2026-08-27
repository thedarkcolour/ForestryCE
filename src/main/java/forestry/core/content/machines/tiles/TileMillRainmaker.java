package forestry.core.content.machines.tiles;

import forestry.api.ForestryConstants;
import forestry.core.platform.advancements.AdvancementHelper;
import forestry.core.platform.render.ParticleRender;
import forestry.core.content.machines.TileMill;
import forestry.core.content.machines.features.FactoryTiles;
import forestry.core.content.machines.inventory.InventoryRainmaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;

import javax.annotation.Nullable;
import forestry.api.ForestryDataMaps;
import forestry.api.core.machines.fuels.RainmakerFuel;

public class TileMillRainmaker extends TileMill {
	private static final ResourceLocation USE_RAINMAKER = ForestryConstants.forestry("use_rainmaker");
	private static final int MAX_PARTICLE_COUNT = 64;

	private int duration;
	private boolean reverse;

	// height of the cloud column climbing above the rainmaker, in sendParticles calls
	private int yParticle;
	private int particleCount = MAX_PARTICLE_COUNT;

	public TileMillRainmaker(BlockPos pos, BlockState state) {
		super(FactoryTiles.RAINMAKER.tileType(), pos, state);
        this.speed = 0.01f;
		setInternalInventory(new InventoryRainmaker(this));
	}

	@Override
	public void openGui(ServerPlayer player, InteractionHand hand, BlockPos pos) {
		if (!player.level().isClientSide) {
			ItemStack heldItem = player.getItemInHand(hand);

			// We don't have a gui, but we can be activated
			RainmakerFuel substrate = heldItem.getItemHolder().getData(ForestryDataMaps.RAINMAKER_FUELS);
			if (substrate != null && this.charge == 0) {
				addCharge(substrate);
				if (!player.isCreative()) {
					heldItem.shrink(1);
				}
				AdvancementHelper.tryUnlock(player, USE_RAINMAKER);
			}
			sendNetworkUpdate();
		}
	}

	@Override
	public void loadAdditional(CompoundTag compoundNBT, HolderLookup.Provider registries) {
		super.loadAdditional(compoundNBT, registries);

        this.charge = compoundNBT.getInt("Charge");
        this.progress = compoundNBT.getFloat("Progress");
        this.stage = compoundNBT.getInt("Stage");
        this.duration = compoundNBT.getInt("Duration");
        this.reverse = compoundNBT.getBoolean("Reverse");
	}


	@Override
	public void saveAdditional(CompoundTag compoundNBT, HolderLookup.Provider registries) {
		super.saveAdditional(compoundNBT, registries);

		compoundNBT.putInt("Charge", this.charge);
		compoundNBT.putFloat("Progress", this.progress);
		compoundNBT.putInt("Stage", this.stage);
		compoundNBT.putInt("Duration", this.duration);
		compoundNBT.putBoolean("Reverse", this.reverse);
	}

	public void addCharge(RainmakerFuel substrate) {
        this.charge = 1;
        this.speed = substrate.speed();
        this.duration = substrate.duration();
        this.reverse = substrate.reverse();
		sendNetworkUpdate();
	}

	@Override
	protected void update(Level level, BlockPos pos, boolean isSimulating) {
		super.update(level, pos, isSimulating);

		if (this.particleCount < MAX_PARTICLE_COUNT && level instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.CLOUD,
				pos.getX() + 0.5,
				this.yParticle,
				pos.getZ() + 0.5,
				10,
				0.025f,
				1f,
				0.025f,
				0.01f
			);

			this.yParticle += 2;
			this.particleCount++;
		}
	}

	@Override
	public void activate(Level level, BlockPos pos) {
		if (level.isClientSide) {
			level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 10000.0F, 0.8F + level.random.nextFloat() * 0.2F);

			float f = pos.getX() + 0.5F;
			float f1 = pos.getY() + level.random.nextFloat() * 6F / 16F;
			float f2 = pos.getZ() + 0.5F;
			float f3 = 0.52F;
			float f4 = level.random.nextFloat() * 0.6F - 0.3F;

			ParticleRender.addEntityExplodeFX(level, f - f3, f1, f2 + f4);
			ParticleRender.addEntityExplodeFX(level, f + f3, f1, f2 + f4);
			ParticleRender.addEntityExplodeFX(level, f + f4, f1, f2 - f3);
			ParticleRender.addEntityExplodeFX(level, f + f4, f1, f2 + f3);
		} else {
			// restart the cloud column so it climbs again from this activation
			this.particleCount = 0;
			this.yParticle = pos.getY() + 2;

			if (this.reverse) {
				level.getLevelData().setRaining(false);
			} else {
				level.getLevelData().setRaining(true);
				((ServerLevelData) level.getLevelData()).setRainTime(this.duration);
			}
            this.charge = 0;
            this.duration = 0;
            this.reverse = false;
			sendNetworkUpdate();
		}
	}

	@Override
	@Nullable
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return null;
	}

	@Override
	protected boolean hasGui() {
		return false;
	}
}
