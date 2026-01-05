package forestry.core.fluids;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.core.network.IStreamable;
import forestry.core.network.packets.PacketTankLevelUpdate;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.tiles.IRenderableTile;
import forestry.core.utils.NetworkUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TankManager implements ITankManager, ITankUpdateHandler, IStreamable, INbtWritable, INbtReadable {
	private final List<StandardTank> tanks = new ArrayList<>();

	// for container updates, keeps track of the fluids known to each client (container)
	private final Table<AbstractContainerMenu, Integer, FluidStack> prevFluidStacks = HashBasedTable.create();

	// tank tile updates, for blocks that show fluid levels on the outside
	@Nullable
	private final ILiquidTankTile tile;

	public TankManager() {
		this.tile = null;
	}

	public TankManager(ILiquidTankTile tile, StandardTank... tanks) {
		this.tile = tile;

		for (StandardTank tank : tanks) {
			add(tank);
		}
	}

	public void add(StandardTank tank) {
		this.tanks.add(tank);
		int index = this.tanks.indexOf(tank);
		tank.setTankUpdateHandler(this);
		tank.setTankIndex(index);
	}

	@Override
	public CompoundTag write(CompoundTag data) {
		ListTag tagList = new ListTag();
		for (byte slot = 0; slot < this.tanks.size(); slot++) {
			StandardTank tank = this.tanks.get(slot);
			if (!tank.getFluid().isEmpty()) {
				CompoundTag tag = new CompoundTag();
				tag.putByte("tank", slot);
				tank.writeToNBT(tag);
				tagList.add(tag);
			}
		}
		data.put("tanks", tagList);
		return data;
	}

	@Override
	public void read(CompoundTag data) {
		for (Tag tag : data.getList("tanks", Tag.TAG_COMPOUND)) {
			CompoundTag compound = (CompoundTag) tag;
			int slot = compound.getByte("tank");
			if (slot >= 0 && slot < this.tanks.size()) {
				StandardTank tank = this.tanks.get(slot);
				tank.readFromNBT(compound);
				updateTankLevels(tank);
			}
		}
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		for (StandardTank tank : this.tanks) {
			tank.writeData(data);
		}
	}

	@Override
	public void readData(FriendlyByteBuf data) {
		for (StandardTank tank : this.tanks) {
			tank.readData(data);
		}
	}

	@Override
	public void sendAllTanks(AbstractContainerMenu container, ServerPlayer player) {
		for (StandardTank tank : this.tanks) {
			sendTankUpdate(container, player, tank);
		}
	}

	@Override
	public void onClosed(AbstractContainerMenu container) {
		for (StandardTank tank : this.tanks) {
            this.prevFluidStacks.remove(container, tank.getTankIndex());
		}
	}

	@Override
	public void broadcastChanges(AbstractContainerMenu container, ServerPlayer players) {
		for (StandardTank tank : this.tanks) {
			sendTankUpdate(container, players, tank.getTankIndex());
		}
	}

	private void sendTankUpdate(AbstractContainerMenu container, ServerPlayer player, int tankIndex) {
		StandardTank tank = this.tanks.get(tankIndex);
		if (tank == null) {
			return;
		}

		FluidStack fluidStack = tank.getFluid();
		FluidStack prev = this.prevFluidStacks.get(container, tankIndex);
		if (prev == null) {
			prev = FluidStack.EMPTY;
		}
		if (FluidHelper.areFluidStacksEqual(fluidStack, prev)) {
			return;
		}

		sendTankUpdate(container, player, tank);
	}

	private void sendTankUpdate(AbstractContainerMenu container, ServerPlayer player, StandardTank tank) {
		if (this.tile != null) {
			int tankIndex = tank.getTankIndex();
			FluidStack fluid = tank.getFluid();
			NetworkUtil.sendToPlayer(new PacketTankLevelUpdate(this.tile, tankIndex, fluid), player);

			if (fluid.isEmpty()) {
                this.prevFluidStacks.remove(container, tankIndex);
			} else {
                this.prevFluidStacks.put(container, tankIndex, fluid.copy());
			}
		}
	}

	@Override
	public void processTankUpdate(int tankIndex, @Nullable FluidStack contents) {
		if (tankIndex < 0 || tankIndex > this.tanks.size()) {
			return;
		}
		StandardTank tank = this.tanks.get(tankIndex);
		tank.setFluid(contents);
	}

	@Nullable
	@Override
	public IFluidTank getTank(int tankIndex) {
		return this.tanks.get(tankIndex);
	}

	@Override
	public int getTanks() {
		return this.tanks.size();
	}

	@Override
	public FluidStack getFluidInTank(int tankIndex) {
		IFluidTank tank = getTank(tankIndex);
		if (tank == null) {
			return FluidStack.EMPTY;
		}
		return tank.getFluid();
	}

	@Override
	public int getTankCapacity(int tankIndex) {
		IFluidTank tank = getTank(tankIndex);
		if (tank == null) {
			return 0;
		}
		return tank.getCapacity();
	}

	@Override
	public boolean isFluidValid(int tankIndex, @Nonnull FluidStack stack) {
		IFluidTank tank = getTank(tankIndex);
		if (tank == null) {
			return false;
		}
		return tank.isFluidValid(stack);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		for (StandardTank tank : this.tanks) {
			if (tankAcceptsFluid(tank, resource)) {
				return fill(tank.getTankIndex(), resource, action);
			}
		}

		return 0;
	}

	public int fill(int tankIndex, FluidStack resource, FluidAction action) {
		if (tankIndex < 0 || tankIndex >= this.tanks.size()) {
			return 0;
		}

		StandardTank tank = this.tanks.get(tankIndex);
		if (!tank.canFill()) {
			return 0;
		}

		return tank.fill(resource, action);
	}

	@Override
	public void updateTankLevels(StandardTank tank) {
		if (!(this.tile instanceof IRenderableTile)) {
			return;
		}

		Level world = this.tile.getWorldObj();
		if (world == null || world.isClientSide)
			return;

		int tankIndex = tank.getTankIndex();
		PacketTankLevelUpdate tankLevelUpdate = new PacketTankLevelUpdate(this.tile, tankIndex, tank.getFluid());
		NetworkUtil.sendNetworkPacket(tankLevelUpdate, this.tile.getCoordinates(), world);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		for (StandardTank tank : this.tanks) {
			if (tankCanDrain(tank)) {
				return drain(tank.getTankIndex(), maxDrain, action);
			}
		}
		return FluidStack.EMPTY;
	}

	public FluidStack drain(int tankIndex, int maxDrain, FluidAction action) {
		if (tankIndex < 0 || tankIndex >= this.tanks.size()) {
			return FluidStack.EMPTY;
		}

		StandardTank tank = this.tanks.get(tankIndex);
		if (!tank.canDrain()) {
			return FluidStack.EMPTY;
		}

		return tank.drain(maxDrain, action);
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		for (StandardTank tank : this.tanks) {
			if (tankCanDrainFluid(tank, resource)) {
				return drain(tank.getTankIndex(), resource.getAmount(), action);
			}
		}
		return FluidStack.EMPTY;
	}

	@Nullable
	public FluidStack getFluid(int tankIndex) {
		return this.tanks.get(tankIndex).getFluid();
	}

	@Override
	public boolean canFillFluidType(FluidStack fluidStack) {
		for (StandardTank tank : this.tanks) {
			if (tank.isFluidValid(fluidStack)) {
				return true;
			}
		}

		return false;
	}

	private static boolean tankAcceptsFluid(StandardTank tank, FluidStack fluidStack) {
		return tank.canFill() &&
			tank.fill(fluidStack, FluidAction.SIMULATE) > 0;
	}

	private static boolean tankCanDrain(StandardTank tank) {
		if (!tank.canDrain()) {
			return false;
		}
		FluidStack drained = tank.drain(1, FluidAction.SIMULATE);
		return !drained.isEmpty() && drained.getAmount() > 0;
	}

	private static boolean tankCanDrainFluid(StandardTank tank, FluidStack fluidStack) {
		return ForestryFluids.areEqual(tank.getFluidType(), fluidStack) &&
			tankCanDrain(tank);
	}
}
