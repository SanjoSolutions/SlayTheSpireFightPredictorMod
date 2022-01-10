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

    private static int cardCount = 714;
    private static int relicCount = 179;
    private static int encountersCount = 73;

    private static Map<String, Integer> cardEncoding;
    private static Map<String, Integer> relicEncoding;
    private static Map<String, Integer> encounterEncoding;

    private static List<Float> inputScales;

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

            // upgrades
            "Immolate+1",
            "Grand Finale+1",
            "Regret+1",
            "Crippling Poison+1",
            "Storm+1",
            "DeusExMachina+1",
            "A Thousand Cuts+1",
            "Spot Weakness+1",
            "Genetic Algorithm+1",
            "Go for the Eyes+1",
            "Zap+1",
            "Steam Power+1",
            "Wound+1",
            "Core Surge+1",
            "Fission+1",
            "Writhe+1",
            "Beta+1",
            "Hello World+1",
            "Creative AI+1",
            "Dark Shackles+1",
            "Glass Knife+1",
            "Consecrate+1",
            "Cloak And Dagger+1",
            "BowlingBash+1",
            "Underhanded Strike+1",
            "Anger+1",
            "Storm of Steel+1",
            "WheelKick+1",
            "Cleave+1",
            "Ball Lightning+1",
            "Warcry+1",
            "Sunder+1",
            "Glacier+1",
            "Inflame+1",
            "Sadistic Nature+1",
            "J.A.X.+1",
            "Offering+1",
            "Vengeance+1",
            "FlyingSleeves+1",
            "Exhume+1",
            "Streamline+1",
            "Wireheading+1",
            "Consume+1",
            "Power Through+1",
            "Dual Wield+1",
            "Deadly Poison+1",
            "Leg Sweep+1",
            "PanicButton+1",
            "Flex+1",
            "Redo+1",
            "AscendersBane+1",
            "Dagger Spray+1",
            "Bullet Time+1",
            "Fusion+1",
            "Catalyst+1",
            "Sanctity+1",
            "Halt+1",
            "Reaper+1",
            "Shiv+1",
            "Bane+1",
            "Tactician+1",
            "JustLucky+1",
            "Infernal Blade+1",
            "After Image+1",
            "Unload+1",
            "FlurryOfBlows+1",
            "Blade Dance+1",
            "Deflect+1",
            "Compile Driver+1",
            "TalkToTheHand+1",
            "BattleHymn+1",
            "Protect+1",
            "Trip+1",
            "Indignation+1",
            "Dagger Throw+1",
            "Amplify+1",
            "ThirdEye+1",
            "Brutality+1",
            "Night Terror+1",
            "WindmillStrike+1",
            "Iron Wave+1",
            "Reboot+1",
            "Reckless Charge+1",
            "All For One+1",
            "ForeignInfluence+1",
            "Decay+1",
            "FameAndFortune+1",
            "Tools of the Trade+1",
            "Aggregate+1",
            "Expertise+1",
            "Dramatic Entrance+1",
            "Hemokinesis+1",
            "Blizzard+1",
            "Chaos+1",
            "LiveForever+1",
            "Intimidate+1",
            "Echo Form+1",
            "Necronomicurse+1",
            "Juggernaut+1",
            "Choke+1",
            "Caltrops+1",
            "Impatience+1",
            "DevaForm+1",
            "Poisoned Stab+1",
            "The Bomb+1",
            "Blur+1",
            "LikeWater+1",
            "Body Slam+1",
            "True Grit+1",
            "Insight+1",
            "Setup+1",
            "Barrage+1",
            "Crescendo+1",
            "SpiritShield+1",
            "Blood for Blood+1",
            "Impervious+1",
            "ClearTheMind+1",
            "EmptyBody+1",
            "Shrug It Off+1",
            "Meteor Strike+1",
            "Establishment+1",
            "Fasting2+1",
            "Clash+1",
            "Stack+1",
            "Miracle+1",
            "CarveReality+1",
            "Wallop+1",
            "Thunderclap+1",
            "Rebound+1",
            "Flame Barrier+1",
            "Seek+1",
            "Endless Agony+1",
            "WreathOfFlame+1",
            "Collect+1",
            "SashWhip+1",
            "Wraith Form v2+1",
            "Melter+1",
            "Berserk+1",
            "Pummel+1",
            "Burning Pact+1",
            "Riddle With Holes+1",
            "Metallicize+1",
            "Self Repair+1",
            "Pommel Strike+1",
            "Pain+1",
            "Rainbow+1",
            "InnerPeace+1",
            "Burst+1",
            "Acrobatics+1",
            "Adaptation+1",
            "Loop+1",
            "Blind+1",
            "Doppelganger+1",
            "Skewer+1",
            "Omniscience+1",
            "Envenom+1",
            "Chill+1",
            "Adrenaline+1",
            "Quick Slash+1",
            "Twin Strike+1",
            "BootSequence+1",
            "Parasite+1",
            "Bash+1",
            "RitualDagger+1",
            "Gash+1",
            "Wish+1",
            "Clothesline+1",
            "DeceiveReality+1",
            "MentalFortress+1",
            "Shockwave+1",
            "BecomeAlmighty+1",
            "Rampage+1",
            "Coolheaded+1",
            "Static Discharge+1",
            "Alpha+1",
            "Heatsinks+1",
            "Vault+1",
            "Bandage Up+1",
            "Scrawl+1",
            "Sever Soul+1",
            "Eruption+1",
            "Whirlwind+1",
            "Bite+1",
            "LessonLearned+1",
            "Secret Technique+1",
            "Calculated Gamble+1",
            "Tempest+1",
            "Combust+1",
            "Deep Breath+1",
            "Doubt+1",
            "Escape Plan+1",
            "CutThroughFate+1",
            "ReachHeaven+1",
            "Finisher+1",
            "Dark Embrace+1",
            "Die Die Die+1",
            "Well Laid Plans+1",
            "Ragnarok+1",
            "Buffer+1",
            "Electrodynamics+1",
            "FearNoEvil+1",
            "Seeing Red+1",
            "SandsOfTime+1",
            "Smite+1",
            "Violence+1",
            "Disarm+1",
            "Turbo+1",
            "Panache+1",
            "Undo+1",
            "Fiend Fire+1",
            "Terror+1",
            "Force Field+1",
            "Dazed+1",
            "Barricade+1",
            "Armaments+1",
            "Havoc+1",
            "Secret Weapon+1",
            "Apotheosis+1",
            "Sweeping Beam+1",
            "Feel No Pain+1",
            "FTL+1",
            "Rip and Tear+1",
            "Darkness+1",
            "Corruption+1",
            "Heel Hook+1",
            "Blasphemy+1",
            "Injury+1",
            "Double Energy+1",
            "Rage+1",
            "Headbutt+1",
            "Machine Learning+1",
            "Reinforced Body+1",
            "Limit Break+1",
            "Entrench+1",
            "Noxious Fumes+1",
            "Infinite Blades+1",
            "Phantasmal Killer+1",
            "WaveOfTheHand+1",
            "Malaise+1",
            "Conserve Battery+1",
            "Mayhem+1",
            "Reflex+1",
            "Study+1",
            "Expunger+1",
            "Sentinel+1",
            "Survivor+1",
            "Wild Strike+1",
            "HandOfGreed+1",
            "Meditate+1",
            "Eviscerate+1",
            "Flash of Steel+1",
            "Battle Trance+1",
            "Forethought+1",
            "Dualcast+1",
            "Auto Shields+1",
            "Perseverance+1",
            "Swivel+1",
            "Heavy Blade+1",
            "Slimed+1",
            "Clumsy+1",
            "Biased Cognition+1",
            "Searing Blow+1",
            "Searing Blow+2",
            "Searing Blow+3",
            "Searing Blow+4",
            "Searing Blow+5",
            "Searing Blow+6",
            "Searing Blow+7",
            "Searing Blow+8",
            "Searing Blow+9",
            "Searing Blow+10",
            "Searing Blow+11",
            "Searing Blow+12",
            "Searing Blow+13",
            "Searing Blow+14",
            "Searing Blow+15",
            "Devotion+1",
            "Reprogram+1",
            "Hologram+1",
            "Corpse Explosion+1",
            "Second Wind+1",
            "Enlightenment+1",
            "Purity+1",
            "Panacea+1",
            "Lockon+1",
            "Dash+1",
            "Worship+1",
            "Conclude+1",
            "ThroughViolence+1",
            "Transmutation+1",
            "Ghostly+1",
            "Backstab+1",
            "Chrysalis+1",
            "FollowUp+1",
            "Void+1",
            "Scrape+1",
            "Feed+1",
            "Vigilance+1",
            "Rupture+1",
            "Venomology+1",
            "Discovery+1",
            "Beam Cell+1",
            "Leap+1",
            "CurseOfTheBell+1",
            "Bouncing Flask+1",
            "PathToVictory+1",
            "Bludgeon+1",
            "Finesse+1",
            "Slice+1",
            "Recycle+1",
            "Backflip+1",
            "Outmaneuver+1",
            "Bloodletting+1",
            "Brilliance+1",
            "Magnetism+1",
            "Concentrate+1",
            "Skim+1",
            "White Noise+1",
            "Capacitor+1",
            "Cold Snap+1",
            "CrushJoints+1",
            "Master of Strategy+1",
            "Flechettes+1",
            "Tantrum+1",
            "Perfected Strike+1",
            "Thunder Strike+1",
            "Carnage+1",
            "Masterful Stab+1",
            "Nirvana+1",
            "Evaluate+1",
            "Prepared+1",
            "Good Instincts+1",
            "Dropkick+1",
            "Swift Strike+1",
            "Normality+1",
            "MasterReality+1",
            "Omega+1",
            "Hyperbeam+1",
            "Accuracy+1",
            "Sword Boomerang+1",
            "EmptyMind+1",
            "Pride+1",
            "Defragment+1",
            "Jack Of All Trades+1",
            "Demon Form+1",
            "Fire Breathing+1",
            "Ghostly Armor+1",
            "Weave+1",
            "Safety+1",
            "Metamorphosis+1",
            "Prostrate+1",
            "SignatureMove+1",
            "Uppercut+1",
            "PiercingWail+1",
            "Mind Blast+1",
            "Neutralize+1",
            "Multi-Cast+1",
            "Shame+1",
            "Doom and Gloom+1",
            "Evolve+1",
            "Double Tap+1",
            "Sucker Punch+1",
            "Burn+1",
            "ConjureBlade+1",
            "Judgement+1",
            "Footwork+1",
            "Steam+1",
            "Distraction+1",
            "Dodge and Roll+1",
            "Thinking Ahead+1",
            "EmptyFist+1",
            "All Out Attack+1",
            "Flying Knee+1",
            "Predator+1",
            "Pray+1",
            "Madness+1",
            "Strike",
            "Strike+1",
            "Defend",
            "Defend+1"
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
            cardCount * MAX_AMOUNT_PER_CARD_TYPE + relicCount,
            cardCount * MAX_AMOUNT_PER_CARD_TYPE + relicCount + encountersCount,
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

        List<String> cardIds = masterDeck.stream()
            .map(c -> {
                if (c.upgraded) {
                    return c.cardID + "+1";
                }
                return c.cardID;
            })
            .map(ModelUtils::generalizeStrikeDefend)
            .collect(Collectors.toList());
        List<String> relicIds = masterRelics.stream()
            .map(r -> r.relicId)
            .collect(Collectors.toList());

        for (String cID : cardIds) {
            int index = cardEncoding.get(cID);
            for (int offset = 0; offset < MAX_AMOUNT_PER_CARD_TYPE; offset++) {
                if (outputVector[index + offset] == 0) {
                    outputVector[index + offset] = 1;
                    break;
                }
            }
        }
        for (String rID : relicIds) {
            outputVector[cardCount * MAX_AMOUNT_PER_CARD_TYPE + relicEncoding.get(rID)] += 1;
        }
        outputVector[cardCount * MAX_AMOUNT_PER_CARD_TYPE + relicCount + encounterEncoding.get(encounter)] += 1;

        int remainingOffset = cardCount * MAX_AMOUNT_PER_CARD_TYPE + relicCount + encountersCount;
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
            cardCount * MAX_AMOUNT_PER_CARD_TYPE + relicCount,
            cardCount * MAX_AMOUNT_PER_CARD_TYPE + relicCount + encountersCount,
            0.0f
        );
        int index = cardCount * MAX_AMOUNT_PER_CARD_TYPE + relicCount + encounterEncoding.get(encounter);
        vecCopy[index] += 1;
        return vecCopy;
    }
}
