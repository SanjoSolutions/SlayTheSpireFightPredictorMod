package FightPredictor.patches.com.megacrit.cardcrawl.screens.DungeonMapScreen;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.screens.DungeonMapScreen;
import FightPredictor.FightPredictor;

public class DungeonMapScreenPatches {
    @SpirePatch(clz = DungeonMapScreen.class, method = "open")
    public static class OpenPatch {
        @SpirePostfixPatch
        public static void PostOpen(DungeonMapScreen __instance, boolean doScrollingAnimation) {
            FightPredictor.generateDataForMapNodes();
        }
    }
}
