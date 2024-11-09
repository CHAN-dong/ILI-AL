package utils;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.ArrayList;
import java.util.List;

public class BLS {
    // 加载BN254曲线参数文件
    static public Pairing pairing = PairingFactory.getPairing("BN254.properties");
    static Element g = pairing.getG1().newRandomElement().getImmutable();
    static Element sk = pairing.getZr().newRandomElement().getImmutable();
    static Element pk = g.powZn(sk).getImmutable();

    public BLS() {
    }

    public static Element sign(String message) {
        byte[] hash = SHA.hashToBytes(message);
        Element hm = pairing.getG1().newElement().setFromHash(hash, 0, hash.length);
        return hm.mulZn(sk).getImmutable();
    }

    public static Element aggregate(List<Element> sigList) {
        Element aggSig = pairing.getG1().newZeroElement().getImmutable();
        for (Element sig : sigList) {
            aggSig = aggSig.add(sig);
        }
        return aggSig;
    }

    public static boolean verify(Element sig, String message) {
        byte[] hash = SHA.hashToBytes(message);
        Element hm = pairing.getG1().newElement().setFromHash(hash, 0, hash.length);
        Element v1 = pairing.pairing(sig, g).getImmutable();
        Element v2 = pairing.pairing(hm, pk).getImmutable();
        return v1.equals(v2);
    }

    public static boolean verify(Element sig, Element hm) {
        Element v1 = pairing.pairing(sig, g).getImmutable();
        Element v2 = pairing.pairing(hm, pk).getImmutable();
        return v1.equals(v2);
    }

    public static boolean aggVerify(Element aggSig, List<String> messages) {
        Element hm = pairing.getG1().newZeroElement().getImmutable();
        for (String message : messages) {
            byte[] hash = SHA.hashToBytes(message);
            hm.add(pairing.getG1().newElement().setFromHash(hash, 0, hash.length));
        }
        Element v1 = pairing.pairing(aggSig, g).getImmutable();
        Element v2 = pairing.pairing(hm, pk).getImmutable();
        return v1.equals(v2);
    }

    public static void main(String[] args) {

        pairTest();
        long s, e;
        List<String> messageList = new ArrayList<>();
        messageList.add("hello");
        messageList.add("BLS");

        // Signing
        List<Element> sigList = new ArrayList<>();

        s = System.nanoTime();
        sigList.add(sign(messageList.get(0)));
        e = System.nanoTime();
        System.out.println("sign time: " + (e - s) / Math.pow(10,6) + "ms");

        sigList.add(sign(messageList.get(1)));

        s = System.nanoTime();
        boolean verify = verify(sigList.get(0), messageList.get(0));
        e = System.nanoTime();
        System.out.println("ver time: " + (e - s) / Math.pow(10,6) + "ms");

        s = System.nanoTime();
        Element aggSig = aggregate(sigList);
        e = System.nanoTime();
        System.out.println("aggSig time: " + (e - s) / Math.pow(10,6)+ "ms");

        s = System.nanoTime();
        boolean isPass = aggVerify(aggSig, messageList);
        e = System.nanoTime();
        System.out.println("aggVer time: " + (e - s) / Math.pow(10,6) + "ms");
    }

    public static void pairTest() {
        Element zr = pairing.getZr().newRandomElement().getImmutable();
        Element g1 = pairing.getG1().newRandomElement().getImmutable();
        Element g2 = pairing.getG1().newRandomElement().getImmutable();

        long s, e;

        s = System.nanoTime();
        g1.add(g2);
        e = System.nanoTime();
        System.out.println("add time:" + (e - s) / Math.pow(10,6) + "ms");

        s = System.nanoTime();
        g1.mulZn(zr);
        e = System.nanoTime();

        System.out.println("mul time:" + (e - s) / Math.pow(10,6) + "ms");
    }

}
