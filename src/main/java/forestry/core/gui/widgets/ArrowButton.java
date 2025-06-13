package forestry.core.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import forestry.api.ForestryConstants;
import forestry.core.config.Constants;
import forestry.core.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ArrowButton extends Button {
	private static final ResourceLocation TEXTURE = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/buttons.png");

	protected final Texture texture;

	public ArrowButton(int x, int y, Texture texture, OnPress handler) {
		super(x, y, texture.width(), texture.height(), Component.empty(), handler, DEFAULT_NARRATION);
		this.texture = texture;
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mX, int mY, float partialTick) {
		int xOffset = this.texture.x();
		int yOffset = this.texture.y();
		int h = this.height;
		int w = this.width;

		// VANILLA COPY EXCEPT FOR TEXTURE AND COORDINATES
		Minecraft minecraft = Minecraft.getInstance();
		graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		graphics.blit(TEXTURE, getX(), getY(), xOffset, yOffset + RenderUtil.getYImage(this) * h, w, h);
		graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		int i = getFGColor();
		this.renderString(graphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
	}

	public record Texture(int x, int y, int height, int width) {
		public static final Texture LEFT_BUTTON_SMALL = new Texture(238, 220, 12, 9);
		public static final Texture RIGHT_BUTTON_SMALL = new Texture(247, 220, 12, 9);
	}
}
