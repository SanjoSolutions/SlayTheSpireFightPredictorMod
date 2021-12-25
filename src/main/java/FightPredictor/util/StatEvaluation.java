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
        return score1 - score2;
    }

    private static float determineWeightedScore(StatEvaluation statEvaluation, int actNumber) {
        float score;

        if (actNumber == AbstractDungeon.actNum) {
            score = determineWeightedScoreForCurrentAct(statEvaluation);
        } else {
            score = 0f;
        }

        return score;
    }

    private static float determineWeightedScoreForCurrentAct(StatEvaluation statEvaluation) {
        float score = 0;

        List<MapRoomNode> nodes = new ArrayList<MapRoomNode>();
        nodes.add(AbstractDungeon.getCurrMapNode());
        int levelIndex = 0;

        while (nodes.size() >= 1) {
            float weight = 1f / (float) nodes.size();

            for (MapRoomNode node : nodes) {
                if (node.room instanceof MonsterRoom) {
                    Map<Integer, Set<String>> ids;
                    if (node.room instanceof MonsterRoomElite) {
                        ids = BaseGameConstants.eliteIDs;
                    } else if (node.room instanceof MonsterRoomBoss) {
                        ids = BaseGameConstants.bossIDs;
                    } else {
                        ids = BaseGameConstants.hallwayIDs;
                    }
                    float nodeScore = enemiesToAverage(ids, AbstractDungeon.actNum, statEvaluation.predictions);
                    score += weight * nodeScore;
                }
            }

            int potentialChildrenLevelIndex = levelIndex + 1;

            if (potentialChildrenLevelIndex >= AbstractDungeon.map.size()) {
                break;
            }

            nodes = nodes.stream()
                    .flatMap(node -> getChildren(node, potentialChildrenLevelIndex).stream())
                    .collect(Collectors.toList());
            levelIndex++;
        }

        return score;
    }

    private static ArrayList<MapRoomNode> getChildren(MapRoomNode node, int potentialChildrenLevelIndex) {
        ArrayList<MapRoomNode> potentialChildrenLevel = AbstractDungeon.map.get(potentialChildrenLevelIndex);
        ArrayList<MapRoomNode> children = new ArrayList<>();
        for (MapRoomNode potentialChild : potentialChildrenLevel) {
            if (potentialChild.getParents().stream().anyMatch(parent -> parent == node)) {
                children.add(potentialChild);
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
                .getAsDouble();
        return (float) total;
    }

    public Map<String, Float> getPredictions() {
        return predictions;
    }

    public Set<String> getEnemies() {
        return predictions.keySet();
    }
}
