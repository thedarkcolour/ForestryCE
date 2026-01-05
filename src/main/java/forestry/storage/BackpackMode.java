package forestry.storage;

import net.minecraft.util.StringRepresentable;

import javax.annotation.Nullable;
import java.util.Locale;

public enum BackpackMode implements StringRepresentable {
	NEUTRAL(null),
	LOCKED("for.storage.backpack.mode.locked"),
	RECEIVE("for.storage.backpack.mode.receiving"),
	RESUPPLY("for.storage.backpack.mode.resupply");

	public static final BackpackMode[] VALUES = values();

	@Nullable
	private final String translationKey;

	BackpackMode(@Nullable String translationKey) {
		this.translationKey = translationKey;
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ENGLISH);
	}

	@Nullable
	public String getTranslationKey() {
		return this.translationKey;
	}
}
