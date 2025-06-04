package forestry.core.circuits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.circuits.CircuitLayout;
import forestry.api.circuits.ICircuit;
import forestry.api.circuits.ICircuitBoard;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CircuitBoard implements ICircuitBoard {
	public static final Codec<CircuitBoard> CODEC = RecordCodecBuilder.create(inst -> inst.group(
		StringRepresentable.fromEnum(EnumCircuitBoardType::values).fieldOf("").forGetter(b -> b.type),
		CircuitLayout.CODEC.optionalFieldOf("layout").forGetter(b -> Optional.ofNullable(b.layout)),
		ICircuit.CODEC.listOf().fieldOf("circuits").forGetter(b -> b.circuits)
	).apply(inst, CircuitBoard::new));

	private final EnumCircuitBoardType type;
	@Nullable
	private final CircuitLayout layout;
	private final List<ICircuit> circuits;

	public CircuitBoard(EnumCircuitBoardType type, Optional<CircuitLayout> layout, List<ICircuit> circuits) {
		this.type = type;
		this.layout = layout.orElse(null);
		this.circuits = circuits;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getPrimaryColor() {
		return this.type.getPrimaryColor();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getSecondaryColor() {
		return this.type.getSecondaryColor();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addTooltip(List<Component> list) {
		if (this.layout != null) {
			list.add(Component.literal(this.layout.getUsage().getString() + ":").withStyle(ChatFormatting.GOLD));
			List<Component> extendedTooltip = new ArrayList<>();
			for (ICircuit circuit : this.circuits) {
				if (circuit != null) {
					circuit.addTooltip(extendedTooltip);
				}
			}

			if (Screen.hasShiftDown() || extendedTooltip.size() <= 4) {
				list.addAll(extendedTooltip);
			} else {
				list.add(Component.literal("<").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.GRAY)
					.append(Component.translatable("for.gui.tooltip.tmi"))
					.append(Component.literal(">")));
			}
		} else {
			int socketCount = this.type.getSockets();
			String localizationKey = "item.forestry.circuit_board.tooltip." + (socketCount == 1 ? "singular" : "plural");
			list.add(Component.translatable(localizationKey, this.type.getSockets()).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public void onInsertion(Object tile) {
		for (int i = 0; i < this.circuits.size(); i++) {
			ICircuit circuit = this.circuits.get(i);
			if (circuit == null) {
				continue;
			}
			circuit.onInsertion(i, tile);
		}
	}

	@Override
	public void onLoad(Object tile) {
		for (int i = 0; i < this.circuits.size(); i++) {
			ICircuit circuit = this.circuits.get(i);
			if (circuit == null) {
				continue;
			}
			circuit.onLoad(i, tile);
		}
	}

	@Override
	public void onRemoval(Object tile) {
		for (int i = 0; i < this.circuits.size(); i++) {
			ICircuit circuit = this.circuits.get(i);
			if (circuit == null) {
				continue;
			}
			circuit.onRemoval(i, tile);
		}
	}

	@Override
	public void onTick(Object tile) {
		for (int i = 0; i < this.circuits.size(); i++) {
			ICircuit circuit = this.circuits.get(i);
			if (circuit == null) {
				continue;
			}
			circuit.onTick(i, tile);
		}
	}

	@Override
	public List<ICircuit> getCircuits() {
		return this.circuits;
	}

	@Nullable
	@Override
	public ResourceLocation getSocketType() {
		if (this.layout == null) {
			return null;
		}
		return this.layout.socketType();
	}
}
