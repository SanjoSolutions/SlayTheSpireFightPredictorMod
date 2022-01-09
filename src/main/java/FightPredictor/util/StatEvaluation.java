package FightPredictor.util;

import FightPredictor.FightPredictor;
import FightPredictor.ml.ModelUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;

import java.util.*;

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
        return determineWeightedScoreForAFollowingAct(statEvaluation, 3);
    }

    private static float determineWeightedScoreForCurrentAct(StatEvaluation statEvaluation) {
        float score = 0;

        float nodeScore = enemiesToAverage(
            BaseGameConstants.bossIDs,
            FightPredictor.determineEvaluationStartingAct(),
            statEvaluation.predictions
        );
        score += nodeScore;

        return score;
    }

    private static float determineWeightedScoreForAFollowingAct(StatEvaluation statEvaluation, int actNumber) {
        float score = enemiesToAverage(BaseGameConstants.bossIDs, actNumber, statEvaluation.predictions);

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
