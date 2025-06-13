package forestry.core.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class StringUtil {
	public static String append(String delim, String source, String appendix) {
		if (source.isEmpty()) {
			return appendix;
		}

		if (appendix.isEmpty()) {
			return source;
		}

		return source + delim + appendix;
	}

	public static String floatAsPercent(float val) {
		return (int) (val * 100) + " %";
	}

	public static Component line(int length) {
		return Component.literal("-".repeat(Math.max(0, length)));
	}

	@OnlyIn(Dist.CLIENT)
	public static int getLineHeight(int maxWidth, FormattedText... strings) {
		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;

		int lineCount = 0;
		for (FormattedText string : strings) {
			lineCount += fontRenderer.split(string, maxWidth).size();
		}

		return lineCount * fontRenderer.lineHeight;
	}
}
