package forestry.core.content.energy.tiles;

import forestry.api.ForestryConstants;
import forestry.core.platform.advancements.AdvancementHelper;
import forestry.core.platform.config.Constants;
import forestry.core.platform.damage.CoreDamageTypes;
import forestry.core.platform.tile.TemperatureState;
import forestry.core.content.energy.features.EnergyBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

public class ClockworkEngineBlockEntity extends EngineBlockEntity {

	private final static float WIND_EXHAUSTION = 0.05f;
	private final static float WIND_TENSION_BASE = 0.5f;
	private final static int WIND_DELAY = 10;

	private static final int ENGINE_CLOCKWORK_HEAT_MAX = 300000;
	private static final int ENGINE_CLOCKWORK_ENERGY_PER_CYCLE = 2;
	private static final float ENGINE_CLOCKWORK_WIND_MAX = 8f;

	private float tension = 0.0f;
	private short delay = 0;

	private static final ResourceLocation CLOCKWORK_ENGINE_HURT_PLAYER = ForestryConstants.forestry("take_damage_from_clockwork_engine");

	public ClockworkEngineBlockEntity(BlockPos pos, BlockState state) {
		super(EnergyBlockEntities.CLOCKWORK_ENGINE.tileType(), pos, state, "", ENGINE_CLOCKWORK_HEAT_MAX, 10000);
	}

	@Override
	public void openGui(ServerPlayer player, InteractionHand hand, BlockPos pos) {
		if (player instanceof FakePlayer) {
			return;
		}

		if (this.tension <= 0) {
            this.tension = WIND_TENSION_BASE;
		} else if (this.tension < ENGINE_CLOCKWORK_WIND_MAX + WIND_TENSION_BASE) {
            this.tension += (ENGINE_CLOCKWORK_WIND_MAX + WIND_TENSION_BASE - this.tension) / (ENGINE_CLOCKWORK_WIND_MAX + WIND_TENSION_BASE) * WIND_TENSION_BASE;
		} else {
			return;
		}

		player.causeFoodExhaustion(WIND_EXHAUSTION);
		if (this.tension > ENGINE_CLOCKWORK_WIND_MAX + 0.1 * WIND_TENSION_BASE) {
			if (player.hurt(CoreDamageTypes.source(this.level, CoreDamageTypes.CLOCKWORK), 6)) {
				AdvancementHelper.tryUnlock(player, CLOCKWORK_ENGINE_HURT_PLAYER);
			}
		}
        this.tension = Math.min(this.tension, ENGINE_CLOCKWORK_WIND_MAX + WIND_TENSION_BASE);
        this.delay = WIND_DELAY;
		sendNetworkUpdate();
	}

	/* LOADING & SAVING */
	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

        this.tension = nbt.getFloat("tension");
	}


	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		nbt.putFloat("tension", this.tension);
	}

	@Override
	public boolean isRedstoneActivated() {
		return true;
	}

	@Override
	public void dissipateHeat() {
	}

	@Override
	public void generateHeat() {
	}

	@Override
	public boolean mayBurn() {
		return true;
	}

	@Override
	public void burn() {
        this.heat = (int) (this.tension * 10000);

		if (this.delay > 0) {
            this.delay--;
			return;
		}

		if (!isBurning()) {
			return;
		}

		if (this.tension > 0.01f) {
            this.tension *= 0.9995f;
		} else {
            this.tension = 0;
		}
        this.energyStorage.generateEnergy(ENGINE_CLOCKWORK_ENERGY_PER_CYCLE * (int) this.tension);
        this.level.updateNeighbourForOutputSignal(this.worldPosition, getBlockState().getBlock());
	}

	@Override
	protected boolean isBurning() {
		return this.tension > 0;
	}

	@Override
	public TemperatureState getTemperatureState() {
		TemperatureState state = TemperatureState.getState(this.heat / 10000, ENGINE_CLOCKWORK_WIND_MAX);
		if (state == TemperatureState.MELTING) {
			state = TemperatureState.OVERHEATING;
		}
		return state;
	}

	@Override
	public float getPistonSpeed() {
		if (this.delay > 0) {
			return 0;
		}

		float fromClockwork = this.tension / ENGINE_CLOCKWORK_WIND_MAX * Constants.ENGINE_PISTON_SPEED_MAX;

		fromClockwork = Math.round(fromClockwork * 100f) / 100f;

		return fromClockwork;
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
		return null;
	}
}
