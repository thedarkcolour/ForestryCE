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
import forestry.core.network.packets.PacketGuiSelectRequest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.connection.ConnectionType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class NetworkUtil {
	public static void sendToPlayersTrackingPos(IForestryPacketClient packet, BlockPos pos, ServerLevel level) {
		PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(pos), packet);
	}

	// Used for Streamable to prepare FriendlyByteBuf for sending over the network
	public static void writePayloadBuffer(RegistryFriendlyByteBuf buffer, Consumer<RegistryFriendlyByteBuf> dataWriter) {
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
	public static RegistryFriendlyByteBuf readPayloadBuffer(RegistryFriendlyByteBuf buffer) {
		return new RegistryFriendlyByteBuf(buffer.readBytes(buffer.readInt()), buffer.registryAccess(), ConnectionType.NEOFORGE);
	}

	public static void writeItemStacks(RegistryFriendlyByteBuf buffer, List<ItemStack> itemStacks) {
		buffer.writeVarInt(itemStacks.size());
		for (ItemStack stack : itemStacks) {
			ItemStack.STREAM_CODEC.encode(buffer, stack);
		}
	}

	public static List<ItemStack> readItemStacks(RegistryFriendlyByteBuf buffer) {
		int stackCount = buffer.readVarInt();
		ArrayList<ItemStack> itemStacks = new ArrayList<>(stackCount);
		for (int i = 0; i < stackCount; i++) {
			itemStacks.add(ItemStack.STREAM_CODEC.decode(buffer));
		}
		return itemStacks;
	}

	public static void writeInventory(RegistryFriendlyByteBuf buffer, Container inventory) {
		int size = inventory.getContainerSize();
		buffer.writeVarInt(size);

		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getItem(i);
			ItemStack.STREAM_CODEC.encode(buffer, stack);
		}
	}

	public static void readInventory(RegistryFriendlyByteBuf buffer, Container inventory) {
		int size = buffer.readVarInt();

		for (int i = 0; i < size; i++) {
			ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
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
	public static <T extends IStreamable> T readStreamable(RegistryFriendlyByteBuf buffer, Function<RegistryFriendlyByteBuf, T> factory) {
		if (buffer.readBoolean()) {
			return factory.apply(buffer);
		}
		return null;
	}

	public static <T extends IStreamable> void writeStreamables(RegistryFriendlyByteBuf buffer, @Nullable List<T> streamables) {
		if (streamables == null) {
			buffer.writeVarInt(0);
		} else {
			buffer.writeVarInt(streamables.size());
			for (IStreamable streamable : streamables) {
				writeStreamable(buffer, streamable);
			}
		}
	}

	public static <T extends IStreamable> void readStreamables(RegistryFriendlyByteBuf buffer, List<T> outputList, Function<RegistryFriendlyByteBuf, T> factory) {
		outputList.clear();
		int length = buffer.readVarInt();
		if (length > 0) {
			for (int i = 0; i < length; i++) {
				T streamable = readStreamable(buffer, factory);
				outputList.add(streamable);
			}
		}
	}

	public static void writeClimateState(RegistryFriendlyByteBuf buffer, @Nullable IClimateProvider climateState) {
		if (climateState != null) {
			buffer.writeBoolean(true);
			buffer.writeByte(climateState.temperature().ordinal());
			buffer.writeByte(climateState.humidity().ordinal());
		} else {
			buffer.writeBoolean(false);
		}
	}

	public static void writeClimateState(RegistryFriendlyByteBuf buffer, TemperatureType temperature, HumidityType humidity) {
		buffer.writeBoolean(true);
		buffer.writeByte(temperature.ordinal());
		buffer.writeByte(humidity.ordinal());
	}

	public static IClimateProvider readClimateState(RegistryFriendlyByteBuf buffer) {
		if (buffer.readBoolean()) {
			return new ClimateState(TemperatureType.VALUES.get(buffer.readByte()), HumidityType.VALUES.get(buffer.readByte()));
		} else {
			return IForestryApi.INSTANCE.getClimateManager().createDummyClimateProvider();
		}
	}

	public static void writeBlockState(RegistryFriendlyByteBuf buffer, BlockState state) {
		buffer.writeById(Block.BLOCK_STATE_REGISTRY::getId, state);
	}

	public static BlockState readBlockState(RegistryFriendlyByteBuf buffer) {
		return buffer.readById(Block.BLOCK_STATE_REGISTRY::byId);
	}

	public static void writeDirection(RegistryFriendlyByteBuf buffer, Direction direction) {
		buffer.writeByte(direction.ordinal());
	}

	public static Direction readDirection(RegistryFriendlyByteBuf buffer) {
		byte ordinal = buffer.readByte();
		if (ordinal > 5 || ordinal < 0) {
			throw new IllegalArgumentException("Tried to deserialize Direction enum from network, but got invalid ordinal: " + ordinal);
		}
		return Direction.VALUES[ordinal];
	}

	public static void writeShortArray(RegistryFriendlyByteBuf buffer, short[] array) {
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

	public static void sendRecipeClick(int mouseButton, int recipeIndex) {
        IForestryPacketServer packet = new PacketGuiSelectRequest(mouseButton, recipeIndex);
        PacketDistributor.sendToServer(packet);
    }
}
