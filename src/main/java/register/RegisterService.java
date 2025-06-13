package register;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import util.WalletUtils;

import java.security.KeyPair;

public class RegisterService {
    private static final String USERS_URL = "https://ksimjlnjcuyuclcckimg.supabase.co/rest/v1/users";
    private static final String WALLET_URL = "https://ksimjlnjcuyuclcckimg.supabase.co/rest/v1/wallets";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtzaW1qbG5qY3V5dWNsY2NraW1nIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDkwMDA3MzAsImV4cCI6MjA2NDU3NjczMH0.Aw-rpLCI82t6Guh15U8vwxK_PFXBKZa0cbxgJ_AVMP8";

    private static final OkHttpClient client = new OkHttpClient();

    public static void registerUser(String nickname) throws Exception {
        // Cek apakah username sudah ada
        if (isUsernameTaken(nickname)) {
            throw new RuntimeException("Username sudah dipakai!");
        }

        // Generate key pair
        KeyPair keyPair = WalletUtils.generateKeyPair();
        String privateKey = WalletUtils.toHex(keyPair.getPrivate().getEncoded());
        String publicKey = WalletUtils.toHex(keyPair.getPublic().getEncoded());
        String walletAddress = WalletUtils.generateWalletAddress(keyPair.getPublic().getEncoded());
        String backupPhrase = "backup-" + System.currentTimeMillis();

        // Simpan ke tabel users
        JSONObject userJson = new JSONObject();
        userJson.put("username", nickname);
        userJson.put("private_key", privateKey);
        userJson.put("public_key", publicKey);
        userJson.put("wallet_address", walletAddress);
        userJson.put("backup_phrase", backupPhrase);

        RequestBody userBody = RequestBody.create(
            userJson.toString(),
            MediaType.parse("application/json")
        );

        Request userRequest = new Request.Builder()
            .url(USERS_URL)
            .post(userBody)
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=representation")
            .build();

        String userId;
        try (Response response = client.newCall(userRequest).execute()) {
            String resBody = response.body().string();

            if (!response.isSuccessful()) {
                throw new RuntimeException("Gagal simpan user: " + resBody);
            }

            JSONArray arr = new JSONArray(resBody);
            userId = arr.getJSONObject(0).getString("id");
            System.out.println("User berhasil disimpan. ID: " + userId);
        }

        // Simpan ke tabel wallet
        JSONObject walletJson = new JSONObject();
        walletJson.put("user_id", userId);
        walletJson.put("wallet_address", walletAddress);
        walletJson.put("balance", 0);

        RequestBody walletBody = RequestBody.create(
            walletJson.toString(),
            MediaType.parse("application/json")
        );

        Request walletRequest = new Request.Builder()
            .url(WALLET_URL)
            .post(walletBody)
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=representation")
            .build();

        try (Response response = client.newCall(walletRequest).execute()) {
            String walletRes = response.body().string();

            if (!response.isSuccessful()) {
                throw new RuntimeException("Gagal simpan wallet: " + walletRes);
            }

            System.out.println("Wallet berhasil disimpan: " + walletRes);
        }

        System.out.println("✅ Register sukses!");
        System.out.println("Nickname: " + nickname);
        System.out.println("Wallet Address: " + walletAddress);
    }

    // Fungsi cek username apakah sudah dipakai
    public static boolean isUsernameTaken(String nickname) throws Exception {
        HttpUrl url = HttpUrl.parse(USERS_URL).newBuilder()
            .addQueryParameter("username", "eq." + nickname)
            .build();

        Request request = new Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .build();

        try (Response response = client.newCall(request).execute()) {
            String res = response.body().string();
            JSONArray arr = new JSONArray(res);
            return arr.length() > 0;
        }
    }
}
