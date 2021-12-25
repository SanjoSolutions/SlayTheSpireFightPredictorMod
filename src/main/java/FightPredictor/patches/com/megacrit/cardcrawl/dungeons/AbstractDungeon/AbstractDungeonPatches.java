package FightPredictor.patches.com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import FightPredictor.FightPredictor;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;

public class AbstractDungeonPatches {
    @SpirePatch(clz = AbstractDungeon.class, method = "setCurrMapNode")
    public static class SetCurrMapNodePatch {
        @SpirePostfixPatch
        public static void postSetCurrMapNode(MapRoomNode currentMapNode) {
            FightPredictor.generateDataForMapNodes();
        }
    }
}
