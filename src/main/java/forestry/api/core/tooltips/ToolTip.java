package forestry.api.core.tooltips;

public class ToolTip extends TextCollection {
	private final long delay;
	private long mouseOverStart;

	public ToolTip() {
		this.delay = 0;
	}

	public ToolTip(int delay) {
		this.delay = delay;
	}

	public void onTick(boolean mouseOver) {
		if (this.delay == 0) {
			return;
		}
		if (mouseOver) {
			if (this.mouseOverStart == 0) {
				this.mouseOverStart = System.currentTimeMillis();
			}
		} else {
			this.mouseOverStart = 0;
		}
	}

	public boolean isReady() {
		return this.delay == 0 || this.mouseOverStart != 0 && System.currentTimeMillis() - this.mouseOverStart >= this.delay;
	}

	public void refresh() {
	}
}
