package e.natasja.sugar_kidz;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Natasja on 23-1-2018.
 * This is a class that creates a random, safe code of 8 characters. It is used to generate a couple
 * code that is used to follow a kid's account.
 */

public class CodeGenerator {
    private static SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String nextSessionId() {
        return new BigInteger(40, SECURE_RANDOM).toString(32).toUpperCase();
    }

}
