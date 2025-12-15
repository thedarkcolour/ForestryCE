package forestry.api.client;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TextOptions {
	private int color = 0xffffff;
	private boolean underlined;
	private boolean bold;
	private boolean italic;

	private boolean dropShadow = false;
	private @Nullable InteractableTextOptions.OnHover onHover;
	private @Nullable Runnable onClick;

	public MutableComponent transform(Component text) {
		return directTransform(text.copy());
	}

	public MutableComponent directTransform(MutableComponent text) {
		return text.withStyle(s -> s
			.withBold(this.bold)
			.withUnderlined(this.underlined)
			.withItalic(this.italic));
	}

	public int color() {
		return this.color;
	}

	public TextOptions setColor(int color) {
		this.color = color;
		return this;
	}

	public boolean underlined() {
		return this.underlined;
	}

	public TextOptions setUnderlined(boolean underlined) {
		this.underlined = underlined;
		return this;
	}

	public boolean bold() {
		return this.bold;
	}

	public TextOptions setBold(boolean bold) {
		this.bold = bold;
		return this;
	}

	public boolean italic() {
		return this.italic;
	}

	public TextOptions setItalic(boolean italic) {
		this.italic = italic;
		return this;
	}

	public boolean dropShadow() {
		return this.dropShadow;
	}

	public TextOptions setDropShadow(boolean dropShadow) {
		this.dropShadow = dropShadow;
		return this;
	}
}
