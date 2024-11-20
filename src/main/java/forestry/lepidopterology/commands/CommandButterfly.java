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
package forestry.lepidopterology.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import forestry.api.lepidopterology.genetics.IButterflySpeciesType;
import forestry.core.commands.CommandHelpers;
import forestry.core.commands.GiveSpeciesCommand;
import forestry.core.commands.ModifyGenomeCommand;
import forestry.core.utils.SpeciesUtil;
import forestry.lepidopterology.features.LepidopterologyEntities;

public class CommandButterfly {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		IButterflySpeciesType type = SpeciesUtil.BUTTERFLY_TYPE.get();

		return Commands.literal("butterfly")
				.then(CommandButterflyKill.register())
				.then(GiveSpeciesCommand.register(type))
				.then(ModifyGenomeCommand.register(type));
	}

	public static class CommandButterflyKill {
		public static ArgumentBuilder<CommandSourceStack, ?> register() {
			return Commands.literal("kill").requires(CommandHelpers.ADMIN).executes(CommandButterflyKill::execute);
		}

		public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
			int killCount = 0;
			for (Entity butterfly : context.getSource().getPlayerOrException().serverLevel().getEntities(LepidopterologyEntities.BUTTERFLY.entityType(), EntitySelector.ENTITY_STILL_ALIVE)) {
				butterfly.remove(Entity.RemovalReason.KILLED);
				killCount++;
			}
			int finalKillCount = killCount;
			context.getSource().sendSuccess(() -> Component.translatable("for.chat.command.forestry.butterfly.kill.response", finalKillCount), true);

			return killCount;
		}
	}
}
