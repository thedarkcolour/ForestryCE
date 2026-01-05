package forestry.core.utils;

import com.google.common.base.Preconditions;
import forestry.api.IForestryApi;
import forestry.api.climate.ClimateState;
import forestry.api.climate.IClimateProvider;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.modules.IForestryPacketClient;
import forestry.api.modules.IForestryPacketServer;
import forestry.core.network.IStreamable;
import forestry.core.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class NetworkUtil {
	public static void sendNetworkPacket(IForestryPacketClient packet, BlockPos pos, Level level) {
		NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), packet);
	}

	public static void sendToPlayer(IForestryPacketClient packet, ServerPlayer player) {
		NetworkHandler.CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}

	public static void sendToAllPlayers(IForestryPacketClient packet) {
		if (ServerLifecycleHooks.getCurrentServer() != null) {
			NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
		}
	}

	// Used for Streamable to prepare FriendlyByteBuf for sending over the network
	public static void writePayloadBuffer(FriendlyByteBuf buffer, Consumer<FriendlyByteBuf> dataWriter) {
		// write a placeholder value for the number of bytes, keeping its index for replacing later
		int dataBytesIndex = buffer.writerIndex();
		buffer.writeInt(0);
		// write data bytes
		dataWriter.accept(buffer);
		// replace placeholder with length of data bytes, not including length integer
		int numDataBytes = buffer.writerIndex() - dataBytesIndex - 4;
		buffer.setInt(dataBytesIndex, numDataBytes);
	}

	// Used for Streamable to read FriendlyByteBuf for receiving from the network
	public static FriendlyByteBuf readPayloadBuffer(FriendlyByteBuf buffer) {
		return new FriendlyByteBuf(buffer.readBytes(buffer.readInt()));
	}

	public static void sendToServer(IForestryPacketServer packet) {
		NetworkHandler.CHANNEL.sendToServer(packet);
	}

	public static void writeItemStacks(FriendlyByteBuf buffer, List<ItemStack> itemStacks) {
		buffer.writeVarInt(itemStacks.size());
		for (ItemStack stack : itemStacks) {
			buffer.writeItem(stack);
		}
	}

	public static NonNullList<ItemStack> readItemStacks(FriendlyByteBuf buffer) {
		int stackCount = buffer.readVarInt();
		NonNullList<ItemStack> itemStacks = NonNullList.create();
		for (int i = 0; i < stackCount; i++) {
			itemStacks.add(buffer.readItem());
		}
		return itemStacks;
	}

	public static void writeInventory(FriendlyByteBuf buffer, Container inventory) {
		int size = inventory.getContainerSize();
		buffer.writeVarInt(size);

		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getItem(i);
			buffer.writeItem(stack);
		}
	}

	public static void readInventory(FriendlyByteBuf buffer, Container inventory) {
		int size = buffer.readVarInt();

		for (int i = 0; i < size; i++) {
			ItemStack stack = buffer.readItem();
			inventory.setItem(i, stack);
		}
	}

	// Assumes Enum.values().length < Byte.MAX_VALUE
	public static <T extends Enum<T>> void writeEnum(FriendlyByteBuf buffer, T enumValue) {
		buffer.writeByte(enumValue.ordinal());
	}

	public static <T extends Enum<T>> T readEnum(FriendlyByteBuf buffer, T[] enumValues) {
		Preconditions.checkArgument(enumValues.length < Byte.MAX_VALUE);
		return enumValues[buffer.readByte()];
	}

	public static void writeStreamable(FriendlyByteBuf buffer, @Nullable IStreamable streamable) {
		if (streamable != null) {
			buffer.writeBoolean(true);
			streamable.writeData(buffer);
		} else {
			buffer.writeBoolean(false);
		}
	}

	@Nullable
	public static <T extends IStreamable> T readStreamable(FriendlyByteBuf buffer, Function<FriendlyByteBuf, T> factory) {
		if (buffer.readBoolean()) {
			return factory.apply(buffer);
		}
		return null;
	}

	public static <T extends IStreamable> void writeStreamables(FriendlyByteBuf buffer, @Nullable List<T> streamables) {
		if (streamables == null) {
			buffer.writeVarInt(0);
		} else {
			buffer.writeVarInt(streamables.size());
			for (IStreamable streamable : streamables) {
				writeStreamable(buffer, streamable);
			}
		}
	}

	public static <T extends IStreamable> void readStreamables(FriendlyByteBuf buffer, List<T> outputList, Function<FriendlyByteBuf, T> factory) {
		outputList.clear();
		int length = buffer.readVarInt();
		if (length > 0) {
			for (int i = 0; i < length; i++) {
				T streamable = readStreamable(buffer, factory);
				outputList.add(streamable);
			}
		}
	}

	public static void writeClimateState(FriendlyByteBuf buffer, @Nullable IClimateProvider climateState) {
		if (climateState != null) {
			buffer.writeBoolean(true);
			buffer.writeByte(climateState.temperature().ordinal());
			buffer.writeByte(climateState.humidity().ordinal());
		} else {
			buffer.writeBoolean(false);
		}
	}

	public static void writeClimateState(FriendlyByteBuf buffer, TemperatureType temperature, HumidityType humidity) {
		buffer.writeBoolean(true);
		buffer.writeByte(temperature.ordinal());
		buffer.writeByte(humidity.ordinal());
	}

	public static IClimateProvider readClimateState(FriendlyByteBuf buffer) {
		if (buffer.readBoolean()) {
			return new ClimateState(TemperatureType.VALUES.get(buffer.readByte()), HumidityType.VALUES.get(buffer.readByte()));
		} else {
			return IForestryApi.INSTANCE.getClimateManager().createDummyClimateProvider();
		}
	}

	public static void writeBlockState(FriendlyByteBuf buffer, BlockState state) {
		buffer.writeId(Block.BLOCK_STATE_REGISTRY, state);
	}

	public static BlockState readBlockState(FriendlyByteBuf buffer) {
		return buffer.readById(Block.BLOCK_STATE_REGISTRY);
	}

	public static void writeDirection(FriendlyByteBuf buffer, Direction direction) {
		buffer.writeByte(direction.ordinal());
	}

	public static Direction readDirection(FriendlyByteBuf buffer) {
		byte ordinal = buffer.readByte();
		if (ordinal > 5 || ordinal < 0) {
			throw new IllegalArgumentException("Tried to deserialize Direction enum from network, but got invalid ordinal: " + ordinal);
		}
		return Direction.VALUES[ordinal];
	}

	public static void writeShortArray(FriendlyByteBuf buffer, short[] array) {
		buffer.writeVarInt(array.length);
		for (short value : array) {
			buffer.writeShort(value);
		}
	}

	public static short[] readShortArray(FriendlyByteBuf buffer) {
		short[] array = new short[buffer.readVarInt()];
		for (int i = 0; i < array.length; i++) {
			array[i] = buffer.readShort();
		}
		return array;
	}
}
