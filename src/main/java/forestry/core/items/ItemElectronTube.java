package forestry.core.items;

import forestry.api.IForestryApi;
import forestry.api.circuits.CircuitLayout;
import forestry.api.circuits.ICircuit;
import forestry.api.circuits.ICircuitManager;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class ItemElectronTube extends ItemOverlay {
	public ItemElectronTube(ItemOverlay.IOverlayInfo type) {
		super(type);
	}

	@Override
	public void appendHoverText(ItemStack itemstack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, ctx, list, flag);
		ArrayList<Pair<CircuitLayout, ICircuit>> circuits = getCircuits(itemstack);
		if (!circuits.isEmpty()) {
			for (var entry : circuits) {
				list.add(entry.left().getUsage().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE));
				entry.right().addTooltip(list);
			}
		} else {
			list.add(Component.literal("<")
				.append(Component.translatable("for.gui.noeffect")
					.append(">").withStyle(ChatFormatting.GRAY)));
		}
	}

	private static ArrayList<Pair<CircuitLayout, ICircuit>> getCircuits(ItemStack stack) {
		ArrayList<Pair<CircuitLayout, ICircuit>> circuits = new ArrayList<>();
		ICircuitManager manager = IForestryApi.INSTANCE.getCircuitManager();

		for (CircuitLayout layout : manager.getLayouts()) {
			ICircuit circuit = manager.getCircuit(layout, stack);
			if (circuit != null) {
				circuits.add(Pair.of(layout, circuit));
			}
		}

		return circuits;
	}
}
