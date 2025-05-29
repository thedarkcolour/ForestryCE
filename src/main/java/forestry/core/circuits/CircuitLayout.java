package forestry.core.circuits;

import forestry.api.circuits.ICircuitLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class CircuitLayout implements ICircuitLayout {
	private final String uid;
	private final ResourceLocation socketType;

	public CircuitLayout(String uid, ResourceLocation socketType) {
		this.uid = uid;
		this.socketType = socketType;
	}

	@Override
	public String getId() {
		return this.uid;
	}

	@Override
	public Component getName() {
		return Component.translatable("circuit.layout." + this.uid);
	}

	@Override
	public MutableComponent getUsage() {
		return Component.translatable("circuit.layout." + this.uid + ".usage");
	}

	@Override
	public ResourceLocation getSocketType() {
		return this.socketType;
	}
}
