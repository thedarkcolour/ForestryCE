package forestry.apiculture.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.core.commands.CommandSaveStats;
import forestry.core.commands.GiveSpeciesCommand;
import forestry.core.commands.IStatsSaveHelper;
import forestry.core.commands.ModifyGenomeCommand;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

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
