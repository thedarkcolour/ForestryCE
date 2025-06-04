package forestry.core.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import forestry.Forestry;
import forestry.api.IForestryApi;
import forestry.api.genetics.IGeneticManager;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.alleles.*;
import forestry.core.utils.SpeciesUtil;
import forestry.core.utils.Translator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.Collection;
import java.util.List;

// Test command to make sure things aren't broken after the API rewrite.
public class DiagnosticsCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("diagnostics")
			.executes(DiagnosticsCommand::diagnostics);
	}

	private static int diagnostics(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();

		source.sendSystemMessage(Component.literal("Running Forestry diagnostics..."));

		IGeneticManager genetics = IForestryApi.INSTANCE.getGeneticManager();
		ISpeciesType<?, ?>[] types = genetics.getSpeciesTypes().toArray(ISpeciesType<?, ?>[]::new);
		Forestry.LOGGER.info("Found {} species types", types.length);

		for (ISpeciesType<?, ?> type : types) {
			List<? extends ISpecies<?>> allSpecies = type.getAllSpecies();
			Forestry.LOGGER.info("Species type {} has {} registered species", type.id(), allSpecies.size());

			for (ISpecies<?> species : allSpecies) {
				if (FMLEnvironment.dist == Dist.CLIENT) {
					// Name translation
					if (!Translator.canTranslateToLocal(species.getTranslationKey())) {
						Forestry.LOGGER.error(species.id() + " is missing a translated name");
					}
					// Bee descriptions (Sengir never added descriptions for the other species types)
					if (species.getType() == SpeciesUtil.BEE_TYPE.get() && !Translator.canTranslateToLocal(species.getDescriptionTranslationKey())) {
						Forestry.LOGGER.error(species.id() + " is missing a description");
					}
				}
			}

			if (FMLEnvironment.dist == Dist.CLIENT) {
				IKaryotype karyotype = type.getKaryotype();

				for (IChromosome<?> chromosome : karyotype.getChromosomes()) {
					if (chromosome == karyotype.getSpeciesChromosome() || chromosome instanceof IBooleanChromosome || chromosome == BeeChromosomes.FERTILITY || chromosome == ButterflyChromosomes.FERTILITY) {
						continue;
					}

					Collection<? extends IAllele> alleles = karyotype.getAlleles(chromosome);

					Forestry.LOGGER.info("Chromosome {} has {} alleles", chromosome.id(), alleles.size());

					for (IAllele allele : alleles) {
						String translationKey = chromosome.getTranslationKey(allele.cast());

						if (!Translator.canTranslateToLocal(translationKey)) {
							Forestry.LOGGER.error("Allele for chromosome {} is missing a translation - {}", chromosome.id(), translationKey);
						}
					}
				}
			}
		}

		source.sendSystemMessage(Component.literal("Diagnostics complete. Check log for details"));

		return 0;
	}
}
