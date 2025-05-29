package forestry.core.circuits;

import forestry.api.IForestryApi;
import forestry.api.circuits.ICircuit;
import forestry.api.circuits.ICircuitBoard;
import forestry.api.circuits.ICircuitLayout;
import forestry.core.features.CoreItems;
import forestry.core.items.ItemForestry;
import forestry.core.items.definitions.IColoredItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCircuitBoard extends ItemForestry implements IColoredItem {
	private final EnumCircuitBoardType type;

	public ItemCircuitBoard(EnumCircuitBoardType type) {
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
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
		ICircuitBoard circuitboard = IForestryApi.INSTANCE.getCircuitManager().getCircuitBoard(stack);
		if (circuitboard != null) {
			circuitboard.addTooltip(list);
		}
	}

	public static ItemStack createCircuitboard(EnumCircuitBoardType type, @Nullable ICircuitLayout layout, ICircuit[] circuits) {
		CompoundTag compoundNBT = new CompoundTag();
		new CircuitBoard(type, layout, circuits).write(compoundNBT);
		ItemStack stack = CoreItems.CIRCUITBOARDS.stack(type, 1);
		stack.setTag(compoundNBT);
		return stack;
	}

	public ItemStack get(EnumCircuitBoardType type) {
		return CoreItems.CIRCUITBOARDS.stack(type, 1);
	}
}
