package FightPredictor.util;

import FightPredictor.CardEvaluationData;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.city.TheLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;

public class HelperMethods {
    private static DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
    private static DecimalFormat twoDecFormat = new DecimalFormat("##0.00", otherSymbols);
//    private static DecimalFormat twoDecFormat = new DecimalFormat("+##0.00;-#", otherSymbols);

    public static String formatNum(double num) {
        if(num != 9999f) {
            String prefix;
            if (num < 0) {
                prefix = "#r";
            } else if (0 <= num && num <= 0.5) {
                prefix = "[#fce803]";
            } else {
                prefix = "#g";
            }
            return prefix + twoDecFormat.format(num);
        } else {
            return "#y----";
        }
    }

    public static String getPredictionString(AbstractCard c, CardEvaluationData eval, boolean forUpgrade) {
        Map<Object, Map<Integer, Float>> scores = eval.getDiffs();

        if (scores.containsKey(c)) {
            Map<Integer, Float> scoresByAct = scores.get(c);

            float[] scores2 = determineScores(scores, c);

            // Almost all upgrades are always good, so set negative values to low positive value
            if (forUpgrade) {
                if (scores2[0] < 0f) {
                    scores2[0] = 0.03f;
                }
                for (int index = 1; index < scores2.length; index++) {
                    if (scores2[index] < 0f) {
                        scores2[index] = 0.04f;
                    }
                }
            }

            return format(scores2);
        } else {
            return "";
        }
    }

    public static String getPredictionStringForRelic(AbstractRelic relic, CardEvaluationData cardEvaluationData) {
        Map<Object, Map<Integer, Float>> scores = cardEvaluationData.getDiffs();

        if (scores.containsKey(relic)) {
            Map<Integer, Float> scoresByAct = scores.get(relic);

            float[] scores2 = determineScores(scores, relic);

            return format(scores2);
        } else {
            return "";
        }
    }

    private static float[] determineScores(Map<Object, Map<Integer, Float>> scores, Object object) {
        Map<Integer, Float> scoresByAct = scores.get(object);

        float[] scores2 = new float[4 - AbstractDungeon.actNum + 1];
        for (int actNumber = AbstractDungeon.actNum; actNumber <= 4; actNumber++) {
            int index = actNumber - AbstractDungeon.actNum;
            scores2[index] = scoresByAct.getOrDefault(actNumber, 9999f);
        }

        return scores2;
    }

    private static String format(float[] scores) {
        StringBuilder stringBuilder = new StringBuilder(HelperMethods.formatNum(scores[0]));
        for (int index = 1; index < scores.length; index++) {
            stringBuilder.append(" | " + HelperMethods.formatNum(scores[index]));
        }
        return stringBuilder.toString();
    }
}
