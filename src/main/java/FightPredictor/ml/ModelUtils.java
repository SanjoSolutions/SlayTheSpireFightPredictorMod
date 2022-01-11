package FightPredictor.ml;

import FightPredictor.FightPredictor;
import com.badlogic.gdx.Gdx;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.RunModStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class ModelUtils {
    private static final int MAX_AMOUNT_PER_CARD_TYPE = 10;
    private static final int MAX_UPGRADES_PER_CARD = 1;

    private static int cardCount;
    private static int relicCount;
    private static int encountersCount;

    private static Map<String, Integer> cardEncoding;
    private static Map<String, Integer> relicEncoding;
    private static Map<String, Integer> encounterEncoding;

    public static void init() {
        List<String> allCards = new ArrayList<>(Arrays.asList(
            "Immolate",
            "Grand Finale",
            "Regret",
            "Crippling Poison",
            "Storm",
            "DeusExMachina",
            "A Thousand Cuts",
            "Spot Weakness",
            "Genetic Algorithm",
            "Go for the Eyes",
            "Zap",
            "Steam Power",
            "Wound",
            "Core Surge",
            "Fission",
            "Writhe",
            "Beta",
            "Hello World",
            "Creative AI",
            "Dark Shackles",
            "Glass Knife",
            "Consecrate",
            "Cloak And Dagger",
            "BowlingBash",
            "Underhanded Strike",
            "Anger",
            "Storm of Steel",
            "WheelKick",
            "Cleave",
            "Ball Lightning",
            "Warcry",
            "Sunder",
            "Glacier",
            "Inflame",
            "Sadistic Nature",
            "J.A.X.",
            "Offering",
            "Vengeance",
            "FlyingSleeves",
            "Exhume",
            "Streamline",
            "Wireheading",
            "Consume",
            "Power Through",
            "Dual Wield",
            "Deadly Poison",
            "Leg Sweep",
            "PanicButton",
            "Flex",
            "Redo",
            "AscendersBane",
            "Dagger Spray",
            "Bullet Time",
            "Fusion",
            "Catalyst",
            "Sanctity",
            "Halt",
            "Reaper",
            "Shiv",
            "Bane",
            "Tactician",
            "JustLucky",
            "Infernal Blade",
            "After Image",
            "Unload",
            "FlurryOfBlows",
            "Blade Dance",
            "Deflect",
            "Compile Driver",
            "TalkToTheHand",
            "BattleHymn",
            "Protect",
            "Trip",
            "Indignation",
            "Dagger Throw",
            "Amplify",
            "ThirdEye",
            "Brutality",
            "Night Terror",
            "WindmillStrike",
            "Iron Wave",
            "Reboot",
            "Reckless Charge",
            "All For One",
            "ForeignInfluence",
            "Decay",
            "FameAndFortune",
            "Tools of the Trade",
            "Aggregate",
            "Expertise",
            "Dramatic Entrance",
            "Hemokinesis",
            "Blizzard",
            "Chaos",
            "LiveForever",
            "Intimidate",
            "Echo Form",
            "Necronomicurse",
            "Juggernaut",
            "Choke",
            "Caltrops",
            "Impatience",
            "DevaForm",
            "Poisoned Stab",
            "The Bomb",
            "Blur",
            "LikeWater",
            "Body Slam",
            "True Grit",
            "Insight",
            "Setup",
            "Barrage",
            "Crescendo",
            "SpiritShield",
            "Blood for Blood",
            "Impervious",
            "ClearTheMind",
            "EmptyBody",
            "Shrug It Off",
            "Meteor Strike",
            "Establishment",
            "Fasting2",
            "Clash",
            "Stack",
            "Miracle",
            "CarveReality",
            "Wallop",
            "Thunderclap",
            "Rebound",
            "Flame Barrier",
            "Seek",
            "Endless Agony",
            "WreathOfFlame",
            "Collect",
            "SashWhip",
            "Wraith Form v2",
            "Melter",
            "Berserk",
            "Pummel",
            "Burning Pact",
            "Riddle With Holes",
            "Metallicize",
            "Self Repair",
            "Pommel Strike",
            "Pain",
            "Rainbow",
            "InnerPeace",
            "Burst",
            "Acrobatics",
            "Adaptation",
            "Loop",
            "Blind",
            "Doppelganger",
            "Skewer",
            "Omniscience",
            "Envenom",
            "Chill",
            "Adrenaline",
            "Quick Slash",
            "Twin Strike",
            "BootSequence",
            "Parasite",
            "Bash",
            "RitualDagger",
            "Gash",
            "Wish",
            "Clothesline",
            "DeceiveReality",
            "MentalFortress",
            "Shockwave",
            "BecomeAlmighty",
            "Rampage",
            "Coolheaded",
            "Static Discharge",
            "Alpha",
            "Heatsinks",
            "Vault",
            "Bandage Up",
            "Scrawl",
            "Sever Soul",
            "Eruption",
            "Whirlwind",
            "Bite",
            "LessonLearned",
            "Secret Technique",
            "Calculated Gamble",
            "Tempest",
            "Combust",
            "Deep Breath",
            "Doubt",
            "Escape Plan",
            "CutThroughFate",
            "ReachHeaven",
            "Finisher",
            "Dark Embrace",
            "Die Die Die",
            "Well Laid Plans",
            "Ragnarok",
            "Buffer",
            "Electrodynamics",
            "FearNoEvil",
            "Seeing Red",
            "SandsOfTime",
            "Smite",
            "Violence",
            "Disarm",
            "Turbo",
            "Panache",
            "Undo",
            "Fiend Fire",
            "Terror",
            "Force Field",
            "Dazed",
            "Barricade",
            "Armaments",
            "Havoc",
            "Secret Weapon",
            "Apotheosis",
            "Sweeping Beam",
            "Feel No Pain",
            "FTL",
            "Rip and Tear",
            "Darkness",
            "Corruption",
            "Heel Hook",
            "Blasphemy",
            "Injury",
            "Double Energy",
            "Rage",
            "Headbutt",
            "Machine Learning",
            "Reinforced Body",
            "Limit Break",
            "Entrench",
            "Noxious Fumes",
            "Infinite Blades",
            "Phantasmal Killer",
            "WaveOfTheHand",
            "Malaise",
            "Conserve Battery",
            "Mayhem",
            "Reflex",
            "Study",
            "Expunger",
            "Sentinel",
            "Survivor",
            "Wild Strike",
            "HandOfGreed",
            "Meditate",
            "Eviscerate",
            "Flash of Steel",
            "Battle Trance",
            "Forethought",
            "Dualcast",
            "Auto Shields",
            "Perseverance",
            "Swivel",
            "Heavy Blade",
            "Slimed",
            "Clumsy",
            "Biased Cognition",
            "Searing Blow",
            "Devotion",
            "Reprogram",
            "Hologram",
            "Corpse Explosion",
            "Second Wind",
            "Enlightenment",
            "Purity",
            "Panacea",
            "Lockon",
            "Dash",
            "Worship",
            "Conclude",
            "ThroughViolence",
            "Transmutation",
            "Ghostly",
            "Backstab",
            "Chrysalis",
            "FollowUp",
            "Void",
            "Scrape",
            "Feed",
            "Vigilance",
            "Rupture",
            "Venomology",
            "Discovery",
            "Beam Cell",
            "Leap",
            "CurseOfTheBell",
            "Bouncing Flask",
            "PathToVictory",
            "Bludgeon",
            "Finesse",
            "Slice",
            "Recycle",
            "Backflip",
            "Outmaneuver",
            "Bloodletting",
            "Brilliance",
            "Magnetism",
            "Concentrate",
            "Skim",
            "White Noise",
            "Capacitor",
            "Cold Snap",
            "CrushJoints",
            "Master of Strategy",
            "Flechettes",
            "Tantrum",
            "Perfected Strike",
            "Thunder Strike",
            "Carnage",
            "Masterful Stab",
            "Nirvana",
            "Evaluate",
            "Prepared",
            "Good Instincts",
            "Dropkick",
            "Swift Strike",
            "Normality",
            "MasterReality",
            "Omega",
            "Hyperbeam",
            "Accuracy",
            "Sword Boomerang",
            "EmptyMind",
            "Pride",
            "Defragment",
            "Jack Of All Trades",
            "Demon Form",
            "Fire Breathing",
            "Ghostly Armor",
            "Weave",
            "Safety",
            "Metamorphosis",
            "Prostrate",
            "SignatureMove",
            "Uppercut",
            "PiercingWail",
            "Mind Blast",
            "Neutralize",
            "Multi-Cast",
            "Shame",
            "Doom and Gloom",
            "Evolve",
            "Double Tap",
            "Sucker Punch",
            "Burn",
            "ConjureBlade",
            "Judgement",
            "Footwork",
            "Steam",
            "Distraction",
            "Dodge and Roll",
            "Thinking Ahead",
            "EmptyFist",
            "All Out Attack",
            "Flying Knee",
            "Predator",
            "Pray",
            "Madness",
            "Strike",
            "Defend"
        ));
        List<String> allRelics = new ArrayList<>(Arrays.asList(
            "Burning Blood",
            "Cracked Core",
            "PureWater",
            "Ring of the Snake",
            "Akabeko",
            "Anchor",
            "Ancient Tea Set",
            "Art of War",
            "Bag of Marbles",
            "Bag of Preparation",
            "Blood Vial",
            "Bronze Scales",
            "Centennial Puzzle",
            "CeramicFish",
            "Damaru",
            "DataDisk",
            "Dream Catcher",
            "Happy Flower",
            "Juzu Bracelet",
            "Lantern",
            "MawBank",
            "MealTicket",
            "Nunchaku",
            "Oddly Smooth Stone",
            "Omamori",
            "Orichalcum",
            "Pen Nib",
            "Potion Belt",
            "PreservedInsect",
            "Red Skull",
            "Regal Pillow",
            "Smiling Mask",
            "Snake Skull",
            "Strawberry",
            "Boot",
            "Tiny Chest",
            "Toy Ornithopter",
            "Vajra",
            "War Paint",
            "Whetstone",
            "Blue Candle",
            "Bottled Flame",
            "Bottled Lightning",
            "Bottled Tornado",
            "Darkstone Periapt",
            "Yang",
            "Eternal Feather",
            "Frozen Egg 2",
            "Cables",
            "Gremlin Horn",
            "HornCleat",
            "InkBottle",
            "Kunai",
            "Letter Opener",
            "Matryoshka",
            "Meat on the Bone",
            "Mercury Hourglass",
            "Molten Egg 2",
            "Mummified Hand",
            "Ninja Scroll",
            "Ornamental Fan",
            "Pantograph",
            "Paper Crane",
            "Paper Frog",
            "Pear",
            "Question Card",
            "Self Forming Clay",
            "Shuriken",
            "Singing Bowl",
            "StrikeDummy",
            "Sundial",
            "Symbiotic Virus",
            "TeardropLocket",
            "The Courier",
            "Toxic Egg 2",
            "White Beast Statue",
            "Bird Faced Urn",
            "Calipers",
            "CaptainsWheel",
            "Champion Belt",
            "Charon's Ashes",
            "CloakClasp",
            "Dead Branch",
            "Du-Vu Doll",
            "Emotion Chip",
            "FossilizedHelix",
            "Gambling Chip",
            "Ginger",
            "Girya",
            "GoldenEye",
            "Ice Cream",
            "Incense Burner",
            "Lizard Tail",
            "Magic Flower",
            "Mango",
            "Old Coin",
            "Peace Pipe",
            "Pocketwatch",
            "Prayer Wheel",
            "Shovel",
            "StoneCalendar",
            "The Specimen",
            "Thread and Needle",
            "Tingsha",
            "Torii",
            "Tough Bandages",
            "TungstenRod",
            "Turnip",
            "Unceasing Top",
            "WingedGreaves",
            "Astrolabe",
            "Black Blood",
            "Black Star",
            "Busted Crown",
            "Calling Bell",
            "Coffee Dripper",
            "Cursed Key",
            "Ectoplasm",
            "Empty Cage",
            "FrozenCore",
            "Fusion Hammer",
            "HolyWater",
            "HoveringKite",
            "Inserter",
            "Mark of Pain",
            "Nuclear Battery",
            "Pandora's Box",
            "Philosopher's Stone",
            "Ring of the Serpent",
            "Runic Cube",
            "Runic Dome",
            "Runic Pyramid",
            "SacredBark",
            "SlaversCollar",
            "Snecko Eye",
            "Sozu",
            "Tiny House",
            "Velvet Choker",
            "VioletLotus",
            "WristBlade",
            "Bloody Idol",
            "CultistMask",
            "Enchiridion",
            "FaceOfCleric",
            "Golden Idol",
            "GremlinMask",
            "Mark of the Bloom",
            "MutagenicStrength",
            "Nloth's Gift",
            "NlothsMask",
            "Necronomicon",
            "NeowsBlessing",
            "Nilry's Codex",
            "Odd Mushroom",
            "Red Mask",
            "Spirit Poop",
            "SsserpentHead",
            "WarpedTongs",
            "Brimstone",
            "Cauldron",
            "Chemical X",
            "ClockworkSouvenir",
            "DollysMirror",
            "Frozen Eye",
            "HandDrill",
            "Lee's Waffle",
            "Medical Kit",
            "Melange",
            "Membership Card",
            "OrangePellets",
            "Orrery",
            "PrismaticShard",
            "Runic Capacitor",
            "Sling",
            "Strange Spoon",
            "TheAbacus",
            "Toolbox",
            "TwistedFunnel",
            "Black Blood",
            "Brimstone",
            "Burning Blood",
            "Champion Belt",
            "Charon's Ashes",
            "Magic Flower",
            "Mark of Pain",
            "Paper Frog",
            "Red Skull",
            "Runic Cube",
            "Self Forming Clay",
            "HoveringKite",
            "Ninja Scroll",
            "Paper Crane",
            "Ring of the Serpent",
            "Ring of the Snake",
            "Snake Skull",
            "The Specimen",
            "Tingsha",
            "Tough Bandages",
            "TwistedFunnel",
            "WristBlade",
            "Cracked Core",
            "DataDisk",
            "Emotion Chip",
            "FrozenCore",
            "Cables",
            "Inserter",
            "Nuclear Battery",
            "Runic Capacitor",
            "Symbiotic Virus",
            "CloakClasp",
            "Damaru",
            "GoldenEye",
            "HolyWater",
            "Melange",
            "PureWater",
            "VioletLotus",
            "TeardropLocket",
            "Yang"
        ));
        List<String> allEncounters = new ArrayList<>(Arrays.asList(
            "Blue Slaver",
            "Cultist",
            "Jaw Worm",
            "2 Louse",
            "Small Slimes",

            "Gremlin Gang",
            "Large Slime",
            "Looter",
            "Lots of Slimes",
            "Exordium Thugs",
            "Exordium Wildlife",
            "Red Slaver",
            "3 Louse",
            "2 Fungi Beasts",

            "Gremlin Nob",
            "Lagavulin",
            "3 Sentries",

            "Lagavulin Event",
            "The Mushroom Lair",

            "The Guardian",
            "Hexaghost",
            "Slime Boss",

            "Chosen",
            "Shell Parasite",
            "Spheric Guardian",
            "3 Byrds",
            "2 Thieves",

            "Chosen and Byrds",
            "Sentry and Sphere",
            "Snake Plant",
            "Snecko",
            "Centurion and Healer",
            "Cultist and Chosen",
            "3 Cultists",
            "Shelled Parasite and Fungi",

            "Gremlin Leader",
            "Slavers",
            "Book of Stabbing",

            "Masked Bandits",
            "Colosseum Nobs",
            "Colosseum Slavers",

            "Automaton",
            "Champ",
            "Collector",

            "Orb Walker",
            "3 Darklings",
            "3 Shapes",

            "Transient",
            "4 Shapes",
            "Maw",
            "Jaw Worm Horde",
            "Sphere and 2 Shapes",
            "Spire Growth",
            "Writhing Mass",

            "Giant Head",
            "Nemesis",
            "Reptomancer",

            "Mysterious Sphere",
            "Mind Bloom Boss Battle",
            "2 Orb Walkers",

            "Awakened One",
            "Donu and Deca",
            "Time Eater",

            "Shield and Spear",

            "The Heart"
        ));

        cardEncoding = putEncodings(allCards);
        relicEncoding = putEncodings(allRelics);
        encounterEncoding = putEncodings(allEncounters);

        cardCount = allCards.size();
        relicCount = allRelics.size();
        encountersCount = allEncounters.size();
    }

    private static Map<String, Integer> putEncodings(List<String> objsToEncode) {
        Map<String, Integer> enc = new HashMap<>();
        for (int i = 0; i < objsToEncode.size(); i++) {
            enc.put(objsToEncode.get(i), i);
        }
        return enc;
    }

    private static String generalizeStrikeDefend(String cardId) {
        if (cardId.startsWith("Strike_") || cardId.startsWith("Defend_")) {
            return cardId.replaceFirst("_.", "");
        }
        return cardId;
    }

    /**
     * Returns the input vector reflecting the current game state. The encounter is set to
     * the last combat encounter. Use changeEncounter to change the encounter.
     *
     * @return input vector
     */
    public static float[] getBaseInputVector() {
        List<AbstractCard> masterDeck = AbstractDungeon.player.masterDeck.group;
        List<AbstractRelic> masterRelics = AbstractDungeon.player.relics;
        String encounter;
        if (AbstractDungeon.lastCombatMetricKey != null) {
            encounter = AbstractDungeon.lastCombatMetricKey;
        } else {
            encounter = "2 Louse"; // For occasional crash from save and load
        }
        int maxHP = AbstractDungeon.player.maxHealth;
        int enteringHP = AbstractDungeon.player.currentHealth;
        int ascension = AbstractDungeon.ascensionLevel;
        boolean potionUsed = false;

        return getInputVector(masterDeck, masterRelics, encounter, maxHP, enteringHP, ascension, potionUsed);
    }

    /**
     * Returns an input vector without an encounter encoded.
     *
     * @param masterDeck
     * @param masterRelics
     * @param maxHP
     * @param enteringHP
     * @param ascension
     * @param potionUsed
     * @return
     */
    public static float[] getInputVectorNoEncounter(List<AbstractCard> masterDeck, List<AbstractRelic> masterRelics,
                                                    int maxHP, int enteringHP, int ascension, boolean potionUsed) {
        float[] vector = getInputVector(masterDeck, masterRelics, "2 Louse", maxHP, enteringHP, ascension, potionUsed);
        Arrays.fill(
            vector,
            (cardCount + MAX_UPGRADES_PER_CARD) * MAX_AMOUNT_PER_CARD_TYPE + relicCount,
            (cardCount + MAX_UPGRADES_PER_CARD) * MAX_AMOUNT_PER_CARD_TYPE + relicCount + encountersCount,
            0.0f
        );
        return vector;
    }

    /**
     * Returns an input vector that can be used by the Model
     *
     * @param masterDeck
     * @param masterRelics
     * @param encounter
     * @param maxHP
     * @param enteringHP
     * @param ascension
     * @param potionUsed
     * @return input vector
     */
    public static float[] getInputVector(List<AbstractCard> masterDeck, List<AbstractRelic> masterRelics, String encounter,
                                         int maxHP, int enteringHP, int ascension, boolean potionUsed) {
        float[] outputVector = new float[Model.NUM_FEATURES];

        List<String> relicIds = masterRelics.stream()
            .map(r -> r.relicId)
            .collect(Collectors.toList());

        for (AbstractCard card : masterDeck) {
            int index = cardEncoding.get(generalizeStrikeDefend(card.cardID));
            for (
                int offset = 0;
                offset < MAX_UPGRADES_PER_CARD * MAX_AMOUNT_PER_CARD_TYPE;
                offset += MAX_UPGRADES_PER_CARD
            ) {
                if (outputVector[index + offset] == 0) {
                    outputVector[index + offset] = 1;
                    if (card.upgraded) {
                        outputVector[index + offset + 1] = 1;
                    }
                    break;
                }
            }
        }
        for (String rID : relicIds) {
            outputVector[(cardCount + MAX_UPGRADES_PER_CARD) * MAX_AMOUNT_PER_CARD_TYPE + relicEncoding.get(rID)] += 1;
        }
        outputVector[(cardCount + MAX_UPGRADES_PER_CARD) * MAX_AMOUNT_PER_CARD_TYPE + relicCount + encounterEncoding.get(encounter)] += 1;

        int remainingOffset = (cardCount + MAX_UPGRADES_PER_CARD) * MAX_AMOUNT_PER_CARD_TYPE + relicCount + encountersCount;
        outputVector[remainingOffset] = maxHP / 100f;
        outputVector[remainingOffset + 1] = enteringHP / 100f;
        outputVector[remainingOffset + 2] = ascension / 20f;
        outputVector[remainingOffset + 3] = potionUsed ? 1 : 0;

        return outputVector;
    }

    public static float[] changeEncounter(float[] vector, String encounter) {
        float[] vecCopy = Arrays.copyOf(vector, vector.length);
        Arrays.fill(
            vecCopy,
            (cardCount + MAX_UPGRADES_PER_CARD) * MAX_AMOUNT_PER_CARD_TYPE + relicCount,
            (cardCount + MAX_UPGRADES_PER_CARD) * MAX_AMOUNT_PER_CARD_TYPE + relicCount + encountersCount,
            0.0f
        );
        int index = (cardCount + MAX_UPGRADES_PER_CARD) * MAX_AMOUNT_PER_CARD_TYPE + relicCount + encounterEncoding.get(encounter);
        vecCopy[index] += 1;
        return vecCopy;
    }
}
