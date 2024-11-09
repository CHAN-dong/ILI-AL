package dataowner;

import static dataowner.Parameter.P_ALT;
import static dataowner.Parameter.err;

public class Parameter {

    public static boolean useBitset = false;
    public static int err = 15;

    //partition per position
    public static int P_ALT = 2;
    //function counts
    public static int P_LMF = 8;
    public static int l = (int) (P_LMF * Math.log(2));

    public static int OF_ALT = 256;
    public static int OF_LMF = 256;

    public static int rate = (int) 1e6;

    public static void setALTPar(int err, int P_ALT, boolean useBitset) {
        Parameter.useBitset = useBitset;
        Parameter.err = err;
        Parameter.P_ALT = P_ALT;
        OF_ALT = -(P_ALT * ((err - 1) / 2 + 1) * 2);

    }

    public static void setLMFPar(int P_LMF) {
        Parameter.P_LMF = P_LMF;
        Parameter.l = Math.max((int) (P_LMF * Math.log(2)), 1);
        Parameter.OF_LMF = Math.max(err * P_LMF, 256);
    }
}
