package forestry.core.utils;

import java.awt.Color;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import forestry.core.fluids.ForestryFluids;

public class RenderUtil {
	// requires external push/pop
	public static void rotateByHorizontalDirection(PoseStack stack, Direction facing) {
		if (facing != Direction.SOUTH) {
			stack.translate(0.5, 0.5, 0.5);
			stack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
			stack.translate(-0.5, -0.5, -0.5);
		}
	}

	// requires external push/pop
	public static void renderDisplayStack(PoseStack stack, ItemRenderer itemRenderer, ItemStack displayStack, Level level, float partialTick, MultiBufferSource buffers, int light) {
		BakedModel itemModel = itemRenderer.getModel(displayStack, level, null, 1);
		boolean isGui3d = itemModel.isGui3d();
		float smoothTick = ((float) (int) level.getGameTime()) + partialTick;
		float f1 = Mth.sin(smoothTick / 10.0f) * 0.1f + 0.1f;
		float f2 = itemModel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
		stack.translate(0, f1 + 0.25f * f2, 0);
		stack.mulPose(Axis.YP.rotation(smoothTick / 20f));

		itemRenderer.render(displayStack, ItemDisplayContext.GROUND, false, stack, buffers, light, OverlayTexture.NO_OVERLAY, itemModel);

		if (!isGui3d) {
			stack.translate(0.0, 0.0, 0.09375F);
		}
	}

	public static int getFluidColor(Fluid fluid) {
		FluidType attributes = fluid.getFluidType();
		int color = IClientFluidTypeExtensions.of(attributes).getTintColor();
		ForestryFluids definition = ForestryFluids.getFluidDefinition(fluid);
		if (color < 0) {
			color = 0x0000ff;
			if (definition != null) {
				color = definition.getParticleColor();
			}
		}
		return color;
	}

	public static Color getRainbowColor(long time, float partialTicks) {
		return Color.getHSBColor((180 * Mth.sin((time + partialTicks) / 30.0f) - 180) / 360.0f, 0.5f, 0.8f);
	}

	// VANILLA COPY
	public static int getYImage(Button button) {
		int i = 1;
		if (!button.active) {
			i = 0;
		} else if (button.isHoveredOrFocused()) {
			i = 2;
		}

		return i;
	}
}
