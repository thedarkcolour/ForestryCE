package forestry.plugin;

import forestry.api.genetics.ForestryTaxa;
import forestry.api.plugin.IGeneticRegistration;

public class TreeTaxonomy {
	@SuppressWarnings("CodeBlock2Expr")
	public static void defineTaxa(IGeneticRegistration genetics) {
		genetics.defineTaxon(ForestryTaxa.KINGDOM_PLANT, ForestryTaxa.PHYLUM_VASCULAR_PLANTS, phylum -> {

			//Ginkgo
			phylum.defineSubTaxon(ForestryTaxa.CLASS_GINKGOOPSIDA, klass -> {
				klass.defineSubTaxon(ForestryTaxa.ORDER_GINKGOALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_GINKGOACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_GINKGO);
					});
				});
			});

			//Palm Trees
			phylum.defineSubTaxon(ForestryTaxa.CLASS_LILIOPSIDA, klass -> {
				klass.defineSubTaxon(ForestryTaxa.ORDER_ARECALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_ARECACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_COCOS);
						family.defineSubTaxon(ForestryTaxa.GENUS_PHOENIX);
					});
				});
			});

			//Flowering Trees
			phylum.defineSubTaxon(ForestryTaxa.CLASS_MAGNOLIOPSIDA, klass -> {
				klass.defineSubTaxon(ForestryTaxa.ORDER_BRASSICALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_CARICACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_CARICA);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_CORNALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_CORNACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_CORNUS);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_ERICALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_EBENACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_DIOSPYROS);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_FABALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_FABACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_ACACIA);
						family.defineSubTaxon(ForestryTaxa.GENUS_DALBERGIA);
						family.defineSubTaxon(ForestryTaxa.GENUS_MILLETTIA);
						family.defineSubTaxon(ForestryTaxa.GENUS_PTEROCARPUS);
						family.defineSubTaxon(ForestryTaxa.GENUS_VACHELLIA);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_FAGALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_BETULACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_BETULA);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_FAGACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_CASTANEA);
						family.defineSubTaxon(ForestryTaxa.GENUS_FAGUS);
						family.defineSubTaxon(ForestryTaxa.GENUS_MICROBERLINIA);
						family.defineSubTaxon(ForestryTaxa.GENUS_QUERCUS);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_JUGLANDACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_JUGLANS);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_LAMIALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_BIGNONIACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_JACARANDA);
						family.defineSubTaxon(ForestryTaxa.GENUS_HANDROANTHUS);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_LAMIACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_TECTONA);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_OLEACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_OLEA);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_LAURALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_LAURACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_CHLOROCARDIUM);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_MALPIGHIALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_SALICACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_POPULUS);
						family.defineSubTaxon(ForestryTaxa.GENUS_SALIX);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_MALVALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_MALVACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_ADANSONIA);
						family.defineSubTaxon(ForestryTaxa.GENUS_CEIBA);
						family.defineSubTaxon(ForestryTaxa.GENUS_OCHROMA);
						family.defineSubTaxon(ForestryTaxa.GENUS_TALIPARITI);
						family.defineSubTaxon(ForestryTaxa.GENUS_THEOBROMA);
						family.defineSubTaxon(ForestryTaxa.GENUS_TILIA);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_MYRTALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_MYRTACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_FEIJOA);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_ROSALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_ROSACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_PRUNUS);
						family.defineSubTaxon(ForestryTaxa.GENUS_PYRUS);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_ULMACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_ULMUS);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_SAPINDALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_MELIACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_SWIETENIA);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_RUTACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_CITRUS);
					});
					order.defineSubTaxon(ForestryTaxa.FAMILY_SAPINDACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_ACER);
					});
				});
			});

			//Pine Trees
			phylum.defineSubTaxon(ForestryTaxa.CLASS_PINOPSIDA, klass -> {
				klass.defineSubTaxon(ForestryTaxa.ORDER_ARAUCARIALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_ARAUCARIACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_AGATHIS);
						family.defineSubTaxon(ForestryTaxa.GENUS_ARAUCARIA);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_PINALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_PINACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_ABIES);
						family.defineSubTaxon(ForestryTaxa.GENUS_LARIX);
						family.defineSubTaxon(ForestryTaxa.GENUS_PICEA);
					});
				});
				klass.defineSubTaxon(ForestryTaxa.ORDER_CUPRESSALES, order -> {
					order.defineSubTaxon(ForestryTaxa.FAMILY_CUPRESSACEAE, family -> {
						family.defineSubTaxon(ForestryTaxa.GENUS_HESPEROCYPARIS);
						family.defineSubTaxon(ForestryTaxa.GENUS_SEQUOIA);
						family.defineSubTaxon(ForestryTaxa.GENUS_SEQUOIADENDRON);
					});
				});
			});
		});
	}
}
