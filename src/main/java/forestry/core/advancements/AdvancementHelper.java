package forestry.core.advancements;

import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class AdvancementHelper {
	public static void tryUnlock(Player player, ResourceLocation id) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}

		MinecraftServer server = serverPlayer.getServer();
		if (server == null) {
			return;
		}

		Advancement adv = server.getAdvancements().getAdvancement(id);
		if (adv == null) {
			return;
		}

		AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(adv);
		for (String criterion : progress.getRemainingCriteria()) {
			serverPlayer.getAdvancements().award(adv, criterion);
		}
	}

	public static <T extends AbstractCriterionTriggerInstance> void trigger(SimpleCriterionTrigger<T> trigger, @Nullable Level level, @Nullable GameProfile gp, Predicate<T> instanceCheck) {
		if (gp == null || level == null || level.getServer() == null) {
			return;
		}

		ServerLevel serverLevel = level.getServer().getLevel(level.dimension());
		if (serverLevel == null) {
			return;
		}

		Player player = serverLevel.getPlayerByUUID(gp.getId());
		if (player instanceof ServerPlayer serverPlayer) {
			trigger.trigger(serverPlayer, instanceCheck);
		}
	}
}
