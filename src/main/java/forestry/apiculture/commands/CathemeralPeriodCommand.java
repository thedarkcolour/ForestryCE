package forestry.apiculture.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import com.mojang.brigadier.builder.ArgumentBuilder;

import forestry.apiculture.CathemeralActivityType;

import org.joml.Vector2i;

public class CathemeralPeriodCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("cathemeral").executes(ctx -> {
			BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
			Vector2i period = CathemeralActivityType.getSleepPeriod(pos);
			ctx.getSource().sendSuccess(() -> Component.literal("Wakes: " + period.x + ", Sleeps: " + period.y), true);
			return 1;
		});
	}
}
