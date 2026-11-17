package forestry.core.platform.render;

import com.mojang.blaze3d.vertex.PoseStack;
import forestry.core.content.analyzer.AnalyzerBlockEntity;
import forestry.core.platform.util.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

// BER for the block form of analyzer
public class AnalyzerRenderer implements BlockEntityRenderer<AnalyzerBlockEntity> {
	private final ItemRenderer itemRenderer;

	public AnalyzerRenderer(BlockEntityRendererProvider.Context ctx) {
		this.itemRenderer = ctx.getItemRenderer();
	}

	@Override
	public void render(AnalyzerBlockEntity analyzer, float partialTick, PoseStack stack, MultiBufferSource buffers, int light, int overlay) {
		ItemStack displayStack = analyzer.getIndividualOnDisplay();
		if (!displayStack.isEmpty()) {
			stack.pushPose();
			stack.translate(0.5f, 0.2f, 0.5f);

			RenderUtil.renderDisplayStack(stack, this.itemRenderer, displayStack, analyzer.getLevel(), partialTick, buffers, light);

			stack.popPose();
		}
	}
}
