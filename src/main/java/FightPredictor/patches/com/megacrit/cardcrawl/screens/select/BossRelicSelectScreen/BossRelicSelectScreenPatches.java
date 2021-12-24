package FightPredictor.patches.com.megacrit.cardcrawl.screens.select.BossRelicSelectScreen;

import FightPredictor.FightPredictor;
import FightPredictor.util.HelperMethods;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.chests.BossChest;
import com.megacrit.cardcrawl.rooms.TreasureRoomBoss;
import com.megacrit.cardcrawl.screens.select.BossRelicSelectScreen;

import java.util.ArrayList;

public class BossRelicSelectScreenPatches {
    @SpirePatch(clz = BossRelicSelectScreen.class, method = "render")
    public static class RenderPatch {
        @SpirePostfixPatch
        public static void postRender(BossRelicSelectScreen __instance, SpriteBatch spriteBatch) {
            BossChest chest = (BossChest) ((TreasureRoomBoss) AbstractDungeon.getCurrRoom()).chest;
            ArrayList<AbstractRelic> relics = chest.relics;
            for (int index = 0; index < relics.size(); index++) {
                AbstractRelic relic = relics.get(index);
                float offsetX = determineOffsetX(index);
                renderScore(spriteBatch, relic, offsetX);
            }
        }

        private static float determineOffsetX(int index) {
            float offsetX = 0;

            if (index == 1) {
                offsetX = -20;
            } else if (index == 2) {
                offsetX = 20;
            }

            return offsetX;
        }

        private static void renderScore(SpriteBatch spriteBatch, AbstractRelic relic, float offsetX) {
            String text = HelperMethods.getPredictionStringForRelic(relic, FightPredictor.relicChoiceEvaluations);
            spriteBatch.setColor(Color.WHITE);
            FontHelper.renderSmartText(
                    spriteBatch,
                    FontHelper.cardDescFont_N,
                    text,
                    relic.hb.cX - FontHelper.getSmartWidth(FontHelper.cardDescFont_N, text, Float.MAX_VALUE, FontHelper.cardDescFont_N.getSpaceWidth()) * 0.5f + offsetX,
                    relic.hb.y + (12f * Settings.scale),
                    Color.WHITE
            );
        }
    }
}
