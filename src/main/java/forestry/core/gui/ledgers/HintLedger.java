package forestry.core.gui.ledgers;

import forestry.api.client.ForestrySprites;
import forestry.api.client.IForestryClientApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Random;

public class HintLedger extends Ledger {
	private final Component hintString;
	private final Component hintTooltip;

	public HintLedger(LedgerManager manager, List<String> hints) {
		super(manager, "hint");
		int position = new Random().nextInt(hints.size());
		String hint = hints.get(position);

        this.hintString = Component.translatable("for.hints." + hint + ".desc");
        this.hintTooltip = Component.translatable("for.hints." + hint + ".tag");

		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;
		//TODO text component
		int lineCount = fontRenderer.split(this.hintString, this.maxTextWidth).size();
        this.maxHeight = (lineCount + 1) * fontRenderer.lineHeight + 20;
	}

	@Override
	public void draw(GuiGraphics graphics, int y, int x) {

		// Draw background
		drawBackground(graphics, y, x);

		// Draw icon
		drawSprite(graphics, IForestryClientApi.INSTANCE.getTextureManager().getSprite(ForestrySprites.MISC_HINT), x + 3, y + 4);

		if (!isFullyOpened()) {
			return;
		}

		drawHeader(graphics, Component.translatable("for.gui.didyouknow").append("?"), x + 22, y + 8);
		drawSplitText(graphics, this.hintString, x + 12, y + 20, this.maxTextWidth);
	}

	@Override
	public Component getTooltip() {
		return this.hintTooltip;
	}
}
