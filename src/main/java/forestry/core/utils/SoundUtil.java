/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
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
