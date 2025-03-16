package forestry.plugin;

import forestry.api.genetics.ForestryTaxa;
import forestry.api.plugin.IGeneticRegistration;

public class TreeTaxonomy {
	@SuppressWarnings("CodeBlock2Expr")
	public static void defineTaxa(IGeneticRegistration genetics) {
		genetics.defineTaxon(ForestryTaxa.KINGDOM_PLANT, ForestryTaxa.PHYLUM_FLOWERING_PLANT, phylum -> {
			phylum.defineSubTaxon(ForestryTaxa.CLASS_ASTERIDS, klass -> {
				klass.defineSubTaxon(ForestryTaxa.ORDER_ERICALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_EBENACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_DIOSPYROS);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_FABACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_ACACIA);
						family.defineSubTaxon(ForestryTaxa.GENUS_DALBERGIA);
						family.defineSubTaxon(ForestryTaxa.GENUS_MILLETTIA);
						family.defineSubTaxon(ForestryTaxa.GENUS_PTEROCARPUS);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_LAMIALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_BIGNONIACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_HANDROANTHUS);
						family.defineSubTaxon(ForestryTaxa.GENUS_JACARANDA);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_LAMIACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_TECTONA);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_OLEACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_OLEA);
					});
				});
			});
			phylum.defineSubTaxon(ForestryTaxa.CLASS_COMMELINIDS, klass -> {
				klass.defineSubTaxon(ForestryTaxa.ORDER_ARECALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_ARECACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_PHOENIX);
						family.defineSubTaxon(ForestryTaxa.GENUS_COCOS); //Lilopsidia class is supposedly outdated?
					});
				});
			});
			phylum.defineSubTaxon(ForestryTaxa.CLASS_ROSIDS, klass -> {
				klass.defineSubTaxon(ForestryTaxa.ORDER_BRASSICALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_CARICACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_CARICA);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_MYRTALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_MYRTACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_FEIJOA);
						family.defineSubTaxon(ForestryTaxa.GENUS_EUCALYPTUS);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_FABALES);
				klass.defineSubTaxon(ForestryTaxa.ORDER_FAGALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_BETULACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_BETULA);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_FAGACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_CASTANEA);
						family.defineSubTaxon(ForestryTaxa.GENUS_QUERCUS);
						family.defineSubTaxon(ForestryTaxa.GENUS_FAGUS);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_JUGLANDACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_JUGLANS);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_MALVALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_DIPTEROCARPACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_MAHOGANY);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_MALVACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_ADANSONIA);
						family.defineSubTaxon(ForestryTaxa.GENUS_CEIBA);
						family.defineSubTaxon(ForestryTaxa.GENUS_OCHROMA);
						family.defineSubTaxon(ForestryTaxa.GENUS_TALIPARITI);
						family.defineSubTaxon(ForestryTaxa.GENUS_TILIA);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_LAURALES);
				klass.defineSubTaxon(ForestryTaxa.ORDER_MALPIGHIALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_SALICACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_CHLOROCARDIUM);
						family.defineSubTaxon(ForestryTaxa.GENUS_POPULUS);
						family.defineSubTaxon(ForestryTaxa.GENUS_SALIX);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_LAURACEAE);
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_SAPINDALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_RUTACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_CITRUS);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_SAPINDACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_ACER);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_ANACARDIACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_ASTRONIUM);
					});
				});
			});
			phylum.defineSubTaxon(ForestryTaxa.CLASS_DICOTYLEDONS, klass -> {
				klass.defineSubTaxon(ForestryTaxa.ORDER_ROSALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_ULMACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_ULMUS);
					});
					//Moved here from Rosids, because according to the following sites, this is where it should be
					//https://www.picturethisai.com/wiki/Prunus_serrulata.html
					//https://www.picturethisai.com/wiki/Rosales.html
					order.defineSubTaxon(ForestryTaxa.FAMILY_ROSACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_PRUNUS);
						family.defineSubTaxon(ForestryTaxa.GENUS_PYRUS);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_CORNALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_CORNACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_CORNUS);
					});
				});
			});
		});
		genetics.defineTaxon(ForestryTaxa.KINGDOM_PLANT, ForestryTaxa.PHYLUM_NAKED_SEED_PLANT, phylum -> {
			phylum.defineSubTaxon(ForestryTaxa.CLASS_CONIFERS, klass -> {
				klass.defineSubTaxon(ForestryTaxa.ORDER_PINALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_PINACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_PICEA);
						family.defineSubTaxon(ForestryTaxa.GENUS_PINUS);
						family.defineSubTaxon(ForestryTaxa.GENUS_LARIX);
						family.defineSubTaxon(ForestryTaxa.GENUS_ABIES);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_CUPRESSACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_SEQUOIA);
						family.defineSubTaxon(ForestryTaxa.GENUS_SEQUOIADENDRON);
						family.defineSubTaxon(ForestryTaxa.GENUS_JUNIPERUS);
						family.defineSubTaxon(ForestryTaxa.GENUS_HESPEROCYPARIS);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_ARAUCARIALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_ARAUCARIACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_ARAUCARIA);
					});
				});
			});
			phylum.defineSubTaxon(ForestryTaxa.CLASS_GINKGOOPSIDA, klass -> {
				klass.defineSubTaxon(ForestryTaxa.ORDER_GINKGOALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_GINKGOACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_GINKGO);
					});
				});
			});
		});
	}
}
