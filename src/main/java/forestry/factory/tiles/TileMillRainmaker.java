package forestry.factory.tiles;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;

import forestry.api.ForestryDataMaps;
import forestry.api.fuels.RainmakerFuel;
import forestry.core.render.ParticleRender;
import forestry.core.tiles.TileMill;
import forestry.factory.features.FactoryTiles;
import forestry.factory.inventory.InventoryRainmaker;

public class TileMillRainmaker extends TileMill {
	private int duration;
	private boolean reverse;

	public TileMillRainmaker(BlockPos pos, BlockState state) {
		super(FactoryTiles.RAINMAKER.tileType(), pos, state);
		this.speed = 0.01f;
		setInternalInventory(new InventoryRainmaker(this));
	}

	@Override
	public boolean interactWithItem(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
		if (this.charge == 0) {
			Holder<Item> holder = stack.getItemHolder();
			RainmakerFuel fuel = holder.getData(ForestryDataMaps.RAINMAKER_FUELS);

			if (fuel != null) {
				if (!level.isClientSide) {
					addCharge(fuel);
					if (!player.isCreative()) {
						stack.shrink(1);
					}
					sendNetworkUpdate();
				}
			}
		}
		return true;
	}

	@Override
	public boolean interactNoItem(Level level, Player player, BlockPos pos) {
		return false;
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		this.charge = nbt.getInt("Charge");
		this.progress = nbt.getFloat("Progress");
		this.stage = nbt.getInt("Stage");
		this.duration = nbt.getInt("Duration");
		this.reverse = nbt.getBoolean("Reverse");
	}


	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		nbt.putInt("Charge", this.charge);
		nbt.putFloat("Progress", this.progress);
		nbt.putInt("Stage", this.stage);
		nbt.putInt("Duration", this.duration);
		nbt.putBoolean("Reverse", this.reverse);
	}

	public void addCharge(RainmakerFuel substrate) {
		this.charge = 1;
		this.speed = substrate.speed();
		this.duration = substrate.duration();
		this.reverse = substrate.reverse();
		sendNetworkUpdate();
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
}
