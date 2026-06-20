package forestry.apiculture.multiblock;

import com.mojang.authlib.GameProfile;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.core.HumidityType;
import forestry.api.core.IErrorLogic;
import forestry.api.core.TemperatureType;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.apiculture.FakeBeekeepingLogic;
import forestry.apiculture.tiles.FakeBeeHousingInventory;
import forestry.core.errors.FakeErrorLogic;
import forestry.core.inventory.FakeInventoryAdapter;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.owner.FakeOwnerHandler;
import forestry.core.owner.IOwnerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The "no controller" stand-in resolved by {@code MultiblockLogicAlveary.getController()} when a block is
 * not part of an assembled alveary (spec §7.2, §9). Reshaped onto the trimmed public
 * {@link IAlvearyControllerInternal} after the engine rewrite (no engine-internal surface).
 */
public enum FakeAlvearyController implements IAlvearyControllerInternal {
	INSTANCE;

	@Override
	public Iterable<IBeeModifier> getBeeModifiers() {
		return Collections.emptyList();
	}

	@Override
	public Iterable<IBeeListener> getBeeListeners() {
		return Collections.emptyList();
	}

	@Override
	public IBeeHousingInventory getBeeInventory() {
		return FakeBeeHousingInventory.INSTANCE;
	}

	@Override
	public IBeekeepingLogic getBeekeepingLogic() {
		return FakeBeekeepingLogic.INSTANCE;
	}

	@Override
	public int getBlockLightValue() {
		return 0;
	}

	@Override
	public boolean canBlockSeeTheSky() {
		return false;
	}

	@Override
	public boolean isRaining() {
		return false;
	}

	@Override
	@Nullable
	public GameProfile getOwner() {
		return null;
	}

	@Override
	public BlockPos getCoordinates() {
		return BlockPos.ZERO;
	}

	@Override
	@Nullable
	public Level getWorldObj() {
		return null;
	}

	@Override
	public Vec3 getBeeFXCoordinates() {
		return Vec3.ZERO;
	}

	@Override
	public Holder<Biome> getBiome() {
		return ForgeRegistries.BIOMES.getDelegateOrThrow(Biomes.PLAINS);
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		return FakeInventoryAdapter.INSTANCE;
	}

	@Override
	public int getHealthScaled(int i) {
		return 0;
	}

	public String getUnlocalizedType() {
		return "for.multiblock.alveary.type";
	}

	/* IClimateProvider */
	@Override
	public TemperatureType temperature() {
		return TemperatureType.NORMAL;
	}

	@Override
	public HumidityType humidity() {
		return HumidityType.NORMAL;
	}

	/* IErrorLogicSource */
	@Override
	public IErrorLogic getErrorLogic() {
		return FakeErrorLogic.INSTANCE;
	}

	/* IOwnedTile */
	@Override
	public IOwnerHandler getOwnerHandler() {
		return FakeOwnerHandler.INSTANCE;
	}

	/* IStreamableGui */
	@Override
	public void writeGuiData(FriendlyByteBuf data) {
	}

	@Override
	public void readGuiData(FriendlyByteBuf data) {
	}

	/* IMultiblockController */
	@Override
	public boolean isAssembled() {
		return false;
	}

	@Override
	public void reassemble() {
	}

	@Override
	@Nullable
	public String getLastValidationError() {
		return null;
	}

	@Override
	public Collection<IMultiblockComponent> getComponents() {
		return List.of();
	}
}
