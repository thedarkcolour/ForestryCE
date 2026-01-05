package forestry.core.genetics;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class GenericRatings {
	public static MutableComponent rateActivityTime(boolean neverSleeps, boolean naturalNocturnal) {
		MutableComponent active = naturalNocturnal ? Component.translatable("for.gui.nocturnal") : Component.translatable("for.gui.diurnal");
		if (neverSleeps) {
			active.append(", ").append(naturalNocturnal ? Component.translatable("for.gui.diurnal") : Component.translatable("for.gui.nocturnal"));
		}

		return active;
	}
}
