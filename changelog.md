## Forestry 2.1.2
- Fix crash with Diagonal Fences mod (#145)
- Fix integer alleles always being recessive (#146)
- Fixed Naturalist chests not closing when KubeJS is installed (#148)
- Relax the version requirement on Forge to allow launching Forestry with NeoForge, no guarantees of stability (#147)
- Fix Engine chaining not working
- Electron tube tooltips are now always shown

## Forestry 2.1.1
- Fix IIndividualHandlerItem methods returning individuals for empty stacks that were decremented
- Added method in IAlleleManager to get IChromosome instances by registry name
- Fix butterfly humidity tolerance having the same range as temperature tolerance
- Chromosomes now have translation keys and display names
- Fixed saplings not growing (#142)
- Allow overriding Hive generation chances in IForestryPlugin and KubeJS
- Add partial KubeJS support for bees, documentation coming soon on [ReadTheDocs](https://forestrydocs.readthedocs.io/en/latest/)

## Forestry 2.1.0
- Fixed typo that cut off black order on the right side of the Portable Analyzer GUI
- Add IActivityType bee chromosome for more complex sleep patterns (#97)
- Add missing recipes for Stripped Wood, Fireproof Wood, and Fireproof Stripped Wood

## Forestry 2.0.5
- Fixed KubeJS server event error (#138)
- Add stripped log/wood variants for Forestry wood types (#118)

## Forestry 2.0.4
- Fixed locked slots not displaying properly in the Escritoire
- Fixed incorrect species count in Naturalist chest (#136)
- Fix serverside crash with Merry bees (#137)
- Fix missing textures for Snowing effect particles (#120)
- Fix Escritoire bounding box having a solid top (#119)

## Forestry 2.0.3
- Fixed AbstractMethodError with farm and alveary (#131, #132)

## Forestry 2.0.2
- Fixed error when joining server (#130)

## Forestry 2.0.1
- Fixed crash with Alveary controller (#129)

## Forestry 2.0.0
- Ported to Forge 1.20.1.
- Reorganized creative tabs.
- Renamed Forestry's Cherry species to Hill Cherry.
- Added Vanilla's Cherry trees to Forestry under the Cherry Blossom species.
- Fixed non-default height allele being ignored by saplings.
- Fixed rainmaker JEI recipe category icon being a rain tank instead of a rainmaker.
- Updated Ebony log texture.
- Updated Savanna hive texture.
- Removed the unused Wax Cast item that used to be for making Stained Glass.
- A bunch of other things!

## Forestry 1.0.14
- Add Falkory's textures (#33)
- Allow Cocoa plantations to automatically replant, thanks to ACGaming
- Fix erroneous letter NBT, thanks to ACGaming

## Forestry 1.0.13
- Fixed climate display in Alveary when Fan/Heater/Hygroregulator were used (#99)
- Changed Apiarist villager to use Escritoire as workstation instead of Apiary (#98)

## Forestry 1.0.12
- Fixed crash when inserting fuels into Biogas engine (#105)
- Fix Wood Pile recipe conflict with 1.13 Wood recipes (#104)
- Rename "Wood Pile" to "Log Pile"

## Forestry 1.0.11
- Fixed world generation crash with fruit pods (#102)
- Fixed incorrect translation of ru_ru.json (#101)
- Add additional constructor to GuiForestryTitled that accepts ResourceLocation
- Fix Alvearies and Multifarms not dropping their inventories when destroyed (#100)
- Add ISpectacleBlock API so that addon mods can have their blocks highlighted by players wearing spectacles

## Forestry 1.0.10
- Fixed Grafter for Forestry trees (#95)
- Fixed ru_ru, thanks to Quarkrus (#93)
- Fixed non-default Girths not working with genetically modified saplings (#96)
- Fix analyzer display for Haploid drones, thanks to EnderiumSmith (#94)
- Implement weakly inherxited chromosomes for temperature & humidity tolerances and other traits, thanks to EnderiumSmith (#94)
- Fix hive generation, thanks to EnderiumSmith (#94)
- Fix Mycophilic effect replacing Tall grass instead of Grass Blocks

## Forestry 1.0.9
- Added API for using custom bee species textures. (#30)
- Add Analyzer widget to the Alveary GUI (#66)
- Fixes hives at Taiga villages ignoring the climate
- Fixes inaccurate climate information in GUIs after leaving/rejoining the server/dimension (#73)
- Fixes apiaries not reacting to changes in climate as a result of the biome changing
- Make wild hives extensible by addon mods
- Fix fruits not dropping from tree leaves on decay (#61)
- Fix leaves with non-default genomes not dropping any saplings (#83)
- Fix pod fruits not dropping when their supporting trunk is destroyed
- Fix Alveary Hygroregulator corrupting Alveary inventories (#91)

## Forestry 1.0.8 (Breaking change, backup your worlds)
- Remove bottler recipes from JEI
- Fix spectacles to show pollinated leaves and wild hives (#14)
- Fix double flower bee placement (#81)
- Creative hive frames now show in JEI and have epic rarity
- Added the Savanna bee line to replace Modest bees in the Savanna biomes, thanks to EnderiumSmith (#76)
- Finally fixed haploid mode, thanks to EnderiumSmith (#78)
- Fixed end sky always being obstructed, thanks to EnderiumSmith (#71)
- Forestry Beeswax can now be used to wax copper blocks, like Vanilla's honeycomb, thanks to EnderiumSmith
- Set default temperature tolerance of Forest bees to `TOLERANCE_DOWN_1` to facilitate breeding with Wintry, thanks to EnderiumSmith
- Set default humidity tolerance of Meadows bees to `TOLERANCE_DOWN_1` to work in the NORMAL / DRY climate, thanks to EnderiumSmith
- Cultivated line can now also be bred using Valiant, Savanna, and Ended, thanks to EnderiumSmith
- Added tags for configuring valid spawn blocks for Snowy and Desert hives.
- Fix zh_cn.json, thanks to Obsoletes
- Remove milk fluid (#46)

## Forestry 1.0.7
- Sort products displayed in JEI by their chances
- Fix mating behaviour of unmated queens (ex. from Creative Menu)
- Fix Apiaries with frames producing more than Alvearies (#79)
- Fix blocks not dropping their inventories if blown up by Creeper (#52)
- Fix incorrect implementation of Haploid drone breeding option (#78)
- Fix creative frames having mixed up tooltips
- Fixed species mutations and biome-restricted bee mutations
- Fixed Fertile bee effect (#72)
- Fixed snowy hives not spawning in Grove biomes
- Fix fruits not spawning on tree leaves of non-default genome saplings (#82)
- Fix hives not spawning below y=0 (affects some custom/superflat worlds)

## Forestry 1.0.6
- Fixed wild beehives not being able to spawn in snowy areas (#56)
- Fix fruit squeezer recipes to use tags + correct mulch rates (#59)
- Fix Miner's bag not accepting raw ores (#58)
- Make all biomes in `#minecraft:is_nether` tag marked as having HELLISH climate (#65)
- The Phantasmal line (Ended, Spectral, Phantasmal) is now actually nocturnal and only works during the night time (unless given the NEVER_SLEEPS allele).
- Added Creative Frames for debugging Forestry mutations.
- Hive frames can now stack up to 64.
- Biomes in the end (tagged as `#minecraft:is_end`) will now use COLD / ARID climate.
- Fix Forester's Manual not working on dedicated server (#57, #60)
- Fix clientside console spam when opening menus using Database widgets (#74)
- Fix mutation date formatting to be more human-readable (#69)
- Add JEI support for bee/tree/butterfly mutations and products

## Forestry 1.0.5
- Fixed wild beehives not having particle effects
- Fix biogas engine lava tank accepting any fluid
- Fix still not working
- Added `forestry_fruits` tag to `forge:fruits` item tag + fruit tags for each fruit added by Forestry:
  - `forge:fruits/cherry` for Cherry
  - `forge:fruits/walnut` for Walnut
  - `forge:fruits/chestnut` for Chestnut
  - `forge:fruits/lemon` for Lemon, the same tag used by Fruits Delight
  - `forge:fruits/plum` for Plum
  - `forge:fruits/date` for Date
  - `forge:fruits/papaya` for Papaya
- Replaced ICheckPollinatable and IPollinatable with new IPollenType API.
  - A pollen type allows Forestry bees, butterflies, and Alveary Sieves to handle non-tree pollen types.
    To use, register a new IPollenType in `IForestryPlugin#registerPollen`.
- Fixed bug where "default" variants of Forestry leaves would never be used
- Fixed bug where fruit ripeness would sometimes not persist
- ACTUALLY make saplings and other items usable in the composter (#35)
- Increase energy storage of Bottler, Still, Fermenter by 10x
- Fixed bug where species would be able to mutate with themselves.
- Add haploid breeding option
- Fixed nether biome climates
- Savannah biomes are now WARM/ARID instead of HOT/ARID, making their climate distinct from the desert biomes.
- Fix pipette textures
- Fix bees not planting flowers (#53)
- Fix bronze crafting recipe only producing 1 ingot instead of 4 (#54)

## Forestry 1.0.4
- Added modify genome command. Can be used like `/forestry bee modify <chromosome> <allele> <both|dominant|recessive>` to
  modify the genome of the player's currently held genetic item (bees, saplings, butterflies)
- Fixed loading of Chinese (simplified) language file (#44)
- Fixed many translation keys in Chinese (simplified) language file (#45)
- Fixed Apiarist chest menu not displaying queens/princesses/drones in menu (#40)
- Unused honeycombs are no longer obtainable in survival, from village houses or trades (#42)
- Fixed fluid attributes and use honey/milk tags where possible
- Fixed liquid container menu desync (#29, #41)
- Fixed wild beehives dropping bees without a scoop
- Fixed legacy farms not planting from slots correctly (#43)

## Forestry 1.0.3
- Fixed saplings not dropping themselves (#36)
- Fixed villager professions not working (#38)
- Added new planks textures for all wood types
- Make saplings and other items usable in the composter (#35)
- Fix Forest and Tropical wild hive generation (#34, #37)
- Add TickHelper utility as public API
- Move factory methods from IBeeSpeciesType to IHiveManager for easier access.

## Forestry 1.0.2
- Fixed Naturalist backpack crash and bugs
- Fixed incorrect Apiarist backpack recipe
- Fixed Forestry wood/log naming to be consistent with Vanilla
- Fixed Analyzer not recognizing the vanilla Jungle Sapling
- Add missing recipes for Forestry wood/bark blocks
- Added Arborist backpack
- Add new textures for Apiary and Bee House
- Add new textures for Pine and Cherry planks (since they match Bee house and Apiary/Alveary planks)

## Forestry 1.0.1
- Add butterfly mating recipe
- Fix reobf crash with IFeatureSubtype
- Fix ore names and deepslate textures

## Forestry 1.0.0
- Initial release