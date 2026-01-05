package forestry.core.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import forestry.api.client.ForestrySprites;
import forestry.api.client.IForestryClientApi;
import forestry.api.core.tooltips.ToolTip;
import forestry.core.gui.GuiUtil;
import forestry.core.network.packets.PacketGuiSelectRequest;
import forestry.core.tiles.EscritoireGame;
import forestry.core.tiles.EscritoireGameToken;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.SoundUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;

public class GameTokenWidget extends Widget {
	private final ItemStack HIDDEN_TOKEN = new ItemStack(Items.BOOK);

	private final EscritoireGame game;
	private final int index;

	public GameTokenWidget(EscritoireGame game, WidgetManager manager, int xPos, int yPos, int index) {
		super(manager, xPos, yPos);
		this.game = game;
		this.index = index;
		this.width = 20;
		this.height = 20;
	}

	@Nullable
	private EscritoireGameToken getToken() {
		return this.game.getToken(this.index);
	}

	@Override
	public void draw(GuiGraphics graphics, int startX, int startY) {
		EscritoireGameToken token = getToken();
		if (token == null) {
			return;
		}

		int tokenColour = token.getTokenColour();

		float colorR = (tokenColour >> 16 & 255) / 255.0F;
		float colorG = (tokenColour >> 8 & 255) / 255.0F;
		float colorB = (tokenColour & 255) / 255.0F;

		RenderSystem.enableDepthTest();
		RenderSystem.setShaderColor(colorR, colorG, colorB, 1.0f);
		graphics.blit(this.manager.gui.textureFile, startX + this.xPos, startY + this.yPos, 228, 0, 22, 22);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

		ItemStack tokenStack = this.HIDDEN_TOKEN;
		if (token.isVisible()) {
			tokenStack = token.getTokenStack();
		}

		GuiUtil.drawItemStack(graphics, this.manager.gui, tokenStack, startX + this.xPos + 3, startY + this.yPos + 3);

		ResourceLocation overlayToken = token.getOverlayToken();

		if (overlayToken != null) {
			RenderSystem.disableDepthTest();
			RenderSystem.setShaderTexture(0, ForestrySprites.TEXTURE_ATLAS);
			TextureAtlasSprite icon = IForestryClientApi.INSTANCE.getTextureManager().getSprite(overlayToken);
			graphics.blit(startX + this.xPos + 3, startY + this.yPos + 3, 0, 16, 16, icon);
			RenderSystem.enableDepthTest();
		}
	}

	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		EscritoireGameToken token = getToken();
		if (token == null || !token.isVisible()) {
			return null;
		}

		ToolTip tooltip = new ToolTip();
		tooltip.add(token.getTooltip());
		return tooltip;
	}

	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton) {
        this.game.choose(this.index);
		NetworkUtil.sendToServer(new PacketGuiSelectRequest(this.index, 0));
		SoundUtil.playButtonClick();
	}
}
