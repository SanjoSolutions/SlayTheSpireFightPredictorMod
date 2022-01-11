package FightPredictor.patches.com.megacrit.cardcrawl.neow.NeowEvent;

import FightPredictor.FightPredictor;
import FightPredictor.CardEvaluationData;
import FightPredictor.util.BaseGameConstants;
import FightPredictor.util.StatEvaluation;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.neow.NeowEvent;
import com.megacrit.cardcrawl.neow.NeowReward;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class NeowEventPatches {
    private static int STARTING_ACT = 3;
    private static int ENDING_ACT = 3;
    private static Set<String> enemies = BaseGameConstants.bossIDs.get(3);

    @SpirePatch(clz = NeowEvent.class, method = "blessing")
    public static class BlessingPatch {
        @SpirePostfixPatch
        public static void blessing(NeowEvent __instance) {
            try {
                Field rewardsField = __instance.getClass().getDeclaredField("rewards");
                rewardsField.setAccessible(true);
                ArrayList<NeowReward> rewards = (ArrayList<NeowReward>)rewardsField.get(__instance);
                for (int index = 0; index < rewards.size(); index++) {
                    NeowReward reward = rewards.get(index);
                    boolean receivesCurse = reward.drawback == NeowReward.NeowRewardDrawback.CURSE;
                    Float evaluation = null;
                    if (reward.type == NeowReward.NeowRewardType.RANDOM_COLORLESS_2) {
                        evaluation = evaluateAddingARareColorlessCard(receivesCurse);
                    } else if (reward.type == NeowReward.NeowRewardType.THREE_CARDS) {
                        evaluation = evaluateAddingACard();
                    } else if (reward.type == NeowReward.NeowRewardType.ONE_RANDOM_RARE_CARD) {
                        evaluation = evaluateAddingARandomRareCard();
                    } else if (reward.type == NeowReward.NeowRewardType.REMOVE_CARD) {
                        evaluation = evaluateRemovingACard();
                    } else if (reward.type == NeowReward.NeowRewardType.UPGRADE_CARD) {
                        evaluation = evaluateUpgradingACard();
                    } else if (reward.type == NeowReward.NeowRewardType.RANDOM_COLORLESS) {
                        evaluation = evaluateAddingAColorlessCard();
                    } else if (reward.type == NeowReward.NeowRewardType.TRANSFORM_CARD) {
                        evaluation = evaluateTransformingACard();
                    } else if (reward.type == NeowReward.NeowRewardType.THREE_SMALL_POTIONS) {

                    } else if (reward.type == NeowReward.NeowRewardType.RANDOM_COMMON_RELIC) {
                        evaluation = evaluateAddingACommonRelic();
                    } else if (reward.type == NeowReward.NeowRewardType.TEN_PERCENT_HP_BONUS) {

                    } else if (reward.type == NeowReward.NeowRewardType.HUNDRED_GOLD) {

                    } else if (reward.type == NeowReward.NeowRewardType.THREE_ENEMY_KILL) {

                    } else if (reward.type == NeowReward.NeowRewardType.REMOVE_TWO) {
                        evaluation = evaluateRemovingTwoCards();
                    } else if (reward.type == NeowReward.NeowRewardType.TRANSFORM_TWO_CARDS) {
                        evaluation = evaluateTransformingTwoCards();
                    } else if (reward.type == NeowReward.NeowRewardType.ONE_RARE_RELIC) {
                        evaluation = evaluateAddingOneRareRelic(receivesCurse);
                    } else if (reward.type == NeowReward.NeowRewardType.THREE_RARE_CARDS) {
                        evaluation = evaluateAddingThreeRareCards(receivesCurse);
                    } else if (reward.type == NeowReward.NeowRewardType.TWO_FIFTY_GOLD) {
                        // TODO: Implement receivesCurse
                    } else if (reward.type == NeowReward.NeowRewardType.TWENTY_PERCENT_HP_BONUS) {

                    } else if (reward.type == NeowReward.NeowRewardType.BOSS_RELIC) {
                        evaluation = evaluateRemovingStartingRelicAndAddingARandomBossRelic();
                    }
                    if (evaluation != null) {
                        appendEvaluationToRoomEventOption(__instance, index, evaluation);
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException exception) {
                // Empty catch block
            }
        }

        private static void appendEvaluationToRoomEventOption(NeowEvent __instance, int index, float evaluation) {
            appendTextToRoomEventOption(
                __instance,
                index,
                " (" + String.format(Locale.ENGLISH, "%.2f", evaluation) + ")"
            );
        }

        private static void appendTextToRoomEventOption(NeowEvent __instance, int index, String text) {
            __instance.roomEventText.optionList.get(index).msg += text;
        }

        // TODO: Implement receivesCurse
        private static float evaluateAddingARareColorlessCard(boolean receivesCurse) {
            CardEvaluationData evaluations = CardEvaluationData.createByAdding(
                CardLibrary.getCardList(CardLibrary.LibraryType.COLORLESS).stream().filter(card -> card.rarity == AbstractCard.CardRarity.RARE).collect(Collectors.toList()),
                STARTING_ACT,
                ENDING_ACT
            );

            float evaluation = 0;
            for (StatEvaluation statEvaluation : evaluations.getEvals().values()) {
                evaluation += StatEvaluation.determineWeightedScoreForCurrentAct(statEvaluation);
            }
            evaluation /= evaluations.getEvals().size();

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(evaluations.getSkip());

            return skipEvaluation - evaluation;
        }

        private static float evaluateAddingACard() {
            CardEvaluationData evaluations = CardEvaluationData.createByAdding(
                CardLibrary.getCardList(convertCardColorToLibraryType(getColorForChosenClass())),
                STARTING_ACT,
                ENDING_ACT
            );

            float evaluation = 0;
            for (StatEvaluation statEvaluation : evaluations.getEvals().values()) {
                evaluation += StatEvaluation.determineWeightedScoreForCurrentAct(statEvaluation);
            }
            evaluation /= evaluations.getEvals().size();

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(evaluations.getSkip());

            return skipEvaluation - evaluation;
        }

        private static AbstractCard.CardColor getColorForChosenClass() {
            return getColorForClass(AbstractDungeon.player.chosenClass);
        }

        private static AbstractCard.CardColor getColorForClass(AbstractPlayer.PlayerClass playerClass) {
            switch (playerClass) {
                case IRONCLAD:
                    return AbstractCard.CardColor.RED;
                case THE_SILENT:
                    return AbstractCard.CardColor.GREEN;
                case DEFECT:
                    return AbstractCard.CardColor.BLUE;
                case WATCHER:
                    return AbstractCard.CardColor.PURPLE;
                default:
                    throw new RuntimeException("Unhandled playerClass");
            }
        }

        private static float evaluateAddingARandomRareCard() {
            CardEvaluationData evaluations = CardEvaluationData.createByAdding(
                CardLibrary.getAllCards().stream().filter(card -> card.rarity == AbstractCard.CardRarity.RARE).collect(Collectors.toList()),
                STARTING_ACT,
                ENDING_ACT
            );

            float evaluation = 0;
            for (StatEvaluation statEvaluation : evaluations.getEvals().values()) {
                evaluation += StatEvaluation.determineWeightedScoreForCurrentAct(statEvaluation);
            }
            evaluation /= evaluations.getEvals().size();

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(evaluations.getSkip());

            return skipEvaluation - evaluation;
        }

        private static float evaluateRemovingACard() {
            CardEvaluationData evaluations = CardEvaluationData.createByRemoving(
                AbstractDungeon.player.masterDeck.group,
                STARTING_ACT,
                ENDING_ACT
            );

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(evaluations.getSkip());

            return (float)evaluations.getEvals().values().stream().mapToDouble(statEvaluation -> skipEvaluation - StatEvaluation.determineWeightedScoreForCurrentAct(statEvaluation)).max().orElse(0);
        }

        private static float evaluateUpgradingACard() {
            CardEvaluationData evaluations = CardEvaluationData.createByUpgrading(
                AbstractDungeon.player.masterDeck.group,
                STARTING_ACT,
                ENDING_ACT
            );

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(evaluations.getSkip());

            return (float)evaluations.getEvals().values().stream().mapToDouble(statEvaluation -> skipEvaluation - StatEvaluation.determineWeightedScoreForCurrentAct(statEvaluation)).max().orElse(0);
        }

        private static float evaluateAddingAColorlessCard() {
            CardEvaluationData evaluations = CardEvaluationData.createByAdding(
                CardLibrary.getCardList(CardLibrary.LibraryType.COLORLESS),
                STARTING_ACT,
                ENDING_ACT
            );

            float evaluation = 0;
            for (StatEvaluation statEvaluation : evaluations.getEvals().values()) {
                evaluation += StatEvaluation.determineWeightedScoreForCurrentAct(statEvaluation);
            }
            evaluation /= evaluations.getEvals().size();

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(evaluations.getSkip());

            return skipEvaluation - evaluation;
        }

        private static float evaluateTransformingACard() {
            int startingAct = STARTING_ACT;
            int endingAct = ENDING_ACT;

            ArrayList<AbstractCard> transformCandidates = AbstractDungeon.player.masterDeck.group;
            float evaluation = 0;
            for (AbstractCard cardToTransform : transformCandidates) {
                List<AbstractCard> newDeck = new ArrayList<>(AbstractDungeon.player.masterDeck.group);
                newDeck.remove(cardToTransform);

                ArrayList<AbstractCard> transformedIntoCandidates = CardLibrary.getCardList(convertCardColorToLibraryType(cardToTransform.color));

                float evaluation2 = 0;
                for (AbstractCard transformedIntoCandidate : transformedIntoCandidates) {
                    List<AbstractCard> newDeck2 = new ArrayList<>(newDeck);
                    newDeck2.add(transformedIntoCandidate);

                    // Run stat evaluation
                    StatEvaluation statEvaluation = new StatEvaluation(
                        newDeck2,
                        AbstractDungeon.player.relics,
                        AbstractDungeon.player.maxHealth,
                        AbstractDungeon.player.currentHealth,
                        AbstractDungeon.ascensionLevel,
                        false,
                        enemies
                    );

                    evaluation2 += StatEvaluation.determineWeightedScore(statEvaluation, AbstractDungeon.actNum);
                }
                evaluation2 /= transformedIntoCandidates.size();

                evaluation += evaluation2;
            }
            evaluation /= transformCandidates.size();

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(new StatEvaluation(
                AbstractDungeon.player.masterDeck.group,
                AbstractDungeon.player.relics,
                AbstractDungeon.player.maxHealth,
                AbstractDungeon.player.currentHealth,
                AbstractDungeon.ascensionLevel,
                false,
                enemies
            ));

            return skipEvaluation - evaluation;
        }

        private static CardLibrary.LibraryType convertCardColorToLibraryType(AbstractCard.CardColor color) {
            switch (color) {
                case BLUE:
                    return CardLibrary.LibraryType.BLUE;
                case COLORLESS:
                    return CardLibrary.LibraryType.COLORLESS;
                case CURSE:
                    return CardLibrary.LibraryType.CURSE;
                case PURPLE:
                    return CardLibrary.LibraryType.PURPLE;
                case GREEN:
                    return CardLibrary.LibraryType.GREEN;
                case RED:
                    return CardLibrary.LibraryType.RED;
                default:
                    throw new RuntimeException("Unexpected value for color");
            }
        }

        private static float evaluateAddingACommonRelic() {
            CardEvaluationData evaluations = CardEvaluationData.createByAddingRelic(
                RelicLibrary.commonList,
                STARTING_ACT,
                ENDING_ACT
            );

            float evaluation = 0;
            for (StatEvaluation statEvaluation : evaluations.getEvals().values()) {
                evaluation += StatEvaluation.determineWeightedScoreForCurrentAct(statEvaluation);
            }
            evaluation /= evaluations.getEvals().size();

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(evaluations.getSkip());

            return skipEvaluation - evaluation;
        }

        private static float evaluateRemovingTwoCards() {
            int startingAct = STARTING_ACT;
            int endingAct = ENDING_ACT;

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(new StatEvaluation(
                AbstractDungeon.player.masterDeck.group,
                AbstractDungeon.player.relics,
                AbstractDungeon.player.maxHealth,
                AbstractDungeon.player.currentHealth,
                AbstractDungeon.ascensionLevel,
                false,
                enemies
            ));

            ArrayList<AbstractCard> transformCandidates = AbstractDungeon.player.masterDeck.group;
            float maxEvaluation = Float.NEGATIVE_INFINITY;
            for (int index1 = 0; index1 < AbstractDungeon.player.masterDeck.group.size(); index1++) {
                AbstractCard card1ToRemove = AbstractDungeon.player.masterDeck.group.get(index1);
                List<AbstractCard> newDeck = new ArrayList<>(AbstractDungeon.player.masterDeck.group);
                newDeck.remove(card1ToRemove);
                for (int index2 = index1 + 1; index2 < AbstractDungeon.player.masterDeck.group.size(); index2++) {
                    AbstractCard card2ToRemove = AbstractDungeon.player.masterDeck.group.get(index2);
                    List<AbstractCard> newDeck2 = new ArrayList<>(newDeck);
                    newDeck2.remove(card2ToRemove);

                    StatEvaluation statEvaluation = new StatEvaluation(
                        newDeck2,
                        AbstractDungeon.player.relics,
                        AbstractDungeon.player.maxHealth,
                        AbstractDungeon.player.currentHealth,
                        AbstractDungeon.ascensionLevel,
                        false,
                        enemies
                    );

                    float evaluation = skipEvaluation - StatEvaluation.determineWeightedScore(statEvaluation, AbstractDungeon.actNum);

                    maxEvaluation = Math.max(maxEvaluation, evaluation);
                }
            }

            return maxEvaluation;
        }

        private static float evaluateTransformingTwoCards() {
            int startingAct = STARTING_ACT;
            int endingAct = ENDING_ACT;

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(new StatEvaluation(
                AbstractDungeon.player.masterDeck.group,
                AbstractDungeon.player.relics,
                AbstractDungeon.player.maxHealth,
                AbstractDungeon.player.currentHealth,
                AbstractDungeon.ascensionLevel,
                false,
                enemies
            ));

            float maxEvaluation = Float.NEGATIVE_INFINITY;

            ArrayList<AbstractCard> transformCandidates = AbstractDungeon.player.masterDeck.group;

            for (int index1 = 0; index1 < transformCandidates.size(); index1++) {
                AbstractCard cardToTransform1 = transformCandidates.get(index1);
                List<AbstractCard> newDeck = new ArrayList<>(AbstractDungeon.player.masterDeck.group);
                newDeck.remove(cardToTransform1);

                ArrayList<AbstractCard> transformedIntoCandidates1 = CardLibrary.getCardList(convertCardColorToLibraryType(cardToTransform1.color));

                for (int index2 = index1 + 1; index2 < transformCandidates.size(); index2++) {
                    AbstractCard cardToTransform2 = transformCandidates.get(index2);
                    List<AbstractCard> newDeck2 = new ArrayList<>(newDeck);
                    newDeck2.remove(cardToTransform2);

                    ArrayList<AbstractCard> transformedIntoCandidates2 = CardLibrary.getCardList(convertCardColorToLibraryType(cardToTransform2.color));

                    float evaluation = 0;
                    int count = 0;

                    for (AbstractCard transformCandidate1 : transformedIntoCandidates1) {
                        for (AbstractCard transformCandidate2 : transformedIntoCandidates2) {
                            List<AbstractCard> newDeck3 = new ArrayList<>(newDeck2);
                            newDeck3.add(transformCandidate1);
                            newDeck3.add(transformCandidate2);

                            StatEvaluation statEvaluation = new StatEvaluation(
                                newDeck3,
                                AbstractDungeon.player.relics,
                                AbstractDungeon.player.maxHealth,
                                AbstractDungeon.player.currentHealth,
                                AbstractDungeon.ascensionLevel,
                                false,
                                enemies
                            );

                            float evaluation2 = skipEvaluation - StatEvaluation.determineWeightedScore(statEvaluation, AbstractDungeon.actNum);

                            evaluation += evaluation2;
                            count++;
                        }
                    }

                    maxEvaluation = Math.max(maxEvaluation, evaluation / count);
                }
            }

            return maxEvaluation;
        }

        // TODO: Implement receivesCurse
        private static float evaluateAddingOneRareRelic(boolean receivesCurse) {
            CardEvaluationData evaluations = CardEvaluationData.createByAddingRelic(
                RelicLibrary.rareList,
                STARTING_ACT,
                ENDING_ACT
            );

            float evaluation = 0;
            for (StatEvaluation statEvaluation : evaluations.getEvals().values()) {
                evaluation += StatEvaluation.determineWeightedScoreForCurrentAct(statEvaluation);
            }
            evaluation /= evaluations.getEvals().size();

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(evaluations.getSkip());

            return skipEvaluation - evaluation;
        }

        private static float evaluateAddingThreeRareCards(boolean receivesCurse) {
            int startingAct = STARTING_ACT;
            int endingAct = ENDING_ACT;

            List<AbstractCard> rareCards = CardLibrary.getAllCards().stream().filter(card -> card.rarity == AbstractCard.CardRarity.RARE).collect(Collectors.toList());

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(new StatEvaluation(
                AbstractDungeon.player.masterDeck.group,
                AbstractDungeon.player.relics,
                AbstractDungeon.player.maxHealth,
                AbstractDungeon.player.currentHealth,
                AbstractDungeon.ascensionLevel,
                false,
                enemies
            ));

            ArrayList<AbstractCard> transformCandidates = AbstractDungeon.player.masterDeck.group;

            float evaluation = 0;
            int count = 0;

            for (AbstractCard card1 : rareCards) {
                List<AbstractCard> newDeck = new ArrayList<>(AbstractDungeon.player.masterDeck.group);
                newDeck.add(card1);

                for (AbstractCard card2 : rareCards) {
                    List<AbstractCard> newDeck2 = new ArrayList<>(newDeck);
                    newDeck2.add(card2);

                    for (AbstractCard card3 : rareCards) {
                        List<AbstractCard> newDeck3 = new ArrayList<>(newDeck2);
                        newDeck3.add(card3);

                        if (receivesCurse) {
                            List<AbstractCard> curses = getCurses();
                            for (AbstractCard curse : curses) {
                                List<AbstractCard> newDeck4 = new ArrayList<>(newDeck3);
                                newDeck4.add(curse);

                                StatEvaluation statEvaluation = new StatEvaluation(
                                    newDeck4,
                                    AbstractDungeon.player.relics,
                                    AbstractDungeon.player.maxHealth,
                                    AbstractDungeon.player.currentHealth,
                                    AbstractDungeon.ascensionLevel,
                                    false,
                                    enemies
                                );

                                evaluation += StatEvaluation.determineWeightedScore(statEvaluation, AbstractDungeon.actNum);
                                count++;
                            }
                        } else {
                            StatEvaluation statEvaluation = new StatEvaluation(
                                newDeck3,
                                AbstractDungeon.player.relics,
                                AbstractDungeon.player.maxHealth,
                                AbstractDungeon.player.currentHealth,
                                AbstractDungeon.ascensionLevel,
                                false,
                                enemies
                            );

                            evaluation += StatEvaluation.determineWeightedScore(statEvaluation, AbstractDungeon.actNum);
                            count++;
                        }
                    }
                }
            }

            return evaluation / count;
        }

        private static float evaluateRemovingStartingRelicAndAddingARandomBossRelic() {
            CardEvaluationData evaluations = CardEvaluationData.createByRemovingStartingRelicAndAddingARandomBossRelic(
                STARTING_ACT,
                ENDING_ACT
            );

            float evaluation = 0;
            for (StatEvaluation statEvaluation : evaluations.getEvals().values()) {
                evaluation += StatEvaluation.determineWeightedScoreForCurrentAct(statEvaluation);
            }
            evaluation /= evaluations.getEvals().size();

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(evaluations.getSkip());

            return skipEvaluation - evaluation;
        }

        private static List<AbstractCard> getCurses() {
            return CardLibrary.getCardList(CardLibrary.LibraryType.CURSE)
                .stream()
                .filter(card -> !card.cardID.equals("AscendersBane") && !card.cardID.equals("Necronomicurse") && !card.cardID.equals("CurseOfTheBell") && !card.cardID.equals("Pride"))
                .collect(Collectors.toList());
        }
    }
}
