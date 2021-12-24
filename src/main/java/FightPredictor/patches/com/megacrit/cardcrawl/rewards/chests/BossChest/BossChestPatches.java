package FightPredictor.patches.com.megacrit.cardcrawl.rewards.chests.BossChest;

import FightPredictor.FightPredictor;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rewards.chests.BossChest;
import FightPredictor.CardEvaluationData;

import java.util.stream.Collectors;

public class BossChestPatches {
    @SpirePatch(clz = BossChest.class, method = SpirePatch.CONSTRUCTOR)
    public static class ConstructorPatch {
        @SpirePostfixPatch
        public static void postConstructor(BossChest __instance) {
            int startingAct = AbstractDungeon.actNum;
            int endingAct = 4;

            FightPredictor.relicChoiceEvaluations = CardEvaluationData.createByAddingRelic(
                    __instance.relics,
                    startingAct,
                    endingAct
            );
        }
    }
}
