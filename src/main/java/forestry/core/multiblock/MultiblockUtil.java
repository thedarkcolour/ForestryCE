package forestry.core.multiblock;

import forestry.api.multiblock.IMultiblockComponent;
import forestry.api.multiblock.IMultiblockController;
import forestry.api.multiblock.IMultiblockLogic;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;

public class MultiblockUtil {
	@Nullable
	public static <C extends IMultiblockComponent> C getComponent(BlockGetter world, BlockPos pos, Class<C> componentClass) {
		return TileUtil.getTile(world, pos, componentClass);
	}

	@Nullable
	public static <C extends IMultiblockComponent, L extends IMultiblockLogic> L getLogic(BlockGetter world, BlockPos pos, Class<C> componentClass) {
		C component = getComponent(world, pos, componentClass);
		if (component == null) {
			return null;
		}
		return (L) component.getMultiblockLogic();
	}

	@Nullable
	public static <C extends IMultiblockComponent, L extends IMultiblockLogic, M extends IMultiblockController> M getController(BlockGetter world, BlockPos pos, Class<C> componentClass) {
		L logic = getLogic(world, pos, componentClass);
		if (logic == null || !logic.isConnected()) {
			return null;
		}
		return (M) logic.getController();
	}

}
