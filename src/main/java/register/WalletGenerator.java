package register;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Random;

public class WalletGenerator {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // Generate KeyPair secp256k1
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        keyGen.initialize(ecSpec, new SecureRandom());
        return keyGen.generateKeyPair();
    }

    // Generate Private Key Hex
    public static String getPrivateKeyHex(PrivateKey privateKey) {
        byte[] encoded = privateKey.getEncoded();
        return bytesToHex(encoded);
    }

    // Generate Public Key Hex
    public static String getPublicKeyHex(PublicKey publicKey) {
        byte[] encoded = publicKey.getEncoded();
        return bytesToHex(encoded);
    }

    // Generate Dummy Wallet Address (you can replace with actual Keccak or RIPEMD hash later)
    public static String generateWalletAddress(PublicKey publicKey) {
        byte[] pubBytes = publicKey.getEncoded();
        int hash = java.util.Arrays.hashCode(pubBytes);
        return "0x" + Integer.toHexString(hash).replace("-", "");
    }

    // Generate 12-word backup phrase
    public static String generateBackupPhrase() {
        String characters = "abcdefghijklmnopqrstuvwxyz";
        Random rand = new Random();
        StringBuilder phrase = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 5; j++) {
                phrase.append(characters.charAt(rand.nextInt(characters.length())));
            }
            if (i < 11) phrase.append(" ");
        }
        return phrase.toString();
    }

    // Helper: Byte array to hex
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte temp : bytes) {
            result.append(String.format("%02x", temp));
        }
        return result.toString();
    }
}
