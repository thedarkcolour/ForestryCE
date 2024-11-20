package forestry.core.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Drawable {
	/* Final Attributes */
	//Position on the Texture
	public final int u;
	public final int v;
	//Rectangle Size
	public final int uWidth;
	public final int vHeight;
	//Texture
	public final ResourceLocation textureLocation;
	//Texture Size
	private final int textureWidth;
	private final int textureHeight;

	public Drawable(ResourceLocation textureLocation, int u, int v, int uWidth, int vHeight) {
		this(textureLocation, u, v, uWidth, vHeight, 256, 256);
	}

	public Drawable(ResourceLocation textureLocation, int u, int v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
		this.u = u;
		this.v = v;
		this.uWidth = uWidth;
		this.vHeight = vHeight;
		this.textureLocation = textureLocation;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	public void draw(GuiGraphics graphics, int yOffset, int xOffset) {
		draw(graphics, yOffset, uWidth, vHeight, xOffset);
	}

	public void draw(GuiGraphics graphics, int yOffset, int width, int height, int xOffset) {
		RenderSystem.setShaderTexture(0, textureLocation);

		// Enable correct lighting.
		// RenderSystem.enableAlphaTest();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		graphics.blit(this.textureLocation, xOffset, yOffset, width, height, this.u, this.v, this.uWidth, this.vHeight, this.textureWidth, this.textureHeight);
		// RenderSystem.disableAlphaTest();
	}
}
