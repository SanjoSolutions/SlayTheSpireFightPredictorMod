package FightPredictor.patches.com.megacrit.cardcrawl.neow.NeowEvent;

import FightPredictor.FightPredictor;
import FightPredictor.CardEvaluationData;
import FightPredictor.util.StatEvaluation;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.neow.NeowEvent;
import com.megacrit.cardcrawl.neow.NeowReward;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class NeowEventPatches {
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
                    FightPredictor.logger.info("reward " + index + ": " + reward.type);
                    if (reward.type == NeowReward.NeowRewardType.RANDOM_COLORLESS_2) {
                        __instance.roomEventText.optionList.get(index).msg += " (" + String.format(Locale.ENGLISH, "%.2f", evaluateAddingARareColorlessCard()) + ")";
                    } else if (reward.type == NeowReward.NeowRewardType.THREE_CARDS) {

                    } else if (reward.type == NeowReward.NeowRewardType.ONE_RANDOM_RARE_CARD) {
                        __instance.roomEventText.optionList.get(index).msg += " (" + String.format(Locale.ENGLISH, "%.2f", evaluateAddingARandomRareCard()) + ")";
                    } else if (reward.type == NeowReward.NeowRewardType.REMOVE_CARD) {
                        __instance.roomEventText.optionList.get(index).msg += " (" + String.format(Locale.ENGLISH, "%.2f", evaluateRemovingACard()) + ")";
                    } else if (reward.type == NeowReward.NeowRewardType.UPGRADE_CARD) {
                        __instance.roomEventText.optionList.get(index).msg += " (" + String.format(Locale.ENGLISH, "%.2f", evaluateUpgradingACard()) + ")";
                    } else if (reward.type == NeowReward.NeowRewardType.RANDOM_COLORLESS) {
                        __instance.roomEventText.optionList.get(index).msg += " (" + String.format(Locale.ENGLISH, "%.2f", evaluateAddingAColorlessCard()) + ")";
                    } else if (reward.type == NeowReward.NeowRewardType.TRANSFORM_CARD) {
                        __instance.roomEventText.optionList.get(index).msg += " (" + String.format(Locale.ENGLISH, "%.2f", evaluateTransformingACard()) + ")";
                    } else if (reward.type == NeowReward.NeowRewardType.THREE_SMALL_POTIONS) {

                    } else if (reward.type == NeowReward.NeowRewardType.RANDOM_COMMON_RELIC) {
                        __instance.roomEventText.optionList.get(index).msg += " (" + String.format(Locale.ENGLISH, "%.2f", evaluateAddingACommonRelic()) + ")";
                    } else if (reward.type == NeowReward.NeowRewardType.TEN_PERCENT_HP_BONUS) {

                    } else if (reward.type == NeowReward.NeowRewardType.HUNDRED_GOLD) {

                    } else if (reward.type == NeowReward.NeowRewardType.THREE_ENEMY_KILL) {

                    } else if (reward.type == NeowReward.NeowRewardType.REMOVE_TWO) {
                        __instance.roomEventText.optionList.get(index).msg += " (" + String.format(Locale.ENGLISH, "%.2f", evaluateRemovingTwoCards()) + ")";
                    } else if (reward.type == NeowReward.NeowRewardType.TRANSFORM_TWO_CARDS) {
                        __instance.roomEventText.optionList.get(index).msg += " (" + String.format(Locale.ENGLISH, "%.2f", evaluateTransformingTwoCards()) + ")";
                    } else if (reward.type == NeowReward.NeowRewardType.ONE_RARE_RELIC) {
                        __instance.roomEventText.optionList.get(index).msg += " (" + String.format(Locale.ENGLISH, "%.2f", evaluateAddingOneRareRelic()) + ")";
                    } else if (reward.type == NeowReward.NeowRewardType.THREE_RARE_CARDS) {
                        __instance.roomEventText.optionList.get(index).msg += " (" + String.format(Locale.ENGLISH, "%.2f", evaluateAddingThreeRareCards()) + ")";
                    } else if (reward.type == NeowReward.NeowRewardType.TWO_FIFTY_GOLD) {

                    } else if (reward.type == NeowReward.NeowRewardType.TWENTY_PERCENT_HP_BONUS) {

                    } else if (reward.type == NeowReward.NeowRewardType.BOSS_RELIC) {
                        __instance.roomEventText.optionList.get(index).msg += " (" + String.format(Locale.ENGLISH, "%.2f", evaluateRemovingStartingRelicAndAddingARandomBossRelic()) + ")";
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException exception) {
                // Empty catch block
            }
        }

        private static float evaluateAddingARareColorlessCard() {
            CardEvaluationData evaluations = CardEvaluationData.createByAdding(
                CardLibrary.getCardList(CardLibrary.LibraryType.COLORLESS).stream().filter(card -> card.rarity == AbstractCard.CardRarity.RARE).collect(Collectors.toList()),
                AbstractDungeon.actNum,
                4
            );

            float evaluation = 0;
            for (StatEvaluation statEvaluation : evaluations.getEvals().values()) {
                evaluation += StatEvaluation.determineWeightedScoreForCurrentAct(statEvaluation);
            }
            evaluation /= evaluations.getEvals().size();

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(evaluations.getSkip());

            return skipEvaluation - evaluation;
        }

        private static float evaluateAddingARandomRareCard() {
            CardEvaluationData evaluations = CardEvaluationData.createByAdding(
                CardLibrary.getAllCards().stream().filter(card -> card.rarity == AbstractCard.CardRarity.RARE).collect(Collectors.toList()),
                AbstractDungeon.actNum,
                4
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
                AbstractDungeon.actNum,
                4
            );

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(evaluations.getSkip());

            return (float)evaluations.getEvals().values().stream().mapToDouble(statEvaluation -> skipEvaluation - StatEvaluation.determineWeightedScoreForCurrentAct(statEvaluation)).max().orElse(0);
        }

        private static float evaluateUpgradingACard() {
            CardEvaluationData evaluations = CardEvaluationData.createByUpgrading(
                AbstractDungeon.player.masterDeck.group,
                AbstractDungeon.actNum,
                4
            );

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(evaluations.getSkip());

            return (float)evaluations.getEvals().values().stream().mapToDouble(statEvaluation -> skipEvaluation - StatEvaluation.determineWeightedScoreForCurrentAct(statEvaluation)).max().orElse(0);
        }

        private static float evaluateAddingAColorlessCard() {
            CardEvaluationData evaluations = CardEvaluationData.createByAdding(
                CardLibrary.getCardList(CardLibrary.LibraryType.COLORLESS),
                AbstractDungeon.actNum,
                4
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
            int startingAct = AbstractDungeon.actNum;
            int endingAct = AbstractDungeon.actNum;

            Set<String> enemies = CardEvaluationData.getAllEnemies(startingAct, endingAct);

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
                AbstractDungeon.actNum,
                4
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
            int startingAct = AbstractDungeon.actNum;
            int endingAct = AbstractDungeon.actNum;

            Set<String> enemies = CardEvaluationData.getAllEnemies(startingAct, endingAct);

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
            float maxEvaluation = 0;
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
            int startingAct = AbstractDungeon.actNum;
            int endingAct = AbstractDungeon.actNum;

            Set<String> enemies = CardEvaluationData.getAllEnemies(startingAct, endingAct);

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(new StatEvaluation(
                AbstractDungeon.player.masterDeck.group,
                AbstractDungeon.player.relics,
                AbstractDungeon.player.maxHealth,
                AbstractDungeon.player.currentHealth,
                AbstractDungeon.ascensionLevel,
                false,
                enemies
            ));

            float evaluation = 0;
            int count = 0;

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
                }
            }

            return evaluation / count;
        }

        private static float evaluateAddingOneRareRelic() {
            CardEvaluationData evaluations = CardEvaluationData.createByAddingRelic(
                RelicLibrary.rareList,
                AbstractDungeon.actNum,
                4
            );

            float evaluation = 0;
            for (StatEvaluation statEvaluation : evaluations.getEvals().values()) {
                evaluation += StatEvaluation.determineWeightedScoreForCurrentAct(statEvaluation);
            }
            evaluation /= evaluations.getEvals().size();

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(evaluations.getSkip());

            return skipEvaluation - evaluation;
        }

        private static float evaluateAddingThreeRareCards() {
            int startingAct = AbstractDungeon.actNum;
            int endingAct = AbstractDungeon.actNum;

            List<AbstractCard> rareCards = CardLibrary.getAllCards().stream().filter(card -> card.rarity == AbstractCard.CardRarity.RARE).collect(Collectors.toList());

            Set<String> enemies = CardEvaluationData.getAllEnemies(startingAct, endingAct);

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
                        List<AbstractCard> newDeck3 = new ArrayList<>(newDeck);
                        newDeck3.add(card3);

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

            return evaluation / count;
        }

        private static float evaluateRemovingStartingRelicAndAddingARandomBossRelic() {
            CardEvaluationData evaluations = CardEvaluationData.createByRemovingStartingRelicAndAddingARandomBossRelic(AbstractDungeon.actNum, 4);

            float evaluation = 0;
            for (StatEvaluation statEvaluation : evaluations.getEvals().values()) {
                evaluation += StatEvaluation.determineWeightedScoreForCurrentAct(statEvaluation);
            }
            evaluation /= evaluations.getEvals().size();

            float skipEvaluation = StatEvaluation.determineWeightedScoreForCurrentAct(evaluations.getSkip());

            return skipEvaluation - evaluation;
        }
    }
}
