package forestry.apiculture.multiblock;

import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.apiculture.FakeBeekeepingLogic;
import forestry.apiculture.tiles.FakeBeeHousingInventory;
import forestry.core.inventory.FakeInventoryAdapter;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.multiblock.FakeMultiblockController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public enum FakeAlvearyController implements FakeMultiblockController, IAlvearyControllerInternal {
	INSTANCE;

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
	public ResolvableProfile getOwner() {
		return null;
	}

	@Override
	public BlockPos getBlockPos() {
		return BlockPos.ZERO;
	}

	@Override
	public Vec3 getBeeFXCoordinates() {
		return Vec3.ZERO;
	}

	@Override
	public Holder<Biome> getBiome(HolderLookup.Provider registries) {
		return registries.holderOrThrow(Biomes.PLAINS);
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		return FakeInventoryAdapter.INSTANCE;
	}

	@Override
	public int getHealthScaled(int i) {
		return 0;
	}

	@Nullable
	@Override
	public BlockPos getDestroyedCoord() {
		return null;
	}

	@Override
	public String getUnlocalizedType() {
		return "for.multiblock.alveary.type";
	}
}
