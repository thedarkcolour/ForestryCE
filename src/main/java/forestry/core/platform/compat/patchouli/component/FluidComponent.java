package forestry.core.platform.compat.patchouli.component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import forestry.Forestry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

@SuppressWarnings("unused")
public class FluidComponent implements ICustomComponent {
	public IVariable fluid;
	public IVariable amount;
	public IVariable max;
	public IVariable width;
	public IVariable height;

	private transient int x, y, w, h, level, maxLevel;
	private transient FluidStack fluidStack = FluidStack.EMPTY;
	private transient boolean failed;

	@Override
	public void build(int componentX, int componentY, int pageNum) {
		this.x = componentX;
		this.y = componentY;
	}

	@Override
	public void render(GuiGraphics graphics, IComponentRenderContext context, float pticks, int mouseX, int mouseY) {
		if (this.failed || this.fluidStack.isEmpty() || this.maxLevel <= 0) {
			return;
		}

		PoseStack stack = graphics.pose();
		stack.pushPose();
		try {
			renderFluid(graphics, context, mouseX, mouseY);
		} catch (RuntimeException e) {
			this.failed = true;
			Forestry.LOGGER.error("Failed to render fluid {} in the Forester's Manual, hiding it", this.fluidStack.getFluid(), e);
		} finally {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			stack.popPose();
		}
	}

	private void renderFluid(GuiGraphics graphics, IComponentRenderContext context, int mouseX, int mouseY) {
		IClientFluidTypeExtensions fluidAttributes = IClientFluidTypeExtensions.of(this.fluidStack.getFluid());
		ResourceLocation fluidStill = fluidAttributes.getStillTexture(this.fluidStack);
		if (fluidStill == null) {
			return;
		}

		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
		ResourceLocation spriteLocation = sprite.contents().name();
		ResourceLocation fluidTexture = ResourceLocation.fromNamespaceAndPath(spriteLocation.getNamespace(), "textures/" + spriteLocation.getPath() + ".png");
		setGLColorFromInt(fluidAttributes.getTintColor(this.fluidStack));

		int filled = Math.min(this.level, this.maxLevel);
		// MatrixStack transform, int x, int y, float u, float v, int width, int height, int ?, int ?
		graphics.blit(fluidTexture, this.x, (int) (this.y + this.h - Math.floor(this.h * ((float) filled / this.maxLevel))), sprite.getU0(), sprite.getV0(), this.w, this.h * filled / this.maxLevel, 8, 8);

		if (context.isAreaHovered(mouseX, mouseY, this.x, this.y, this.w, this.h)) {
			List<Component> toolTips = new ArrayList<>();
			toolTips.add(this.fluidStack.getHoverName());
			toolTips.add(Component.translatable("for.gui.tooltip.liquid.amount", this.level, this.maxLevel));

			context.setHoverTooltipComponents(toolTips);
		}
	}

	@Override
	public void onVariablesAvailable(UnaryOperator<IVariable> lookup, HolderLookup.Provider registries) {
		try {
			this.w = lookup.apply(this.width).asNumber().intValue();
			this.h = lookup.apply(this.height).asNumber().intValue();
			this.level = lookup.apply(this.amount).asNumber().intValue();
			this.maxLevel = lookup.apply(this.max).asNumber().intValue();

			// the fluid is absent when the processor found no recipe to show
			String fluidName = lookup.apply(this.fluid).asString();
			this.fluidStack = fluidName.isEmpty() ? FluidStack.EMPTY : new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidName)), this.level);
		} catch (RuntimeException e) {
			this.fluidStack = FluidStack.EMPTY;
			Forestry.LOGGER.error("Failed to read fluid component variables in the Forester's Manual, hiding it", e);
		}
	}

	private static void setGLColorFromInt(int color) {
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;

		RenderSystem.setShaderColor(red, green, blue, 1.0F);
	}
}
