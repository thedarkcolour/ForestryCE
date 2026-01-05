package forestry.core.circuits;

import forestry.api.circuits.ICircuit;
import forestry.core.utils.Translator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class Circuit implements ICircuit {
	private final String uid;

	protected Circuit(String uid) {
		this.uid = uid;
	}

	@Override
	public String getId() {
		return "forestry." + this.uid;
	}

	@Override
	public String getTranslationKey() {
		return "for.circuit." + this.uid;
	}

	@Override
	public void addTooltip(List<Component> list) {
		list.add(Component.translatable(getTranslationKey()).withStyle(ChatFormatting.GRAY));

		int i = 1;
		while (true) {
			String unlocalizedDescription = getTranslationKey() + ".description." + i;
			if (!Translator.canTranslateToLocal(unlocalizedDescription)) {
				break;
			}
			list.add(Component.literal(" - ").append(Component.translatable(unlocalizedDescription)).withStyle(ChatFormatting.GRAY));
			i++;
		}
	}
}
