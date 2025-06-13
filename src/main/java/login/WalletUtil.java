/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package login;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import java.math.BigInteger;
import java.util.Arrays;
import org.bouncycastle.jcajce.provider.digest.Keccak;
/**
 *
 * @author 62877
 */
public class WalletUtil {
    public static String getWalletAddressFromPrivateKeyHex(String privateKeyHex) {
        BigInteger privKeyInt = new BigInteger(privateKeyHex, 16);
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
        ECPoint Q = ecSpec.getG().multiply(privKeyInt);
        byte[] publicKeyBytes = Q.getEncoded(false);

        // Keccak-256 hash of public key (tanpa byte 0x04)
        byte[] hash = keccak256(Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));

        // Ambil 20 byte terakhir
        byte[] addressBytes = Arrays.copyOfRange(hash, 12, hash.length);

        return "0x" + Hex.toHexString(addressBytes);
    }

    public static byte[] keccak256(byte[] input) {
        Keccak.Digest256 keccak = new Keccak.Digest256();
        keccak.update(input, 0, input.length);
        return keccak.digest();
    }
}
