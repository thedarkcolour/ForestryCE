package forestry.core;

public class ForestryColors {
	public static final int WHITE = 0xffffff;
	// ChatFormatting.GRAY
	public static final int LIGHT_GRAY = 0xaaaaaa;
	public static final int GRAY = 0x808080;
	public static final int DARK_GRAY = 0x404040;
	public static final int YELLOW_GREEN = 0x99cc32;
	public static final int GREEN = 0xebae85;
	public static final int DOMINANT_RED = 0xec3661;
	public static final int RECESSIVE_BLUE = 0x3687ec;
	public static final int BLACK = 0x000000;

	public static int color(int r, int g, int b) {
		return (0xff << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
	}
}
