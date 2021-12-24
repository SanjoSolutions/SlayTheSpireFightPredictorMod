package FightPredictor.patches.com.megacrit.cardcrawl.shop.StoreRelic;


import FightPredictor.patches.com.megacrit.cardcrawl.screens.ShopScreen.ShopScreenPatches;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.shop.StoreRelic;

public class StoreRelicPatches {
    @SpirePatch(clz = StoreRelic.class, method = "purchaseRelic")
    public static class StoreRelicPurchaseRelicPatch {
        @SpirePostfixPatch
        public static void patch(StoreRelic __instance) {
            ShopScreenPatches.InitCardHook.updatePredictions();
        }
    }
}
