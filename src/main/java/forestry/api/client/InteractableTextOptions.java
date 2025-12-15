package forestry.api.client;

import javax.annotation.Nullable;

public class InteractableTextOptions extends TextOptions {
	private @Nullable OnHover onHover;
	//private @Nullable Runnable onClick;

	@Nullable
	public OnHover onHover() {
		return this.onHover;
	}

	public InteractableTextOptions setOnHover(@Nullable OnHover onHover) {
		this.onHover = onHover;
		return this;
	}
/*
	@Nullable
	public Runnable onClick() {
		return this.onClick;
	}

	public InteractableTextOptions setOnClick(@Nullable Runnable onClick) {
		this.onClick = onClick;
		return this;
	}
*/

	@Override
	public InteractableTextOptions setColor(int color) {
		super.setColor(color);
		return this;
	}

	@Override
	public InteractableTextOptions setUnderlined(boolean underlined) {
		super.setUnderlined(underlined);
		return this;
	}

	@Override
	public InteractableTextOptions setBold(boolean bold) {
		super.setBold(bold);
		return this;
	}

	@Override
	public InteractableTextOptions setItalic(boolean italic) {
		super.setItalic(italic);
		return this;
	}

	@Override
	public InteractableTextOptions setDropShadow(boolean dropShadow) {
		super.setDropShadow(dropShadow);
		return this;
	}

	public interface OnHover {
		void onHover(int mouseX, int mouseY);
	}
}
