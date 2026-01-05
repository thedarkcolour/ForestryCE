package forestry.apiculture;

import forestry.api.apiculture.IBeekeepingLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.List;

public enum FakeBeekeepingLogic implements IBeekeepingLogic {
	INSTANCE;

	@Override
	public boolean canWork() {
		return false;
	}

	@Override
	public void doWork() {
	}

	@Override
	public void clearCachedValues() {
	}

	@Override
	public void syncToClient() {
	}

	@Override
	public void syncToClient(ServerPlayer player) {
	}

	@Override
	public int getBeeProgressPercent() {
		return 0;
	}

	@Override
	public boolean canDoBeeFX() {
		return false;
	}

	@Override
	public void doBeeFX() {
	}

	@Override
	public List<BlockPos> getFlowerPositions() {
		return Collections.emptyList();
	}

	@Override
	public void read(CompoundTag nbt) {
	}

	@Override
	public CompoundTag write(CompoundTag nbt) {
		return nbt;
	}
}
