package forestry.core.network.packets;

import forestry.api.modules.IForestryPacketClient;
import forestry.core.fluids.ITankManager;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.FluidStack;

public record PacketTankLevelUpdate(BlockPos pos, int tankIndex, FluidStack contents) implements IForestryPacketClient {
	public PacketTankLevelUpdate(ILiquidTankTile tileEntity, int tankIndex, FluidStack contents) {
		this(tileEntity.getCoordinates(), tankIndex, contents);
	}

	@Override
	public ResourceLocation id() {
		return PacketIdClient.TANK_LEVEL_UPDATE;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(this.pos);
		buffer.writeVarInt(this.tankIndex);
		buffer.writeFluidStack(this.contents);
	}

	public static PacketTankLevelUpdate decode(FriendlyByteBuf buffer) {
		return new PacketTankLevelUpdate(buffer.readBlockPos(), buffer.readVarInt(), buffer.readFluidStack());
	}

	public static void handle(PacketTankLevelUpdate msg, Player player) {
		TileUtil.actOnTile(player.level(), msg.pos, ILiquidTankTile.class, tile -> {
			ITankManager tankManager = tile.getTankManager();
			tankManager.processTankUpdate(msg.tankIndex, msg.contents);
		});
	}
}
