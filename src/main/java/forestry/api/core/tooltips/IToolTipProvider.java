package forestry.api.core.tooltips;

import javax.annotation.Nullable;

public interface IToolTipProvider {
	@Nullable
	ToolTip getToolTip(int mouseX, int mouseY);

	// Not fully implemented
	default boolean isToolTipVisible() {
		return true;
	}

	boolean isHovering(double mouseX, double mouseY);

	default boolean isRelativeToGui() {
		return true;
	}
}
