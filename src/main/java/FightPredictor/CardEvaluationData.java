package FightPredictor;

import FightPredictor.util.BaseGameConstants;
import FightPredictor.util.StatEvaluation;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.*;
import java.util.stream.Collectors;

public class CardEvaluationData {

    // Evaluation for skip
    private final StatEvaluation skip;

    // Evaluations for each card
    private final Map<Object, StatEvaluation> evals;

    // Maps a card to its score by act. diffs.get(card).get(actNum) => score.
    private final Map<Object, Map<Integer, Float>> diffs;

    // Acts that are supported. Add more acts as needed
    private final Set<Integer> supportedActs;

    private CardEvaluationData(int startingAct, int endingAct) {
        this.supportedActs = new HashSet<>();
        for (int act = startingAct; act <= endingAct; act++) {
            this.supportedActs.add(act);
        }

        Set<String> enemies = getAllEnemies(startingAct, endingAct);
        this.skip = new StatEvaluation(
            AbstractDungeon.player.masterDeck.group,
            AbstractDungeon.player.relics,
            AbstractDungeon.player.maxHealth,
            AbstractDungeon.player.currentHealth,
            AbstractDungeon.ascensionLevel,
            false,
            enemies
        );

        this.evals = new HashMap<>();
        this.diffs = new HashMap<>();

    }

    /**
     * Create a full evaluation of the given cards by evaluating a deck with each card added compared to skip, against enemies in
     * startingAct to endingAct inclusive. Additional acts can be added later if needed.
     * @param cardsToAdd Cards to evaluate adding to the deck
     * @param startingAct Act to start evaluating enemies. Inclusive. Must be 1-4 inclusive
     * @param endingAct Act to end evaluating enmies. Inclusive. Must be 1-4 inclusive and greater than or equal to startingAct
     * @return CardEvaluationData with predictions and scores completed evaluation
     */
    public static CardEvaluationData createByAdding(List<AbstractCard> cardsToAdd, int startingAct, int endingAct) {
        DeckManipulation dm = (deck, card) -> {
            deck.add(card);
        };
        return createByFunction(cardsToAdd, dm, startingAct, endingAct);
    }

    public static CardEvaluationData createByRemoving(List<AbstractCard> cardsToRemove, int startingAct, int endingAct) {
        DeckManipulation dm = (deck, card) -> {
            deck.remove(card);
        };
        return createByFunction(cardsToRemove, dm, startingAct, endingAct);
    }

    public static CardEvaluationData createByUpgrading(List<AbstractCard> cardsToUpgrade, int startingAct, int endingAct) {
        DeckManipulation dm = (deck, card) -> {
            deck.remove(card);
            AbstractCard upgradedCard = card.makeCopy();
            upgradedCard.upgrade();
            deck.add(upgradedCard);
        };
        return createByFunction(cardsToUpgrade, dm, startingAct, endingAct);
    }

    private interface DeckManipulation {
        void modifyDeck(List<AbstractCard> originalDeck, AbstractCard c);
    }

    private static CardEvaluationData createByFunction(List<AbstractCard> cardsToChange, DeckManipulation dm, int startingAct, int endingAct) {
        CardEvaluationData ced = new CardEvaluationData(startingAct, endingAct);
        Set<String> enemies = getAllEnemies(startingAct, endingAct);

        for (AbstractCard card : cardsToChange) {
            // Modify the deck
            List<AbstractCard> newDeck = new ArrayList<>(AbstractDungeon.player.masterDeck.group);
            dm.modifyDeck(newDeck, card);

            // Run stat evaluation
            StatEvaluation se = new StatEvaluation(
                    newDeck,
                    AbstractDungeon.player.relics,
                    AbstractDungeon.player.maxHealth,
                    AbstractDungeon.player.currentHealth,
                    AbstractDungeon.ascensionLevel,
                    false,
                    enemies
            );
            ced.evals.put(card, se);

            // Calculate score for each act by comparing to skip
            Map<Integer, Float> diffsByAct = new HashMap<>();
            for (int act = startingAct; act <= endingAct; act++) {
                float diff = StatEvaluation.determineWeightedScoreDifference(se, ced.skip, act);
                diffsByAct.put(act, diff);
            }
            ced.diffs.put(card, diffsByAct);
        }
        return ced;
    }

