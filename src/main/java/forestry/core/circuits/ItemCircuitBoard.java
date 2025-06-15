package forestry.core.circuits;

import forestry.api.IForestryApi;
import forestry.api.circuits.CircuitLayout;
import forestry.api.circuits.ICircuit;
import forestry.api.circuits.ICircuitBoard;
import forestry.core.features.CoreDataComponents;
import forestry.core.features.CoreItems;
import forestry.core.items.ItemForestry;
import forestry.core.items.definitions.IColoredItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ItemCircuitBoard extends ItemForestry implements IColoredItem {
	private final EnumCircuitBoardType type;

	public ItemCircuitBoard(EnumCircuitBoardType type) {
		super(new Properties().component(CoreDataComponents.CIRCUIT_BOARD, new CircuitBoard(type, Optional.empty(), List.of())));
		this.type = type;
	}

	public EnumCircuitBoardType getType() {
		return this.type;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getColorFromItemStack(ItemStack itemstack, int tintIndex) {
		if (tintIndex == 0) {
			return this.type.getPrimaryColor();
		} else {
			return this.type.getSecondaryColor();
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable TooltipContext ctx, List<Component> list, TooltipFlag flag) {
		IForestryApi.INSTANCE.getCircuitManager();
		ICircuitBoard board = stack.get(CoreDataComponents.CIRCUIT_BOARD);
		if (board != null) {
			board.addTooltip(list);
		}
	}

	public static ItemStack createCircuitboard(EnumCircuitBoardType type, CircuitLayout layout, List<ICircuit> circuits) {
		ItemStack stack = CoreItems.CIRCUITBOARDS.stack(type, 1);
		stack.set(CoreDataComponents.CIRCUIT_BOARD, new CircuitBoard(type, Optional.of(layout), circuits));
		return stack;
	}

	public ItemStack get(EnumCircuitBoardType type) {
		return CoreItems.CIRCUITBOARDS.stack(type, 1);
	}
}
