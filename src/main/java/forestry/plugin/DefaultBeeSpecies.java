package forestry.plugin;

import java.awt.Color;
import java.time.Month;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.minecraftforge.common.Tags;

import forestry.api.ForestryTags;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.plugin.IApicultureRegistration;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.genetics.FireworkProduct;
import forestry.apiculture.genetics.HermitBeeJubilance;
import forestry.apiculture.items.EnumHoneyComb;
import forestry.apiculture.items.EnumPollenCluster;
import forestry.core.features.CoreItems;
import forestry.core.items.definitions.EnumCraftingMaterial;

import static forestry.api.genetics.ForestryTaxa.*;
import static forestry.apiculture.features.ApicultureItems.BEE_COMBS;
import static forestry.apiculture.features.ApicultureItems.POLLEN_CLUSTER;

public class DefaultBeeSpecies {
	@SuppressWarnings("CodeBlock2Expr")
	public static void register(IApicultureRegistration apiculture) {
		ResourceLocation[] overworldHiveBees = new ResourceLocation[]{ForestryBeeSpecies.FOREST, ForestryBeeSpecies.MARSHY, ForestryBeeSpecies.MEADOWS, ForestryBeeSpecies.MODEST, ForestryBeeSpecies.SAVANNA, ForestryBeeSpecies.TROPICAL, ForestryBeeSpecies.VALIANT, ForestryBeeSpecies.WINTRY, ForestryBeeSpecies.ENDED};

		// Forest
		apiculture.registerSpecies(ForestryBeeSpecies.FOREST, GENUS_HONEY, SPECIES_FOREST, true, new Color(0x19d0ec))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.HONEY), 0.30f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOWER);
					genome.set(BeeChromosomes.FERTILITY, ForestryAlleles.FERTILITY_3);
					genome.set(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_DOWN_1);
				});

		// Meadows
		apiculture.registerSpecies(ForestryBeeSpecies.MEADOWS, GENUS_HONEY, SPECIES_MEADOWS, true, new Color(0xef131e))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.HONEY), 0.30f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOWER);
					genome.set(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_DOWN_1);
				});

		// Common
		apiculture.registerSpecies(ForestryBeeSpecies.COMMON, GENUS_HONEY, SPECIES_COMMON, true, new Color(0xb2b2b2))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.HONEY), 0.35f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
				})
				.addMutations(mutations -> {
					for (int i = 0; i < overworldHiveBees.length; i++) {
						ResourceLocation firstParent = overworldHiveBees[i];
						for (int j = i + 1; j < overworldHiveBees.length; j++) {
							mutations.add(firstParent, overworldHiveBees[j], 15);
						}
					}
				});

		// Cultivated
		apiculture.registerSpecies(ForestryBeeSpecies.CULTIVATED, GENUS_HONEY, SPECIES_CULTIVATED, true, new Color(0x5734ec))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.HONEY), 0.40f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_FAST);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTEST);
				})
				.addMutations(mutations -> {
					for (ResourceLocation secondParent : overworldHiveBees) {
						mutations.add(ForestryBeeSpecies.COMMON, secondParent, 12);
					}
				});

		// Noble
		apiculture.registerSpecies(ForestryBeeSpecies.NOBLE, GENUS_NOBLE, SPECIES_NOBLE, false, new Color(0xec9a19))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.DRIPPING), 0.20f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORT);
					genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOW);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.COMMON, ForestryBeeSpecies.CULTIVATED, 10);
				});

		// Majestic
		apiculture.registerSpecies(ForestryBeeSpecies.MAJESTIC, GENUS_NOBLE, SPECIES_MAJESTIC, true, new Color(0x7f0000))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.DRIPPING), 0.30f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_NORMAL);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTENED);
					genome.set(BeeChromosomes.FERTILITY, ForestryAlleles.FERTILITY_4);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.NOBLE, ForestryBeeSpecies.CULTIVATED, 8);
				});

		// Imperial
		apiculture.registerSpecies(ForestryBeeSpecies.IMPERIAL, GENUS_NOBLE, SPECIES_IMPERIAL, false, new Color(0xa3e02f))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.DRIPPING), 0.20f)
				.addProduct(ApicultureItems.ROYAL_JELLY.stack(), 0.15f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_NORMAL);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_BEATIFIC);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.NOBLE, ForestryBeeSpecies.MAJESTIC, 8);
				})
				.setGlint(true);

		// Diligent
		apiculture.registerSpecies(ForestryBeeSpecies.DILIGENT, GENUS_INDUSTRIOUS, SPECIES_DILIGENT, false, new Color(0xc219ec))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.STRINGY), 0.20f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORT);
					genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOW);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.COMMON, ForestryBeeSpecies.CULTIVATED, 10);
				});
		// Unweary
		apiculture.registerSpecies(ForestryBeeSpecies.UNWEARY, GENUS_INDUSTRIOUS, SPECIES_UNWEARY, true, new Color(0x19ec5a))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.STRINGY), 0.30f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_NORMAL);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTENED);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.DILIGENT, ForestryBeeSpecies.CULTIVATED, 8);
				});

		// Industrious
		apiculture.registerSpecies(ForestryBeeSpecies.INDUSTRIOUS, GENUS_INDUSTRIOUS, SPECIES_INDUSTRIOUS, false, new Color(0xffffff))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.STRINGY), 0.20f)
				.addProduct(POLLEN_CLUSTER.stack(EnumPollenCluster.NORMAL), 0.15f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_NORMAL);
					genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_FAST);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.DILIGENT, ForestryBeeSpecies.UNWEARY, 8);
				})
				.setGlint(true);

		// Steadfast
		apiculture.registerSpecies(ForestryBeeSpecies.STEADFAST, GENUS_HEROIC, SPECIES_STEADFAST, false, new Color(0x4d2b15))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.COCOA), 0.20f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_NORMAL);
				})
				.setGlint(true);

		// Valiant
		apiculture.registerSpecies(ForestryBeeSpecies.VALIANT, GENUS_HEROIC, SPECIES_VALIANT, true, new Color(0x626bdd))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.COCOA), 0.30f)
				.addSpecialty(new ItemStack(Items.SUGAR), 0.15f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOW);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONG);
				});

		// Heroic
		apiculture.registerSpecies(ForestryBeeSpecies.HEROIC, GENUS_HEROIC, SPECIES_HEROIC, false, new Color(0xb3d5e4))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.COCOA), 0.40f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOW);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONG);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_HEROIC);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.STEADFAST, ForestryBeeSpecies.VALIANT, 6)
							.restrictBiomeType(BiomeTags.IS_FOREST);
				})
				.setGlint(true);

		// Sinister
		apiculture.registerSpecies(ForestryBeeSpecies.SINISTER, GENUS_INFERNAL, SPECIES_SINISTER, false, new Color(0xb3d5e4))
				.setBody(new Color(0x9a2323))
				.setTemperature(TemperatureType.HELLISH)
				.setHumidity(HumidityType.ARID)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.SIMMERING), 0.45f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_NORMAL);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_AGGRESSIVE);
				})
				.addMutations(mutations -> {
					ResourceLocation[] parents = new ResourceLocation[]{ForestryBeeSpecies.MODEST, ForestryBeeSpecies.TROPICAL};

					for (ResourceLocation parent : parents) {
						mutations.add(ForestryBeeSpecies.CULTIVATED, parent, 60)
								.restrictBiomeType(BiomeTags.IS_NETHER);
					}
				});

		// Fiendish
		apiculture.registerSpecies(ForestryBeeSpecies.FIENDISH, GENUS_INFERNAL, SPECIES_FIENDISH, true, new Color(0xd7bee5))
				.setBody(new Color(0x9a2323))
				.setTemperature(TemperatureType.HELLISH)
				.setHumidity(HumidityType.ARID)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.SIMMERING), 0.55f)
				.addProduct(CoreItems.ASH.stack(), 0.15f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_NORMAL);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONG);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_AGGRESSIVE);
				})
				.addMutations(mutations -> {
					ResourceLocation[] parents = new ResourceLocation[]{ForestryBeeSpecies.CULTIVATED, ForestryBeeSpecies.MODEST, ForestryBeeSpecies.TROPICAL};

					for (ResourceLocation parent : parents) {
						mutations.add(ForestryBeeSpecies.SINISTER, parent, 40)
								.restrictBiomeType(BiomeTags.IS_NETHER);
					}
				});

		// Demonic
		apiculture.registerSpecies(ForestryBeeSpecies.DEMONIC, GENUS_INFERNAL, SPECIES_DEMONIC, false, new Color(0xf4e400))
				.setBody(new Color(0x9a2323))
				.setTemperature(TemperatureType.HELLISH)
				.setHumidity(HumidityType.ARID)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.SIMMERING), 0.45f)
				.addProduct(new ItemStack(Items.GLOWSTONE_DUST), 0.15f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGER);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_IGNITION);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.SINISTER, ForestryBeeSpecies.FIENDISH, 25)
							.restrictBiomeType(BiomeTags.IS_NETHER);
				})
				.setGlint(true);

		// Modest
		apiculture.registerSpecies(ForestryBeeSpecies.MODEST, GENUS_AUSTERE, SPECIES_MODEST, false, new Color(0xc5be86))
				.setTemperature(TemperatureType.HOT)
				.setHumidity(HumidityType.ARID)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.PARCHED), 0.20f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORT);
				});

		// Frugal
		apiculture.registerSpecies(ForestryBeeSpecies.FRUGAL, GENUS_AUSTERE, SPECIES_FRUGAL, true, new Color(0xe8dcb1))
				.setTemperature(TemperatureType.HOT)
				.setHumidity(HumidityType.ARID)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.PARCHED), 0.30f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_NORMAL);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONG);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.MODEST, ForestryBeeSpecies.SINISTER, 16)
							.restrictTemperature(TemperatureType.HOT, TemperatureType.HELLISH)
							.restrictHumidity(HumidityType.ARID);
					mutations.add(ForestryBeeSpecies.MODEST, ForestryBeeSpecies.FIENDISH, 10)
							.restrictTemperature(TemperatureType.HOT, TemperatureType.HELLISH)
							.restrictHumidity(HumidityType.ARID);
				});

		// Austere
		apiculture.registerSpecies(ForestryBeeSpecies.AUSTERE, GENUS_AUSTERE, SPECIES_AUSTERE, false, new Color(0xfffac2))
				.setTemperature(TemperatureType.HOT)
				.setHumidity(HumidityType.ARID)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.PARCHED), 0.20f)
				.addSpecialty(BEE_COMBS.stack(EnumHoneyComb.POWDERY), 0.50f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGER);
					genome.set(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_DOWN_2);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_CREEPER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.MODEST, ForestryBeeSpecies.FRUGAL, 8)
							.restrictTemperature(TemperatureType.HOT, TemperatureType.HELLISH)
							.restrictHumidity(HumidityType.ARID);
				})
				.setGlint(true);

		// Tropical
		apiculture.registerSpecies(ForestryBeeSpecies.TROPICAL, GENUS_TROPICAL, SPECIES_TROPICAL, false, new Color(0x378020))
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.SILKY), 0.20f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORT);
				});

		// Exotic
		apiculture.registerSpecies(ForestryBeeSpecies.EXOTIC, GENUS_TROPICAL, SPECIES_EXOTIC, true, new Color(0x304903))
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.SILKY), 0.30f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_NORMAL);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONG);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.AUSTERE, ForestryBeeSpecies.TROPICAL, 12);
				});

		// Edenic
		apiculture.registerSpecies(ForestryBeeSpecies.EDENIC, GENUS_TROPICAL, SPECIES_EDENIC, false, new Color(0x393d0d))
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.SILKY), 0.20f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGER);
					genome.set(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_2);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_EXPLORATION);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.EXOTIC, ForestryBeeSpecies.TROPICAL, 8);
				})
				.setGlint(true);

		// Ended
		apiculture.registerSpecies(ForestryBeeSpecies.ENDED, GENUS_END, SPECIES_ENDED, false, new Color(0xe079fa))
				.setBody(new Color(0xd9de9e))
				.setTemperature(TemperatureType.COLD)
				.setHumidity(HumidityType.ARID)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.MYSTERIOUS), 0.30f);

		// Spectral
		apiculture.registerSpecies(ForestryBeeSpecies.SPECTRAL, GENUS_END, SPECIES_SPECTRAL, true, new Color(0xa98bed))
				.setBody(new Color(0xd9de9e))
				.setTemperature(TemperatureType.COLD)
				.setHumidity(HumidityType.ARID)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.MYSTERIOUS), 0.50f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_REANIMATION);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.HERMITIC, ForestryBeeSpecies.ENDED, 4);
				});

		// Phantasmal
		apiculture.registerSpecies(ForestryBeeSpecies.PHANTASMAL, GENUS_END, SPECIES_PHANTASMAL, false, new Color(0xcc00fa))
				.setBody(new Color(0xd9de9e))
				.setTemperature(TemperatureType.COLD)
				.setHumidity(HumidityType.ARID)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.MYSTERIOUS), 0.40f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGEST);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_RESURRECTION);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.SPECTRAL, ForestryBeeSpecies.ENDED, 2);
				})
				.setGlint(true);

		// Wintry
		apiculture.registerSpecies(ForestryBeeSpecies.WINTRY, GENUS_FROZEN, SPECIES_WINTRY, false, new Color(0xa0ffc8))
				.setBody(new Color(0xdaf5f3))
				.setTemperature(TemperatureType.ICY)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.FROZEN), 0.30f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORT);
					genome.set(BeeChromosomes.FERTILITY, ForestryAlleles.FERTILITY_4);
				});

		// Icy
		apiculture.registerSpecies(ForestryBeeSpecies.ICY, GENUS_FROZEN, SPECIES_ICY, true, new Color(0xa0ffff))
				.setBody(new Color(0xdaf5f3))
				.setTemperature(TemperatureType.ICY)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.FROZEN), 0.20f)
				.addProduct(CoreItems.CRAFTING_MATERIALS.stack(EnumCraftingMaterial.ICE_SHARD), 0.20f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOW);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORT);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.INDUSTRIOUS, ForestryBeeSpecies.WINTRY, 12)
							.restrictTemperature(TemperatureType.ICY, TemperatureType.COLD);
				});

		// Glacial
		apiculture.registerSpecies(ForestryBeeSpecies.GLACIAL, GENUS_FROZEN, SPECIES_GLACIAL, false, new Color(0xefffff))
				.setBody(new Color(0xdaf5f3))
				.setTemperature(TemperatureType.ICY)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.FROZEN), 0.20f)
				.addProduct(CoreItems.CRAFTING_MATERIALS.stack(EnumCraftingMaterial.ICE_SHARD), 0.40f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORT);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.ICY, ForestryBeeSpecies.WINTRY, 8)
							.restrictTemperature(TemperatureType.ICY, TemperatureType.COLD);
				})
				.setGlint(true);
		// todo move to IC2 plugin when that's ported
