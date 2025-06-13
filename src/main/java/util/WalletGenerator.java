/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
/**
 *
 * @author 62877
 */
public class WalletGenerator {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
        keyGen.initialize(new ECGenParameterSpec("secp256k1"), new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();

        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        String address = WalletUtils.generateWalletAddress(publicKeyBytes);

        System.out.println("Public Key: " + Hex.toHexString(publicKeyBytes));
        System.out.println("Wallet Address: " + address);
    }
}
