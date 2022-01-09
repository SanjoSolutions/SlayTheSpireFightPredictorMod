package FightPredictor.patches.com.megacrit.cardcrawl.map.MapRoomNode;

import FightPredictor.FightPredictor;
import FightPredictor.util.HelperMethods;
import FightPredictor.util.StatEvaluation;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.map.MapRoomNode;

public class MapRoomNodePatches {
    @SpirePatch(clz = MapRoomNode.class, method = "render")
    public static class RenderPatch {
        @SpirePostfixPatch
        public static void render(MapRoomNode __instance, SpriteBatch spriteBatch) {
            MapRoomNode currentMapNode = AbstractDungeon.getCurrMapNode();
            if (!__instance.taken && __instance.y > currentMapNode.y) {
                Integer predictedHP = FightPredictor.predictedHPOnMapNodes.get(__instance);
                if (predictedHP != null) {
                    // String text = Float.toString(StatEvaluation.determineScoreForNode(__instance, AbstractDungeon.actNum));
                    String text = predictedHP.toString();
                    spriteBatch.setColor(Color.WHITE);
                    FontHelper.renderSmartText(
                            spriteBatch,
                            FontHelper.cardDescFont_N,
                            text,
                            __instance.hb.cX - FontHelper.getSmartWidth(FontHelper.cardDescFont_N, text, Float.MAX_VALUE, FontHelper.cardDescFont_N.getSpaceWidth()) * 0.5f,
                            __instance.hb.y + (12f * Settings.scale),
                            Color.WHITE
                    );
                }
            }
        }
    }
}
