package forestry.plugin;

import forestry.api.genetics.ForestryTaxa;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.plugin.IGeneticRegistration;

public class BeeTaxonomy {
	@SuppressWarnings("CodeBlock2Expr")
	public static void defineTaxa(IGeneticRegistration genetics) {
		genetics.defineTaxon(ForestryTaxa.CLASS_INSECTS, ForestryTaxa.ORDER_HYMNOPTERA, order -> {
			order.defineSubTaxon(ForestryTaxa.FAMILY_BEES, family -> {
				family.defineSubTaxon(ForestryTaxa.GENUS_HONEY);
				family.defineSubTaxon(ForestryTaxa.GENUS_NOBLE);
				family.defineSubTaxon(ForestryTaxa.GENUS_INDUSTRIOUS);
				family.defineSubTaxon(ForestryTaxa.GENUS_HEROIC, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_CATHEMERAL);
					genus.setDefaultChromosome(BeeChromosomes.CAVE_DWELLING, ForestryAlleles.TRUE);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_LUSH, genus ->{
					genus.setDefaultChromosome(BeeChromosomes.FERTILITY, ForestryAlleles.FERTILITY_3);
					genus.setDefaultChromosome(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_CAVE);
					genus.setDefaultChromosome(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_CATHEMERAL);
					genus.setDefaultChromosome(BeeChromosomes.CAVE_DWELLING, ForestryAlleles.TRUE);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_INFERNAL, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_DOWN_2);
					genus.setDefaultChromosome(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_METATURNAL);
					genus.setDefaultChromosome(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_NETHER);
					genus.setDefaultChromosome(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_AVERAGE);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_AUSTERE, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_1);
					genus.setDefaultChromosome(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_DOWN_1);
					genus.setDefaultChromosome(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_NOCTURNAL);
					genus.setDefaultChromosome(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_CACTI);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_TROPICAL, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_UP_1);
					genus.setDefaultChromosome(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_UP_1);
					genus.setDefaultChromosome(BeeChromosomes.TOLERATES_RAIN, ForestryAlleles.TRUE);
					genus.setDefaultChromosome(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_JUNGLE);
					genus.setDefaultChromosome(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_MIASMIC);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_END, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.FERTILITY, ForestryAlleles.FERTILITY_1);
					genus.setDefaultChromosome(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genus.setDefaultChromosome(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGER);
					genus.setDefaultChromosome(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_UP_1);
					genus.setDefaultChromosome(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_1);
					genus.setDefaultChromosome(BeeChromosomes.TERRITORY, ForestryAlleles.TERRITORY_LARGE);
					genus.setDefaultChromosome(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_END);
					genus.setDefaultChromosome(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_MISANTHROPE);
					genus.setDefaultChromosome(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_NOCTURNAL);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_FROZEN, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_UP_1);
					genus.setDefaultChromosome(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_1);
					genus.setDefaultChromosome(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_SNOW);
					genus.setDefaultChromosome(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_GLACIAL);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_VENGEFUL, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.TERRITORY, ForestryAlleles.TERRITORY_LARGEST);
					genus.setDefaultChromosome(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_RADIOACTIVE);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_FESTIVE, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genus.setDefaultChromosome(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_2);
					genus.setDefaultChromosome(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_1);
					genus.setDefaultChromosome(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_NORMAL);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_AGRARIAN, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genus.setDefaultChromosome(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTER);
					genus.setDefaultChromosome(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_WHEAT);
					genus.setDefaultChromosome(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_FASTER);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_BOGGY, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_MUSHROOMS);
					genus.setDefaultChromosome(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOWER);
					genus.setDefaultChromosome(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_1);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_SAVANNA, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOWEST);
					genus.setDefaultChromosome(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_GOURD);
					genus.setDefaultChromosome(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_UP_1);
					genus.setDefaultChromosome(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_UP_1);
					genus.setDefaultChromosome(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_CREPUSCULAR);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_MONASTIC, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genus.setDefaultChromosome(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONG);
					genus.setDefaultChromosome(BeeChromosomes.FERTILITY, ForestryAlleles.FERTILITY_1);
					genus.setDefaultChromosome(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_FASTER);
					genus.setDefaultChromosome(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_1);
					genus.setDefaultChromosome(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_1);
					genus.setDefaultChromosome(BeeChromosomes.CAVE_DWELLING, ForestryAlleles.TRUE);
					genus.setDefaultChromosome(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_WHEAT);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_KLEPTOPLASTIC, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.FERTILITY, ForestryAlleles.FERTILITY_1);
					genus.setDefaultChromosome(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOWEST);
					genus.setDefaultChromosome(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_1);
					genus.setDefaultChromosome(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_1);
					genus.setDefaultChromosome(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_PHOTOSYNTHESIS);
					genus.setDefaultChromosome(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_IGNITION);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_RELIC, genus ->{
					genus.setDefaultChromosome(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_ANCIENT);
					genus.setDefaultChromosome(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_DOWN_1);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_AQUATIC, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOWEST);
					genus.setDefaultChromosome(BeeChromosomes.TERRITORY, ForestryAlleles.TERRITORY_LARGEST);
					genus.setDefaultChromosome(BeeChromosomes.TOLERATES_RAIN, ForestryAlleles.TRUE);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_EMBITTERED, genus -> {
					genus.setDefaultChromosome(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTENED);
					genus.setDefaultChromosome(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOWEST);
					genus.setDefaultChromosome(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_NETHER);
					genus.setDefaultChromosome(BeeChromosomes.FERTILITY, ForestryAlleles.FERTILITY_4);
					genus.setDefaultChromosome(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_CATHEMERAL);
					genus.setDefaultChromosome(BeeChromosomes.CAVE_DWELLING, ForestryAlleles.TRUE);
				});
				family.defineSubTaxon(ForestryTaxa.GENUS_ABOMINATION);
			});
		});
	}
}
