package FightPredictor.patches.com.megacrit.cardcrawl.screens.ShopScreen;

import FightPredictor.FightPredictor;
import FightPredictor.util.HelperMethods;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.shop.ShopScreen;
import FightPredictor.CardEvaluationData;

import java.util.ArrayList;
import java.util.List;

public class ShopScreenPatches {

    @SpirePatch(clz = ShopScreen.class, method = "init")
    public static class InitCardHook {
        public static List<AbstractCard> allCards = new ArrayList<>();

        @SpirePostfixPatch
        public static void patch(ShopScreen __instance, ArrayList<AbstractCard> coloredCards, ArrayList<AbstractCard> colorlessCards) {
            InitCardHook.allCards = new ArrayList<>();
            InitCardHook.allCards.addAll(coloredCards);
            InitCardHook.allCards.addAll(colorlessCards);

            InitCardHook.updatePredictions();
        }

        public static void updatePredictions() {
            FightPredictor.cardChoicesEvaluations = CardEvaluationData.createByAdding(
                    InitCardHook.allCards,
                    AbstractDungeon.actNum,
                    Math.min(AbstractDungeon.actNum + 1, 4)
            );
        }
    }

    @SpirePatch(clz = ShopScreen.class, method = "renderCardsAndPrices")
    public static class RenderShopCardEvaluations {
        @SpirePostfixPatch
        public static void patch(ShopScreen __instance, SpriteBatch sb) {
            for(AbstractCard c : __instance.coloredCards) {
                renderGridSelectPrediction(sb, c);
            }
            for(AbstractCard c : __instance.colorlessCards) {
                renderGridSelectPrediction(sb, c);
            }
        }

        private static void renderGridSelectPrediction(SpriteBatch sb, AbstractCard c) {
            String s = HelperMethods.getPredictionString(c, FightPredictor.cardChoicesEvaluations, false);
            sb.setColor(Color.WHITE);
            FontHelper.renderSmartText(sb,
                    FontHelper.cardDescFont_N,
                    s,
                    c.hb.cX - FontHelper.getSmartWidth(FontHelper.cardDescFont_N, s, Float.MAX_VALUE, FontHelper.cardDescFont_N.getSpaceWidth()) * 0.5f,
                    c.hb.y + (12f * Settings.scale),
                    Color.WHITE);
        }
    }

    @SpirePatch(clz = ShopScreen.class, method = "purchaseCard")
    public static class ShopScreenPurchaseCardPatch {
        @SpirePostfixPatch
        public static void patch(ShopScreen __instance, AbstractCard hoveredCard) {
            InitCardHook.updatePredictions();
        }
    }

    @SpirePatch(clz = ShopScreen.class, method = "updatePurge")
    public static class ShopScreenPurgeCardPatch {
        static boolean wasPurgeAvailableLastUpdate = true;

        @SpirePostfixPatch
        public static void postUpdatePurge() {
            if (wasPurgeAvailableLastUpdate && !AbstractDungeon.shopScreen.purgeAvailable) {
                InitCardHook.updatePredictions();
            }
            wasPurgeAvailableLastUpdate = AbstractDungeon.shopScreen.purgeAvailable;
        }
    }
}
