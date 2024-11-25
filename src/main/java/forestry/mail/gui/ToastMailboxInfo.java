package forestry.mail.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import forestry.api.ForestryConstants;
import forestry.core.config.Constants;
import forestry.core.config.ForestryConfig;
import forestry.mail.POBoxInfo;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ToastMailboxInfo implements Toast {
    private static final long DISPLAY_TIME = 5000L;
    private static final long ICON_TIME = 500L;
    private static final ResourceLocation BACKGROUND_SPRITE = ForestryConstants.forestry(Constants.TEXTURE_PATH_GUI + "/mailalert.png");

    private final Component title = Component.translatable("for.gui.mail.toast.title");
    private POBoxInfo poBox;

    public ToastMailboxInfo(POBoxInfo info) {
        this.poBox = info;
    }

    @Override
    public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long timeSinceLastVisible) {
        if (!ForestryConfig.CLIENT.mailAlertsEnabled.get()) {
            return Visibility.HIDE;
        }

        RenderSystem.setShaderTexture(0, BACKGROUND_SPRITE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        toastComponent.blit(poseStack, 0, 0, 0, 0, this.width(), this.height());

        List<Icons> icons = new ArrayList<>(2);
        if (!poBox.hasMail()) {
            icons.add(Icons.PLAYER_LETTER);
        } else {
            if (poBox.playerLetters() > 0) {
                icons.add(Icons.PLAYER_LETTER);
            }
            if (poBox.tradeLetters() > 0) {
                icons.add(Icons.TRADE_LETTER);
            }
        }
        icons.get((int) (timeSinceLastVisible / ICON_TIME % (long) icons.size())).render(poseStack, toastComponent, 6, 8);
        toastComponent.getMinecraft().font.draw(poseStack, this.title, 36, 7, 0xFFFFFF);
        toastComponent.getMinecraft().font.draw(
                poseStack,
                Component.translatable("for.gui.mail.toast.message", poBox.playerLetters() + poBox.tradeLetters()),
                36,
                18,
                0xFFFFFF
        );

        return timeSinceLastVisible >= DISPLAY_TIME || !poBox.hasMail() ? Visibility.HIDE : Visibility.SHOW;
    }

    public static void addOrUpdate(ToastComponent toastGui, POBoxInfo poBox) {
        ToastMailboxInfo toast = toastGui.getToast(ToastMailboxInfo.class, NO_TOKEN);
        if (toast == null) {
            toastGui.addToast(new ToastMailboxInfo(poBox));
        } else {
            toast.poBox = poBox;
        }

    }

    @OnlyIn(Dist.CLIENT)
    public enum Icons {
        PLAYER_LETTER(0, 0),
        TRADE_LETTER(0, 1);

        private final int x;
        private final int y;

        Icons(int pX, int pY) {
            this.x = pX;
            this.y = pY;
        }

        public void render(PoseStack pPoseStack, GuiComponent pGuiComponent, int pX, int pY) {
            RenderSystem.enableBlend();
            pGuiComponent.blit(pPoseStack, pX, pY, 176 + this.x * 26, this.y * 15, 26, 15);
            RenderSystem.enableBlend();
        }
    }
}
