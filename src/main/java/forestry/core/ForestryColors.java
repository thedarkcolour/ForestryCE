package forestry.core;

public class ForestryColors {
	public static final int WHITE = 0xffffff;
	public static final int GRAY = 0x808080;
	public static final int DARK_GRAY = 0x404040;
	public static final int BLACK = 0x000000;

	public static int color(int r, int g, int b) {
		return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
	}
}
