/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package login;
import java.math.BigInteger;
import java.security.*;
import java.util.Arrays;
import javax.swing.JOptionPane;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
/**
 *
 * @author 62877
 */
public class LoginFromPrivateKey {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static String walletAddressFromPrivateKeyHex(String privateKeyHex) throws Exception {
        // 1. Convert HEX to BigInteger
        BigInteger s = new BigInteger(1, Hex.decode(privateKeyHex));

        // 2. Buat PrivateKey object
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
        ECPrivateKeySpec keySpec = new ECPrivateKeySpec(s, ecSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        // 3. Hitung Public Key dari Private Key
        ECPoint Q = ecSpec.getG().multiply(s);
        byte[] publicKeyBytes = Q.getEncoded(false); // uncompressed

        // 4. Keccak256 Hash (Ethereum style)
        MessageDigest keccak = MessageDigest.getInstance("KECCAK-256");
        byte[] hashed = keccak.digest(publicKeyBytes);
        byte[] addressBytes = Arrays.copyOfRange(hashed, 12, 32);

        // 5. Hasil Wallet Address
        return "0x" + Hex.toHexString(addressBytes);
    }

    public static void main(String[] args) {
        try {
            // Misalnya ambil dari TextField user
            String privateKeyHex = JOptionPane.showInputDialog("Paste your private key:");

            // Cek apakah null/empty
            if (privateKeyHex == null || privateKeyHex.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Private key kosong!");
                return;
            }

            String walletAddress = walletAddressFromPrivateKeyHex(privateKeyHex);
            JOptionPane.showMessageDialog(null, "Wallet address dari private key:\n" + walletAddress);

            // 🔁 Tambahkan pengecekan ke database Supabase:
            //    -> Cari `wallet` dengan `wallet_address` = walletAddress
            //    -> Jika ada, login sukses
            //    -> Jika tidak, tampilkan pesan "Wallet tidak ditemukan"

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal login: " + e.getMessage());
        }
    }
}
