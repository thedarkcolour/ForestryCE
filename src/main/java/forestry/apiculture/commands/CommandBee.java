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
package forestry.apiculture.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.mojang.brigadier.builder.ArgumentBuilder;

import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.core.commands.CommandSaveStats;
import forestry.core.commands.IStatsSaveHelper;
import forestry.core.commands.GiveSpeciesCommand;
import forestry.core.commands.ModifyGenomeCommand;
import forestry.core.utils.SpeciesUtil;

public class CommandBee {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		IStatsSaveHelper saveHelper = new BeeStatsSaveHelper();
		IBeeSpeciesType type = SpeciesUtil.BEE_TYPE.get();

		return Commands.literal("bee")
				.then(CommandSaveStats.register(saveHelper))
				.then(GiveSpeciesCommand.register(type))
				.then(ModifyGenomeCommand.register(type))
				.then(CathemeralPeriodCommand.register());
	}
}
