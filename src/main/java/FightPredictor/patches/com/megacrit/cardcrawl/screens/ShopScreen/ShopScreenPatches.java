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
import com.megacrit.cardcrawl.shop.StoreRelic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShopScreenPatches {

    @SpirePatch(clz = ShopScreen.class, method = "init")
    public static class InitCardHook {
        public static List<AbstractCard> allCards = new ArrayList<>();
        public static List<StoreRelic> relics = null;

        @SpirePostfixPatch
        public static void patch(ShopScreen __instance, ArrayList<AbstractCard> coloredCards, ArrayList<AbstractCard> colorlessCards) {
            InitCardHook.allCards = new ArrayList<>();
            InitCardHook.allCards.addAll(coloredCards);
            InitCardHook.allCards.addAll(colorlessCards);

            try {
                Field relicsField = __instance.getClass().getDeclaredField("relics");
                relicsField.setAccessible(true);
                relics = (ArrayList<StoreRelic>)relicsField.get(__instance);
            } catch (NoSuchFieldException | IllegalAccessException exception) {
                // Empty catch block
            }

            InitCardHook.updatePredictions();
        }

        public static void updatePredictions() {
            int startingAct = AbstractDungeon.actNum;
            int endingAct = 4;

            FightPredictor.cardChoicesEvaluations = CardEvaluationData.createByAdding(
                    InitCardHook.allCards,
                    startingAct,
                    endingAct
            );

            FightPredictor.relicChoiceEvaluations = CardEvaluationData.createByAddingRelic(
                    relics.stream().map(relic -> relic.relic).collect(Collectors.toList()),
                    startingAct,
                    endingAct
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

    @SpirePatch(clz = ShopScreen.class, method = "renderRelics")
    public static class ShopScreenRenderRelicsPatch {
        @SpirePostfixPatch
        public static void postRenderRelics(ShopScreen __instance, SpriteBatch spriteBatch) {
            try {
                Field relicsField = __instance.getClass().getDeclaredField("relics");
                relicsField.setAccessible(true);
                ArrayList<StoreRelic> relics = (ArrayList<StoreRelic>) relicsField.get(__instance);
                for (int index = 0; index < relics.size(); index++) {
                    StoreRelic relic = relics.get(index);
                    float offsetY = determineOffsetY(index, relics.size());
                    renderPrediction(spriteBatch, relic, offsetY);
                }
            } catch (NoSuchFieldException | IllegalAccessException exception) {
                // Empty catch block
            }
        }

        private static float determineOffsetY(int index, int length) {
            float offsetY = 0;

            if (index == 0) {
                offsetY = -100;
            } else if (index == length - 1) {
                offsetY = -100;
            }

            return offsetY;
        }

        public static void renderPrediction(SpriteBatch spriteBatch, StoreRelic relic, float offsetY) {
            String text = HelperMethods.getPredictionStringForRelic(relic.relic, FightPredictor.relicChoiceEvaluations);
            spriteBatch.setColor(Color.WHITE);
            FontHelper.renderSmartText(
                    spriteBatch,
                    FontHelper.cardDescFont_N,
                    text,
                    relic.relic.hb.cX - FontHelper.getSmartWidth(FontHelper.cardDescFont_N, text, Float.MAX_VALUE, FontHelper.cardDescFont_N.getSpaceWidth()) * 0.5f,
                    relic.relic.hb.y + (12f * Settings.scale) + offsetY,
                    Color.WHITE
            );
        }
    }
}