    public static CardEvaluationData createByAddingRelic(
            List<AbstractRelic> relicsToEvaluate,
            int startingAct,
            int endingAct
    ) {
        CardEvaluationData ced = new CardEvaluationData(startingAct, endingAct);
        Set<String> enemies = getAllEnemies(startingAct, endingAct);

        List<AbstractCard> newDeck = new ArrayList<>(AbstractDungeon.player.masterDeck.group);

        for (AbstractRelic relic : relicsToEvaluate) {
            ArrayList<AbstractRelic> relics = new ArrayList<AbstractRelic>(AbstractDungeon.player.relics);
            relics.add(relic);
            StatEvaluation se = new StatEvaluation(
                    newDeck,
                    relics,
                    AbstractDungeon.player.maxHealth,
                    AbstractDungeon.player.currentHealth,
                    AbstractDungeon.ascensionLevel,
                    false,
                    enemies
            );
            ced.evals.put(relic, se);

            // Calculate score for each act by comparing to skip
            Map<Integer, Float> diffsByAct = new HashMap<>();
            for (int act = startingAct; act <= endingAct; act++) {
                float diff = StatEvaluation.determineWeightedScoreDifference(se, ced.skip, act);
                diffsByAct.put(act, diff);
            }
            ced.diffs.put(relic, diffsByAct);
        }

        return ced;
    }

    public static CardEvaluationData createByRemovingStartingRelicAndAddingARandomBossRelic(
        int startingAct,
        int endingAct
    ) {
        CardEvaluationData ced = new CardEvaluationData(startingAct, endingAct);
        Set<String> enemies = getAllEnemies(startingAct, endingAct);

        List<AbstractCard> newDeck = new ArrayList<>(AbstractDungeon.player.masterDeck.group);

        List<AbstractRelic> relics2 = AbstractDungeon.player.relics.stream()
            .filter(relic -> RelicLibrary.starterList.contains(relic))
            .collect(Collectors.toList());

        for (AbstractRelic relic : RelicLibrary.bossList) {
            ArrayList<AbstractRelic> relics = new ArrayList<AbstractRelic>(relics2);
            relics.add(relic);
            StatEvaluation se = new StatEvaluation(
                newDeck,
                relics,
                AbstractDungeon.player.maxHealth,
                AbstractDungeon.player.currentHealth,
                AbstractDungeon.ascensionLevel,
                false,
                enemies
            );
            ced.evals.put(relic, se);

            // Calculate score for each act by comparing to skip
            Map<Integer, Float> diffsByAct = new HashMap<>();
            for (int act = startingAct; act <= endingAct; act++) {
                float diff = StatEvaluation.determineWeightedScoreDifference(se, ced.skip, act);
                diffsByAct.put(act, diff);
            }
            ced.diffs.put(relic, diffsByAct);
        }

        return ced;
    }

    public void addAdditionalActs(Set<Integer> actsToAdd) {

    }

    public static Set<String> getAllEnemies(int startingAct, int endingAct) {
        Set<String> enemies = new HashSet<>();
        for (int act = startingAct; act <= endingAct; act++) {
            enemies.addAll(BaseGameConstants.allFightsByAct.get(act));
        }
        return enemies;
    }

    public StatEvaluation getSkip() {
        return skip;
    }

    public Map<Object, StatEvaluation> getEvals() {
        return evals;
    }

    public Map<Object, Map<Integer, Float>> getDiffs() {
        return diffs;
    }

    public Set<Integer> getSupportedActs() {
        return supportedActs;
    }
}
