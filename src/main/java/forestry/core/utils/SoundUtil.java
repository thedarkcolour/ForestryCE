package forestry.core.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class SoundUtil {
	public static void playButtonClick() {
		playSoundEvent(SoundEvents.UI_BUTTON_CLICK);
	}

	public static void playSoundEvent(Holder<SoundEvent> soundIn) {
		playSoundEvent(soundIn, 1.0f);
	}

	public static void playSoundEvent(Holder<SoundEvent> soundIn, float pitchIn) {
		Minecraft minecraft = Minecraft.getInstance();
		SoundManager soundHandler = minecraft.getSoundManager();
		SimpleSoundInstance sound = SimpleSoundInstance.forUI(soundIn, pitchIn);
		soundHandler.play(sound);
	}
}
