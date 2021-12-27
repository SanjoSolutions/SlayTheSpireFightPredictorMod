package FightPredictor.util;

import FightPredictor.FightPredictor;
import FightPredictor.ml.ModelUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;

import java.util.*;
import java.util.stream.Collectors;

public class StatEvaluation {

    // Maps enemies to damage prediction
    private final Map<String, Float> predictions;

    private final List<AbstractCard> cards;
    private final List<AbstractRelic> relics;
    private final int maxHP;
    private final int enteringHP;
    private final int ascension;
    private final boolean potionUsed;

    /**
     * Create a StatEvaluation. Runs a prediction on every fight supplied in enemiesToPredictWith.
     *
     * @param cards in deck
     * @param relics obtained
     * @param maxHP of player
     * @param enteringHP of player
     * @param ascension of player
     * @param potionUsed in fight
     * @param enemiesToPredictWith Runs 1 prediction for every enemy group supplied.
     */
    public StatEvaluation(List<AbstractCard> cards, List<AbstractRelic> relics, int maxHP, int enteringHP, int ascension, boolean potionUsed, Set<String> enemiesToPredictWith) {
        this.predictions = new HashMap<>();

        this.cards = new ArrayList<>(cards);
        this.relics = new ArrayList<>(relics);
        this.maxHP = maxHP;
        this.enteringHP = enteringHP;
        this.ascension = ascension;
        this.potionUsed = potionUsed;

        addPredictions(enemiesToPredictWith);
    }

    /**
     * Additional enemies to run predictions with
     * @param enemiesToAdd enemies to run predictions with
     */
    public void addPredictions(Set<String> enemiesToAdd) {
        float[] vector = ModelUtils.getInputVectorNoEncounter(cards, relics, maxHP, enteringHP, ascension, potionUsed);
        for (String enemy : enemiesToAdd) {
            vector = ModelUtils.changeEncounter(vector, enemy);
            float prediction = FightPredictor.model.predict(vector) * 100f;
            predictions.put(enemy, prediction);
        }
    }

    /**
     * Average damage difference taken between two game loadouts, weighted towards the group of fights most likely to kill the player.
     * For comparable results, both StatEvaluations should have used the same max, entering hp
     * Throws an exception if either StatEvaluation does not have the needed fight data evaluated prior to calling.
     *
     * @param o1 Positive values indicate that this load out is predicted to be better
     * @param o2 Negative values indicate that this load out is predicted to be better
     * @param actNumber Act to get average for
     * @return Weighted average of average damage taken per fight
     */
    public static float determineWeightedScoreDifference(StatEvaluation o1, StatEvaluation o2, int actNumber) {
        float score1 = determineWeightedScore(o1, actNumber);
        float score2 = determineWeightedScore(o2, actNumber);
        return score2 - score1;
    }

    private static float determineWeightedScore(StatEvaluation statEvaluation, int actNumber) {
        float score;

        if (actNumber == AbstractDungeon.actNum) {
            score = determineWeightedScoreForCurrentAct(statEvaluation);
        } else {
            score = determineWeightedScoreForAFollowingAct(statEvaluation, actNumber);
        }

        return score;
    }

    private static float determineWeightedScoreForCurrentAct(StatEvaluation statEvaluation) {
        float score = 0;

        List<MapRoomNode> nodes = new ArrayList<MapRoomNode>();
        nodes.add(AbstractDungeon.getCurrMapNode());
        nodes = getChildren(nodes);
        int distanceFromCurrentMapNode = 0;

        while (nodes.size() >= 1) {
            float weight = 1f / (float) nodes.size();

            for (MapRoomNode node : nodes) {
                if (node.room instanceof MonsterRoom) {
                    Map<Integer, Set<String>> ids;
                    if (node.room instanceof MonsterRoomElite) {
                        ids = BaseGameConstants.eliteIDs;
                    } else if (!(node.room instanceof MonsterRoomBoss)) {
                        ids = BaseGameConstants.hallwayIDs;
                    } else {
                        ids = null;
                    }
                    if (ids != null) {
                        float nodeScore = enemiesToAverage(ids, AbstractDungeon.actNum, statEvaluation.predictions);
                        score += weight * nodeScore;
                    }
                }
            }

            nodes = getChildren(nodes);
            distanceFromCurrentMapNode++;
        }

        float nodeScore = enemiesToAverage(BaseGameConstants.bossIDs, AbstractDungeon.actNum, statEvaluation.predictions);
        score += nodeScore;

        return score;
    }

