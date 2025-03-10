package FightPredictor.util;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class BaseGameConstants {

    public static final Map<Integer, Set<String>> weakIDs;
    public static final Map<Integer, Set<String>> strongIDs;
    public static final Map<Integer, Set<String>> eliteIDs;
    public static final Map<Integer, Set<String>> bossIDs;
    public static final Map<Integer, Set<String>> allFightsByAct;
    public static final Map<Integer, Set<String>> elitesAndBossesByAct;

    static {
        weakIDs = new HashMap<>();
        weakIDs.put(1, new HashSet<>(Arrays.asList(
            "Blue Slaver",
            "Cultist",
            "Jaw Worm",
            "2 Louse",
            "Small Slimes"
        )));
        weakIDs.put(2, new HashSet<>(Arrays.asList(
            "Chosen",
            "Shell Parasite",
            "Spheric Guardian",
            "3 Byrds",
            "2 Thieves"
        )));
        weakIDs.put(3, new HashSet<>(Arrays.asList(
            "Orb Walker",
            "3 Darklings",
            "3 Shapes"
        )));
        weakIDs.put(4, new HashSet<>(Arrays.asList()));
        strongIDs = new HashMap<>();
        strongIDs.put(1, new HashSet<>(Arrays.asList(
            "Gremlin Gang",
            "Large Slime",
            "Looter",
            "Lots of Slimes",
            "Exordium Thugs",
            "Exordium Wildlife",
            "Red Slaver",
            "3 Louse",
            "2 Fungi Beasts"
        )));
        strongIDs.put(2, new HashSet<>(Arrays.asList(
            "Chosen and Byrds",
            "Sentry and Sphere",
            "Snake Plant",
            "Snecko",
            "Centurion and Healer",
            "Cultist and Chosen",
            "3 Cultists",
            "Shelled Parasite and Fungi"
        )));
        strongIDs.put(3, new HashSet<>(Arrays.asList(
            "Transient",
            "4 Shapes",
            "Maw",
            "Jaw Worm Horde",
            "Sphere and 2 Shapes",
            "Spire Growth",
            "Writhing Mass"
        )));
        strongIDs.put(4, new HashSet<>(Arrays.asList()));

        eliteIDs = new HashMap<>();
        eliteIDs.put(1, new HashSet<>(Arrays.asList(
            "Gremlin Nob",
            "Lagavulin",
            "3 Sentries"
        )));
        eliteIDs.put(2, new HashSet<>(Arrays.asList(
            "Gremlin Leader",
            "Slavers",
            "Book of Stabbing"
        )));
        eliteIDs.put(3, new HashSet<>(Arrays.asList(
            "Giant Head",
            "Nemesis",
            "Reptomancer"
        )));
        eliteIDs.put(4, new HashSet<>(Arrays.asList(
            "Shield and Spear"
        )));

        bossIDs = new HashMap<>();
        bossIDs.put(1, new HashSet<>(Arrays.asList(
            "The Guardian",
            "Hexaghost",
            "Slime Boss"
        )));
        bossIDs.put(2, new HashSet<>(Arrays.asList(
            "Automaton",
            "Champ",
            "Collector"
        )));
        bossIDs.put(3, new HashSet<>(Arrays.asList(
            "Awakened One",
            "Donu and Deca",
            "Time Eater"
        )));
        bossIDs.put(4, new HashSet<>(Arrays.asList(
            "The Heart"
        )));

        allFightsByAct = new HashMap<>();
        for (int i = 1; i <= 3; i++) {
            Set<String> enemies = Stream.of(weakIDs.get(i), strongIDs.get(i), eliteIDs.get(i), bossIDs.get(i))
                    .flatMap(Set::stream)
                    .collect(toSet());
            allFightsByAct.put(i, enemies);
        }
        Set<String> enemies = Stream.of(eliteIDs.get(4), bossIDs.get(4))
                .flatMap(Set::stream)
                .collect(toSet());
        allFightsByAct.put(4, enemies);

        elitesAndBossesByAct = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            Set<String> bigEnemies = Stream.of(eliteIDs.get(i), bossIDs.get(i))
                    .flatMap(Set::stream)
                    .collect(toSet());
            elitesAndBossesByAct.put(i, bigEnemies);
        }
    }
}
