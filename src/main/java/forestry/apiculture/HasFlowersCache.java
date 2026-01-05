package forestry.apiculture;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IFlowerType;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.util.TickHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

// Cache used to determine if a beehive has a suitable flower nearby.
// This passively checks one block a tick in a spiraling pattern centered on the hive,
// but the entire area is checked at once when a player opens the hive GUI.
public class HasFlowersCache implements INbtWritable, INbtReadable {
	private static final String NBT_KEY = "hasFlowerCache";
	private static final String NBT_KEY_FLOWERS = "flowers";
	private final int flowerCheckInterval;

	private final TickHelper tickHelper = new TickHelper(0);

	public HasFlowersCache() {
		this.flowerCheckInterval = 200;
	}

	public HasFlowersCache(int checkInterval) {
		this.flowerCheckInterval = checkInterval;
	}

	@Nullable
	private FlowerData flowerData;
	private final ArrayList<BlockPos> flowerCoords = new ArrayList<>();
	private final List<BlockState> flowers = new ArrayList<>();

	private boolean needsSync = false;

	private static class FlowerData {
		public final IFlowerType flowerType;
		public final Vec3i territory;
		public Iterator<BlockPos.MutableBlockPos> areaIterator;

		public FlowerData(IBee queen, IBeeHousing housing) {
			this.flowerType = queen.getGenome().getActiveValue(BeeChromosomes.FLOWER_TYPE);
			this.territory = queen.getGenome().getActiveValue(BeeChromosomes.TERRITORY);
			this.areaIterator = queen.getAreaIterator(housing);
		}

		public void resetIterator(IBee queen, IBeeHousing beeHousing) {
			this.areaIterator = queen.getAreaIterator(beeHousing);
		}
	}

	public void update(IBee queen, IBeeHousing beeHousing) {
		if (this.flowerData == null) {
			this.flowerData = new FlowerData(queen, beeHousing);
			this.flowerCoords.clear();
			this.flowers.clear();
		}
		Level level = beeHousing.getWorldObj();
        this.tickHelper.onTick();

		if (!this.flowerCoords.isEmpty() && this.tickHelper.updateOnInterval(this.flowerCheckInterval)) {
			Iterator<BlockPos> iterator = this.flowerCoords.iterator();
			while (iterator.hasNext()) {
				BlockPos flowerPos = iterator.next();
				if (level.hasChunkAt(flowerPos) && !this.flowerData.flowerType.isAcceptableFlower(level, flowerPos)) {
					iterator.remove();
                    this.flowers.clear();
                    this.needsSync = true;
				}
			}
		}

		final int flowerCount = this.flowerCoords.size();
		final int ticksPerCheck = 1 + (flowerCount * flowerCount);

		if (this.tickHelper.updateOnInterval(ticksPerCheck)) {
			if (this.flowerData.areaIterator.hasNext()) {
				BlockPos.MutableBlockPos blockPos = this.flowerData.areaIterator.next();
				if (this.flowerData.flowerType.isAcceptableFlower(level, blockPos)) {
					addFlowerPos(blockPos.immutable());
				}
			} else {
                this.flowerData.resetIterator(queen, beeHousing);
			}
		}
	}

	public boolean hasFlowers() {
		return !this.flowerCoords.isEmpty();
	}

	public boolean needsSync() {
		boolean returnVal = this.needsSync;
        this.needsSync = false;
		return returnVal;
	}

	public void onNewQueen(IBee queen, IBeeHousing housing) {
		if (this.flowerData != null) {
			IGenome genome = queen.getGenome();
			IFlowerType flowerType = genome.getActiveValue(BeeChromosomes.FLOWER_TYPE);
			if (this.flowerData.flowerType != flowerType || !this.flowerData.territory.equals(genome.getActiveValue(BeeChromosomes.TERRITORY))) {
                this.flowerData = new FlowerData(queen, housing);
                this.flowerCoords.clear();
                this.flowers.clear();
			}
		}
	}

	public List<BlockPos> getFlowerCoords() {
		return Collections.unmodifiableList(this.flowerCoords);
	}

	public List<BlockState> getFlowers(Level level) {
		if (this.flowers.isEmpty() && !this.flowerCoords.isEmpty()) {
			for (BlockPos flowerCoord : this.flowerCoords) {
				BlockState blockState = level.getBlockState(flowerCoord);
                this.flowers.add(blockState);
			}
		}
		return Collections.unmodifiableList(this.flowers);
	}

	public void addFlowerPos(BlockPos blockPos) {
        this.flowerCoords.add(blockPos);
        this.flowers.clear();
        this.needsSync = true;
	}

	public void forceLookForFlowers(IBee queen, IBeeHousing housing) {
		if (this.flowerData != null) {
            this.flowerCoords.clear();
            this.flowers.clear();
            this.flowerData.resetIterator(queen, housing);
			Level level = housing.getWorldObj();
			while (this.flowerData.areaIterator.hasNext()) {
				BlockPos.MutableBlockPos blockPos = this.flowerData.areaIterator.next();
				if (this.flowerData.flowerType.isAcceptableFlower(level, blockPos)) {
					addFlowerPos(blockPos.immutable());
				}
			}
		}
	}

	@Override
	public void read(CompoundTag compoundNBT) {
		if (!compoundNBT.contains(NBT_KEY)) {
			return;
		}

		CompoundTag hasFlowerCacheNBT = compoundNBT.getCompound(NBT_KEY);
        this.flowerCoords.clear();
		if (hasFlowerCacheNBT.contains(NBT_KEY_FLOWERS)) {
			int[] flowersList = hasFlowerCacheNBT.getIntArray(NBT_KEY_FLOWERS);
			if (flowersList.length % 3 == 0) {
				int flowerCount = flowersList.length / 3;

                this.flowerCoords.ensureCapacity(flowerCount);

				for (int i = 0; i < flowerCount; i++) {
					int index = i * 3;
					BlockPos flowerPos = new BlockPos(flowersList[index], flowersList[index + 1], flowersList[index + 2]);
                    this.flowerCoords.add(flowerPos);
				}
                this.needsSync = true;
			}
		}
        this.flowers.clear();
	}

	@Override
	public CompoundTag write(CompoundTag CompoundNBT) {
		CompoundTag hasFlowerCacheNBT = new CompoundTag();

		if (!this.flowerCoords.isEmpty()) {
			int[] flowersList = new int[this.flowerCoords.size() * 3];
			int i = 0;
			for (BlockPos flowerPos : this.flowerCoords) {
				flowersList[i] = flowerPos.getX();
				flowersList[i + 1] = flowerPos.getY();
				flowersList[i + 2] = flowerPos.getZ();
				i += 3;
			}

			hasFlowerCacheNBT.putIntArray(NBT_KEY_FLOWERS, flowersList);
		}

		CompoundNBT.put(NBT_KEY, hasFlowerCacheNBT);
		return CompoundNBT;
	}

	public void writeData(FriendlyByteBuf data) {
		int size = this.flowerCoords.size();
		data.writeVarInt(size);
		if (size > 0) {
			for (BlockPos pos : this.flowerCoords) {
				data.writeVarInt(pos.getX());
				data.writeVarInt(pos.getY());
				data.writeVarInt(pos.getZ());
			}
		}
	}

	public void readData(FriendlyByteBuf data) {
        this.flowerCoords.clear();
        this.flowers.clear();

		int size = data.readVarInt();
		while (size > 0) {
			BlockPos pos = new BlockPos(data.readVarInt(), data.readVarInt(), data.readVarInt());
            this.flowerCoords.add(pos);
			size--;
		}
	}
}
