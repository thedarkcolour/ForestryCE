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
package forestry.core.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.fluids.FluidStack;

import forestry.core.fluids.ITankManager;
import forestry.api.modules.IForestryPacketClient;
import forestry.core.network.PacketIdClient;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.tiles.TileUtil;

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
		buffer.writeBlockPos(pos);
		buffer.writeVarInt(tankIndex);
		buffer.writeFluidStack(contents);
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
