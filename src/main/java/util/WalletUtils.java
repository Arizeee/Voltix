package util;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.jcajce.provider.digest.Keccak;

public class WalletUtils {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
        keyGen.initialize(new ECGenParameterSpec("secp256k1"), new SecureRandom());
        return keyGen.generateKeyPair();
    }

    public static String generateWalletAddress(byte[] publicKeyBytes) {
        Keccak.Digest256 keccak256 = new Keccak.Digest256();
        byte[] hash = keccak256.digest(publicKeyBytes);

        byte[] addressBytes = new byte[20];
        System.arraycopy(hash, hash.length - 20, addressBytes, 0, 20);

        return "0x" + Hex.toHexString(addressBytes);
    }

    public static String getWalletAddressFromPrivateKeyHex(String privateKeyHex) {
        BigInteger privKeyInt = new BigInteger(privateKeyHex, 16);
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
        ECPoint Q = ecSpec.getG().multiply(privKeyInt);
        byte[] publicKeyBytes = Q.getEncoded(false); // Uncompressed public key (0x04...)

        // Keccak-256 hash of public key (tanpa 0x04)
        byte[] hash = keccak256(Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));

        // Ambil 20 byte terakhir
        byte[] addressBytes = Arrays.copyOfRange(hash, 12, hash.length);

        return "0x" + Hex.toHexString(addressBytes);
    }

    private static byte[] keccak256(byte[] input) {
        Keccak.Digest256 keccak = new Keccak.Digest256();
        keccak.update(input, 0, input.length);
        return keccak.digest();
    }

    public static String toHex(byte[] data) {
        return Hex.toHexString(data);
    }
}
