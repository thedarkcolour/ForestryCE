package forestry.core.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class TileUtil {
	public static boolean isUsableByPlayer(Player player, BlockEntity tile) {
		BlockPos pos = tile.getBlockPos();
		Level world = tile.getLevel();

		return !tile.isRemoved() &&
			getTile(world, pos) == tile &&
			player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
	}

	/**
	 * Returns the tile at the specified position, returns null if it is the wrong type or does not exist.
	 * Avoids creating new tile entities when using a ChunkCache (off the main thread).
	 */
	@Nullable
	public static BlockEntity getTile(BlockGetter level, BlockPos pos) {
		return level.getExistingBlockEntity(pos);
	}

	/**
	 * Returns the tile of the specified class, returns null if it is the wrong type or does not exist.
	 * Avoids creating new tile entities when using a ChunkCache (off the main thread).
	 */
	@Nullable
	public static <T> T getTile(BlockGetter world, BlockPos pos, Class<T> tileClass) {
		BlockEntity tileEntity = getTile(world, pos);
		if (tileClass.isInstance(tileEntity)) {
			return tileClass.cast(tileEntity);
		} else {
			return null;
		}
	}

	@Nullable
	public static <T> T getTile(BlockEntity tileEntity, Class<T> tileClass) {
		if (tileClass.isInstance(tileEntity)) {
			return tileClass.cast(tileEntity);
		} else {
			return null;
		}
	}

	public static <T> void actOnTile(LevelReader world, BlockPos pos, Class<T> tileClass, Consumer<T> tileAction) {
		T tile = getTile(world, pos, tileClass);
		if (tile != null) {
			tileAction.accept(tile);
		}
	}

	@Nullable
	public static IItemHandler getInventoryFromTile(@Nullable BlockEntity tile, @Nullable Direction side) {
		if (tile == null) {
			return null;
		}

		LazyOptional<IItemHandler> itemCap = tile.getCapability(ForgeCapabilities.ITEM_HANDLER, side);
		if (itemCap.isPresent()) {
			return itemCap.orElse(null);
		}

		if (tile instanceof WorldlyContainer worldly) {
			return new SidedInvWrapper(worldly, side);
		}

		if (tile instanceof Container container) {
			return new InvWrapper(container);
		}

		return null;
	}

	public static <T> LazyOptional<T> getInterface(Level world, BlockPos pos, Capability<T> capability, @Nullable Direction facing) {
		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (tileEntity == null) {
			return LazyOptional.empty();
		}
		return tileEntity.getCapability(capability, facing);
	}
}