    private static float determineWeightedScoreForAFollowingAct(StatEvaluation statEvaluation, int actNumber) {
        // weighting based on approximated ratio between normal, elite and boss map nodes
        float normalEnemyWeight;
        float eliteEnemyWeight;
        float bossEnemyWeight;

        if (actNumber == 4) {
            normalEnemyWeight = 0;
            eliteEnemyWeight = 0.5f;
            bossEnemyWeight = 0.5f;
        } else {
            int mapHeight = 15;
            int mapWidth = 7;
            int availableRoomCount = mapHeight * mapWidth + 1;

            // From dungeon classes
            float shopRoomChance = 0.05F;
            float restRoomChance = 0.12F;
            float treasureRoomChance = 0.0F;
            float eventRoomChance = 0.22F;
            float eliteRoomChance = 0.08F;
            int eliteCount;
            if (actNumber == 4) {
                eliteCount = 1;
            } else {
                if (ModHelper.isModEnabled("Elite Swarm")) {
                    eliteRoomChance *= 2.5F;
                } else if (AbstractDungeon.ascensionLevel >= 1) {
                    eliteRoomChance *= 1.6F;
                }
                eliteCount = Math.round(availableRoomCount * eliteRoomChance);
            }
            float normalEnemyProportion = 1.0f - shopRoomChance - restRoomChance - treasureRoomChance - eventRoomChance - eliteRoomChance;
            float bossRoomProportion = 1f / availableRoomCount;


            float sum = normalEnemyProportion + eliteRoomChance + bossRoomProportion;
            normalEnemyWeight = normalEnemyProportion / sum;
            eliteEnemyWeight = eliteRoomChance / sum;
            bossEnemyWeight = 1f / sum;
        }

        float score = (
            normalEnemyWeight * enemiesToAverage(BaseGameConstants.hallwayIDs, actNumber, statEvaluation.predictions) +
            eliteEnemyWeight * enemiesToAverage(BaseGameConstants.eliteIDs, actNumber, statEvaluation.predictions) +
            bossEnemyWeight * enemiesToAverage(BaseGameConstants.bossIDs, actNumber, statEvaluation.predictions)
        );

        return score;
    }

    public static float determineScoreForNode(MapRoomNode node, int actNumber) {
        float score;
        if (node.room instanceof MonsterRoom) {
            if (node.room instanceof MonsterRoomElite) {
                score = determineScoreForEliteNode(actNumber);
            } else if (node.room instanceof MonsterRoomBoss) {
                score = determineScoreForBossNode(actNumber);
            } else {
                score = determineScoreForEnemyNode(actNumber);
            }
        } else {
            score = 0f;
        }
        return score;
    }

    public static float determineScoreForEnemyNode(int actNumber) {
        return determineScoreForEnemyIds(actNumber, BaseGameConstants.hallwayIDs);
    }

    public static float determineScoreForEliteNode(int actNumber) {
        return determineScoreForEnemyIds(actNumber, BaseGameConstants.eliteIDs);
    }

    public static float determineScoreForBossNode(int actNumber) {
        return determineScoreForEnemyIds(actNumber, BaseGameConstants.bossIDs);
    }

    private static float determineScoreForEnemyIds(int actNumber, Map<Integer, Set<String>> enemyIds) {
        List<AbstractCard> deck = new ArrayList<>(AbstractDungeon.player.masterDeck.group);
        StatEvaluation statEvaluation = new StatEvaluation(
                deck,
                AbstractDungeon.player.relics,
                AbstractDungeon.player.maxHealth,
                AbstractDungeon.player.currentHealth,
                AbstractDungeon.ascensionLevel,
                false,
                enemyIds.get(AbstractDungeon.actNum)
        );
        float nodeScore = enemiesToAverage(enemyIds, AbstractDungeon.actNum, statEvaluation.predictions);
        return nodeScore;
    }

    private static ArrayList<MapRoomNode> getChildren(List<MapRoomNode> nodes) {
        ArrayList<MapRoomNode> children = new ArrayList<>();

        if (nodes.size() >= 1) {
            int potentialChildrenLevel = nodes.get(0).y + 1;
            ArrayList<ArrayList<MapRoomNode>> map = AbstractDungeon.map;
            if (potentialChildrenLevel < map.size()) {
                ArrayList<MapRoomNode> potentialChildren = map.get(potentialChildrenLevel);

                for (MapRoomNode potentialChild : potentialChildren) {
                    if (potentialChild.getParents().stream().anyMatch(parent -> nodes.contains(parent))) {
                        children.add(potentialChild);
                    }
                }
            }
        }

        return children;
    }

    public static float getWeightedAvgEliteAndBoss(StatEvaluation o1, StatEvaluation o2, int actNum) {
        float o1EliteExpectedDmg = enemiesToAverage(BaseGameConstants.eliteIDs, actNum, o1.predictions);
        float o2EliteExpectedDmg = enemiesToAverage(BaseGameConstants.eliteIDs, actNum, o2.predictions);

        float o1BossExpectedDmg;
        float o2BossExpectedDmg;

        if (actNum == AbstractDungeon.actNum) {
            o1BossExpectedDmg = o1.predictions.get(AbstractDungeon.bossKey);
            o2BossExpectedDmg = o2.predictions.get(AbstractDungeon.bossKey);
        } else {
            o1BossExpectedDmg = enemiesToAverage(BaseGameConstants.bossIDs, actNum, o1.predictions);
            o2BossExpectedDmg = enemiesToAverage(BaseGameConstants.bossIDs, actNum, o2.predictions);
        }

        float eliteDiff = o2EliteExpectedDmg - o1EliteExpectedDmg;
        float bossDiff = o2BossExpectedDmg - o1BossExpectedDmg;

        float numerator = (bossDiff * o2BossExpectedDmg) + (eliteDiff * o2EliteExpectedDmg);
        float denominator = o2BossExpectedDmg + o2EliteExpectedDmg;

        return numerator / denominator;
    }

    private static float enemiesToAverage(Map<Integer, Set<String>> enemiesByAct, int act, Map<String, Float> predictions) {
        double total = enemiesByAct.get(act).stream()
                .mapToDouble(predictions::get)
                .average()
                .orElse(0);
        return (float) total;
    }

    public Map<String, Float> getPredictions() {
        return predictions;
    }

    public Set<String> getEnemies() {
        return predictions.keySet();
    }
}