/*

		// Vindictive
		apiculture.registerSpecies(ForestryBeeSpecies.VINDICTIVE, GENUS_VENGEFUL, SPECIES_VINDICTIVE, false, new Color(0xeafff3))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.IRRADIATED), 0.25f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_NORMAL);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.MONASTIC, ForestryBeeSpecies.DEMONIC, 4);
				})
				.setSecret(true);

		// Vengeful
		apiculture.registerSpecies(ForestryBeeSpecies.VENGEFUL, GENUS_VENGEFUL, SPECIES_VENGEFUL, false, new Color(0xc2de00))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.IRRADIATED), 0.40f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_NORMAL);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.DEMONIC, ForestryBeeSpecies.VINDICTIVE, 8);
					mutations.add(ForestryBeeSpecies.MONASTIC, ForestryBeeSpecies.VINDICTIVE, 8);
				})
				.setSecret(true);

		// Avenging
		apiculture.registerSpecies(ForestryBeeSpecies.AVENGING, GENUS_VENGEFUL, SPECIES_AVENGING, false, new Color(0xddff00))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.IRRADIATED), 0.40f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGEST);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.VENGEFUL, ForestryBeeSpecies.VINDICTIVE, 4);
				})
				.setGlint(true)
				.setSecret(true);
*/

		// Leporine (Easter secret)
		apiculture.registerSpecies(ForestryBeeSpecies.LEPORINE, GENUS_FESTIVE, SPECIES_LEPORINE, false, new Color(0xfeff8f))
				.setBody(new Color(0x3cd757))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.SILKY), 0.30f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_EASTER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.MEADOWS, ForestryBeeSpecies.FOREST, 10)
							.restrictDateRange(Month.MARCH, 29, Month.APRIL, 15);
				})
				.setGlint(true)
				.setSecret(true);

		// Merry (Christmas secret)
		apiculture.registerSpecies(ForestryBeeSpecies.MERRY, GENUS_FESTIVE, SPECIES_MERRY, false, new Color(0xffffff))
				.setBody(new Color(0xd40000))
				.setTemperature(TemperatureType.ICY)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.FROZEN), 0.30f)
				.addProduct(CoreItems.CRAFTING_MATERIALS.stack(EnumCraftingMaterial.ICE_SHARD), 0.20f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_METATURNAL);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_SNOWING);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.WINTRY, ForestryBeeSpecies.FOREST, 10)
							.restrictDateRange(Month.DECEMBER, 21, Month.DECEMBER, 27);
				})
				.setGlint(true)
				.setSecret(true);

		// Tipsy (New Year's secret)
		apiculture.registerSpecies(ForestryBeeSpecies.TIPSY, GENUS_FESTIVE, SPECIES_TIPSY, false, new Color(0xffffff))
				.setBody(new Color(0xc219ec))
				.setTemperature(TemperatureType.ICY)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.FROZEN), 0.30f)
				.addProduct(CoreItems.CRAFTING_MATERIALS.stack(EnumCraftingMaterial.ICE_SHARD), 0.20f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_METATURNAL);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_DRUNKARD);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.WINTRY, ForestryBeeSpecies.MEADOWS, 10)
							.restrictDateRange(Month.DECEMBER, 27, Month.JANUARY, 2);
				})
				.setGlint(true)
				.setSecret(true);

		// todo Solstice (Winter Solstice secret)

		// Tricky (Halloween secret)
		apiculture.registerSpecies(ForestryBeeSpecies.TRICKY, GENUS_FESTIVE, SPECIES_TRICKY, false, new Color(0x49413B))
				.setBody(new Color(0xFF6A00))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.HONEY), 0.40f)
				.addProduct(new ItemStack(Items.COOKIE), 0.15f)
				.addSpecialty(new ItemStack(Items.SKELETON_SKULL), 0.02f)
				.addSpecialty(new ItemStack(Items.ZOMBIE_HEAD), 0.02f)
				.addSpecialty(new ItemStack(Items.CREEPER_HEAD), 0.02f)
				.addSpecialty(new ItemStack(Items.PLAYER_HEAD), 0.02f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_METATURNAL);
					genome.set(BeeChromosomes.TOLERATES_RAIN, true);
					genome.set(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_GOURD);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.SINISTER, ForestryBeeSpecies.COMMON, 10)
							.restrictDateRange(Month.OCTOBER, 15, Month.NOVEMBER, 3);
				})
				.setGlint(true)
				.setSecret(true);

		// todo Wattle (Thanksgiving secret)

		// todo Bissextile (Leap Year secret)

		// American (July 4th secret)
		apiculture.registerSpecies(ForestryBeeSpecies.PATRIOTIC, GENUS_FESTIVE, SPECIES_PATRIOTIC, true, new Color(0x0a3161))
				.setBody(new Color(0xb31942))
				.setStripes(new Color(0xffffff))
				.addProduct(new ItemStack(Items.GUNPOWDER), 0.45f)
				.addProduct(new FireworkProduct(0.20f))
				// todo specialty is a random firework
				.setGenome(genome -> {
					genome.set(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_UP_2);
					genome.set(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_UP_1);
					genome.set(BeeChromosomes.TERRITORY, ForestryAlleles.TERRITORY_LARGEST);
					genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_METATURNAL);
					// todo fireworks on 4th of July effect
					//genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_PATRIOTIC);
				})
				.addMutations(mutations -> {
					for (ResourceLocation parent : overworldHiveBees) {
						mutations.add(ForestryBeeSpecies.RURAL, parent, 15)
								.restrictDateRange(Month.JULY, 1, Month.JULY, 17);
					}
				})
				.setAuthority("TheDarkColour")
				.setSecret(true);

		// Rural
		apiculture.registerSpecies(ForestryBeeSpecies.RURAL, GENUS_AGRARIAN, SPECIES_RURAL, false, new Color(0xfeff8f))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.WHEATEN), 0.20f)
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.MEADOWS, ForestryBeeSpecies.DILIGENT, 12)
							.restrictBiomeType(Tags.Biomes.IS_PLAINS);
				});

		// Farmerly
		apiculture.registerSpecies(ForestryBeeSpecies.FARMERLY, GENUS_AGRARIAN, SPECIES_FARMERLY, true, new Color(0xD39728))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.WHEATEN), 0.27f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOW);
					genome.set(BeeChromosomes.TERRITORY, ForestryAlleles.TERRITORY_LARGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.RURAL, ForestryBeeSpecies.UNWEARY, 10)
							.restrictBiomeType(Tags.Biomes.IS_PLAINS);
				})
				.setAuthority("MysteriousAges");

		// Agrarian
		apiculture.registerSpecies(ForestryBeeSpecies.AGRARIAN, GENUS_AGRARIAN, SPECIES_AGRARIAN, true, new Color(0xFFCA75))
				.setBody(new Color(0xFFE047))
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.WHEATEN), 0.35f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOW);
					genome.set(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_2);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_FERTILE);
					genome.set(BeeChromosomes.TERRITORY, ForestryAlleles.TERRITORY_LARGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.FARMERLY, ForestryBeeSpecies.INDUSTRIOUS, 6)
							.restrictBiomeType(Tags.Biomes.IS_PLAINS);
				})
				.setGlint(true)
				.setAuthority("MysteriousAges");

		// Marshy
		apiculture.registerSpecies(ForestryBeeSpecies.MARSHY, GENUS_BOGGY, SPECIES_MARSHY, true, new Color(0x546626))
				.setHumidity(HumidityType.DAMP)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.MOSSY), 0.30f);

		// Miry
		apiculture.registerSpecies(ForestryBeeSpecies.MIRY, GENUS_BOGGY, SPECIES_MIRY, true, new Color(0x92AF42))
				.setHumidity(HumidityType.DAMP)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.MOSSY), 0.36f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.FERTILITY, ForestryAlleles.FERTILITY_4);
					genome.set(BeeChromosomes.TOLERATES_RAIN, true);
					genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_METATURNAL);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.MARSHY, ForestryBeeSpecies.NOBLE, 15)
							.restrictTemperature(TemperatureType.WARM)
							.restrictHumidity(HumidityType.DAMP);
				})
				.setAuthority("MysteriousAges");

		// Boggy
		apiculture.registerSpecies(ForestryBeeSpecies.BOGGY, GENUS_BOGGY, SPECIES_BOGGY, true, new Color(0x698948))
				.setHumidity(HumidityType.DAMP)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.MOSSY), 0.39f)
				.addSpecialty(CoreItems.PEAT.stack(), 0.08f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.TOLERATES_RAIN, true);
					genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_METATURNAL);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_MYCOPHILIC);
					genome.set(BeeChromosomes.TERRITORY, ForestryAlleles.TERRITORY_LARGER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.MARSHY, ForestryBeeSpecies.MIRY, 9)
							.restrictTemperature(TemperatureType.WARM)
							.restrictHumidity(HumidityType.DAMP);
				})
				.setAuthority("MysteriousAges");

		// Savanna
		apiculture.registerSpecies(ForestryBeeSpecies.SAVANNA, GENUS_SAVANNA, SPECIES_SAVANNA, true, new Color(0xb04e0f))
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.ARID)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.PARCHED), 0.20f)
				.addSpecialty(new ItemStack(Items.RED_SAND), 0.10f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORT);
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genome.set(BeeChromosomes.TERRITORY, ForestryAlleles.TERRITORY_LARGE);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_AGGRESSIVE);
				})
				.setAuthority("EnderiumSmith");

		// Argil
		apiculture.registerSpecies(ForestryBeeSpecies.ARGIL, GENUS_SAVANNA, SPECIES_ARGIL, true, new Color(0x96afd2))
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.ARID)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.KAOLIN), 0.30f)
				.addSpecialty(new ItemStack(Items.RED_SAND), 0.15f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORT);
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOW);
					genome.set(BeeChromosomes.TERRITORY, ForestryAlleles.TERRITORY_LARGE);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_SIFTER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.SAVANNA, ForestryBeeSpecies.DILIGENT, 15)
							.restrictTemperature(TemperatureType.WARM, TemperatureType.HOT)
							.restrictHumidity(HumidityType.ARID);
				})
				.setAuthority("EnderiumSmith");

		// Pride
		apiculture.registerSpecies(ForestryBeeSpecies.PRIDE, GENUS_SAVANNA, SPECIES_PRIDE, true, new Color(0x650021))
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.ARID)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.KAOLIN), 0.20f)
				.addSpecialty(BEE_COMBS.stack(EnumHoneyComb.MELLOW), 0.10f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTENED);
					genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
					genome.set(BeeChromosomes.TERRITORY, ForestryAlleles.TERRITORY_LARGER);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_HAKUNA_MATATA);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.SAVANNA, ForestryBeeSpecies.ARGIL, 9)
							.restrictBiomeType(ForestryTags.Biomes.SHATTERED_SAVANNA);
				})
				.setGlint(true)
				.setAuthority("EnderiumSmith");

		// Monastic (Only obtainable from villagers)
		apiculture.registerSpecies(ForestryBeeSpecies.MONASTIC, GENUS_MONASTIC, SPECIES_MONASTIC, false, new Color(0x42371c))
				.setJubilance(HermitBeeJubilance.INSTANCE)
				.addProduct(BEE_COMBS.stack(EnumHoneyComb.WHEATEN), 0.30f)
				.addSpecialty(BEE_COMBS.stack(EnumHoneyComb.MELLOW), 0.10f);

		// Secluded
		apiculture.registerSpecies(ForestryBeeSpecies.SECLUDED, GENUS_MONASTIC, SPECIES_SECLUDED, true, new Color(0x7b6634))
				.setJubilance(HermitBeeJubilance.INSTANCE)
				.addSpecialty(BEE_COMBS.stack(EnumHoneyComb.MELLOW), 0.20f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_FASTEST);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.MONASTIC, ForestryBeeSpecies.AUSTERE, 12);
				});

		// Hermitic
		apiculture.registerSpecies(ForestryBeeSpecies.HERMITIC, GENUS_MONASTIC, SPECIES_HERMITIC, false, new Color(0xffd46c))
				.setJubilance(HermitBeeJubilance.INSTANCE)
				.addSpecialty(BEE_COMBS.stack(EnumHoneyComb.MELLOW), 0.20f)
				.setGenome(genome -> {
					genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_FASTEST);
					genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_REPULSION);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryBeeSpecies.MONASTIC, ForestryBeeSpecies.SECLUDED, 8);
				})
				.setGlint(true);
	}
}
