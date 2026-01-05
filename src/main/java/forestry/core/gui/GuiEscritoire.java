package forestry.core.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import forestry.core.config.Constants;
import forestry.core.gui.widgets.GameTokenWidget;
import forestry.core.gui.widgets.ProbeButton;
import forestry.core.gui.widgets.Widget;
import forestry.core.render.ColourProperties;
import forestry.core.tiles.EscritoireGame;
import forestry.core.tiles.EscritoireTextSource;
import forestry.core.tiles.TileEscritoire;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GuiEscritoire extends GuiForestry<ContainerEscritoire> {
	private final ItemStack LEVEL_ITEM = new ItemStack(Items.PAPER);
	private final EscritoireTextSource textSource = new EscritoireTextSource();
	private final TileEscritoire tile;

	public GuiEscritoire(ContainerEscritoire container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/escritoire.png", container, inv, title);

		this.tile = container.getTile();
		this.imageWidth = 228;
		this.imageHeight = 235;

		this.widgetManager.add(new ProbeButton(this, this.widgetManager, 14, 16));

		EscritoireGame game = this.tile.getGame();

		// Inner ring
		addTokenWidget(game, 115, 51, 0);
		addTokenWidget(game, 115, 77, 1);
		addTokenWidget(game, 94, 90, 2);
		addTokenWidget(game, 73, 77, 3);
		addTokenWidget(game, 73, 51, 4);
		addTokenWidget(game, 94, 38, 5);

		// Outer ring
		addTokenWidget(game, 115, 25, 6);
		addTokenWidget(game, 136, 38, 7);
		addTokenWidget(game, 136, 64, 8);

		addTokenWidget(game, 136, 90, 9);
		addTokenWidget(game, 115, 103, 10);
		addTokenWidget(game, 94, 116, 11);

		addTokenWidget(game, 73, 103, 12);
		addTokenWidget(game, 52, 90, 13);
		addTokenWidget(game, 52, 64, 14);

		addTokenWidget(game, 52, 38, 15);
		addTokenWidget(game, 73, 25, 16);
		addTokenWidget(game, 94, 12, 17);

		// Corners
		addTokenWidget(game, 52, 12, 18);
		addTokenWidget(game, 136, 12, 19);
		addTokenWidget(game, 52, 116, 20);
		addTokenWidget(game, 136, 116, 21);
	}

	private void addTokenWidget(EscritoireGame game, int x, int y, int index) {
		Widget gameTokenWidget = new GameTokenWidget(game, this.widgetManager, x, y, index);
        this.widgetManager.add(gameTokenWidget);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		for (int i = 0; i <= this.tile.getGame().getBountyLevel() / 4; i++) {
			GuiUtil.drawItemStack(graphics, this, this.LEVEL_ITEM, this.leftPos + 170 + i * 8, this.topPos + 7);
		}

        this.textLayout.startPage(graphics);
		{
			PoseStack stack = graphics.pose();
			stack.scale(0.5F, 0.5F, 0.5F);
			stack.translate(this.leftPos + 170, this.topPos + 10, 0.0);

            this.textLayout.newLine();
            this.textLayout.newLine();
			Component attemptNoString = Component.translatable("for.gui.escritoire.attempt.number", EscritoireGame.BOUNTY_MAX - this.tile.getGame().getBountyLevel())
				.withStyle(ChatFormatting.UNDERLINE, ChatFormatting.ITALIC);
            this.textLayout.drawLine(graphics, attemptNoString, 170, ColourProperties.INSTANCE.get("gui.mail.lettertext"));
            this.textLayout.newLine();
			Component escritoireText = this.textSource.getText(this.tile.getGame());
            this.textLayout.drawSplitLine(graphics, escritoireText, 170, 90, ColourProperties.INSTANCE.get("gui.mail.lettertext"));
		}
        this.textLayout.endPage(graphics);
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addHintLedger("escritoire");
	}
}
