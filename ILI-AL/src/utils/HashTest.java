package utils;

import org.bouncycastle.crypto.digests.KeccakDigest;
import org.bouncycastle.util.encoders.Hex;

public class HashTest {
    public static void main(String[] args) {
        String input = "Hello, blockchain!";

        // Convert input string to bytes
        byte[] inputBytes = input.getBytes();

        // Initialize Keccak256 digest
        KeccakDigest digest = new KeccakDigest(256);

        // Update digest with input bytes
        digest.update(inputBytes, 0, inputBytes.length);

        // Finalize hash calculation
        byte[] hashBytes = new byte[digest.getDigestSize()];
        digest.doFinal(hashBytes, 0);

        // Convert hash bytes to hexadecimal string
        String hashHex = Hex.toHexString(hashBytes);

        System.out.println("Keccak256 hash of '" + input + "':");
        System.out.println(hashHex);
    }
}
