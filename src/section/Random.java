package section;

import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author TrongDai
 */
public class Random {
    public static int RandInteger(int min, int max) {
        int result = 0;
        for (int i = 0; i < 10; i++) {
            result = ThreadLocalRandom.current().nextInt(((max - min) + 1)) + min;
        }
        return result;
    }

    public static double RandDouble(double min, double max) {
        double result = 0.0;
        for (int i = 0; i < 10; i++) {
            result = ThreadLocalRandom.current().nextDouble(((max - min) + 1)) + min;
        }
        return result;
    }
}
