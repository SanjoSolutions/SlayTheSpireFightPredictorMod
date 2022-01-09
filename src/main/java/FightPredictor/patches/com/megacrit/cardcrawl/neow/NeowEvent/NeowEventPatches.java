package FightPredictor.patches.com.megacrit.cardcrawl.neow.NeowEvent;

import FightPredictor.FightPredictor;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.neow.NeowEvent;
import com.megacrit.cardcrawl.neow.NeowReward;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class NeowEventPatches {
    @SpirePatch(clz = NeowEvent.class, method = "blessing")
    public static class BlessingPatch {
        @SpirePostfixPatch
        public static void blessing(NeowEvent __instance) {
            try {
                Field rewardsField = __instance.getClass().getDeclaredField("rewards");
                rewardsField.setAccessible(true);
                ArrayList<NeowReward> rewards = (ArrayList<NeowReward>)rewardsField.get(__instance);
                NeowReward reward = rewards.get(3);
                FightPredictor.logger.info("reward option label: " + reward.optionLabel);
                reward.optionLabel += " (test)";
            } catch (NoSuchFieldException | IllegalAccessException exception) {
                // Empty catch block
            }
        }
    }
}
