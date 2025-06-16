package config;

import java.io.IOException;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import register.WalletData;

public class SupabaseUsers {
    private static final String SUPABASE_URL = "https://ksimjlnjcuyuclcckimg.supabase.co/rest/v1/users";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtzaW1qbG5qY3V5dWNsY2NraW1nIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0OTAwMDczMCwiZXhwIjoyMDY0NTc2NzMwfQ.njmN8y4e8lbgdSES_VlAyLONDdG4ZH5PTkE0TDjDODc";

    private static String currentUserId; // Menyimpan user_id global

    public static void setCurrentUserId(String userId) {
        currentUserId = userId;
    }

    public static String getCurrentUserId() {
        return currentUserId;
    }

        public static void registerWallet() throws Exception {
        OkHttpClient client = new OkHttpClient();

        // 1. Cek apakah username sudah ada
        HttpUrl url = HttpUrl.parse(SUPABASE_URL).newBuilder()
            .addQueryParameter("username", "eq." + WalletData.nickname)
            .build();

        Request checkUserRequest = new Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .addHeader("Content-Type", "application/json")
            .build();

        String userId;

        try (Response response = client.newCall(checkUserRequest).execute()) {
            String responseBody = response.body().string();
            JSONArray resArray = new JSONArray(responseBody);

            if (resArray.length() > 0) {
                // User sudah ada
                userId = resArray.getJSONObject(0).getString("id");
            } else {
                // Buat user baru
                JSONObject json = new JSONObject();
                json.put("username", WalletData.nickname);
                json.put("public_key", WalletData.publicKey);
                json.put("private_key", WalletData.privateKey);
                json.put("wallet_address", WalletData.walletAddress);
                json.put("backup_phrase", WalletData.backupPhrase);

                RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
                );

                Request createUserRequest = new Request.Builder()
                    .url(SUPABASE_URL)
                    .post(body)
                    .addHeader("apikey", API_KEY)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .build();

                try (Response createResponse = client.newCall(createUserRequest).execute()) {
                    if (!createResponse.isSuccessful()) {
                        throw new RuntimeException("Gagal buat user baru: " + createResponse.body().string());
                    }

                    String createBody = createResponse.body().string();
                    JSONArray createdArr = new JSONArray(createBody);
                    userId = createdArr.getJSONObject(0).getString("id");
                }
            }

            // ✅ Simpan ID ke variabel global
            setCurrentUserId(userId);
            System.out.println("✅ currentUserId di-set: " + userId);
        }

    // 2. Simpan wallet ke tabel wallet
    SupabaseWallet.registerWalletToTable(currentUserId, WalletData.walletAddress);
}


    public static String getPrivateKeyFromUser() {
        OkHttpClient client = new OkHttpClient();

        String userId = getCurrentUserId(); // pastikan tidak null

        if (userId == null || userId.isEmpty()) {
            System.err.println("❌ currentUserId kosong! Pastikan registerWallet() sudah dipanggil.");
            return null;
        }

        String url = SUPABASE_URL + "?id=eq." + userId + "&select=private_key";

        Request request = new Request.Builder()
            .url(url)
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();

            System.out.println("📡 GET private_key response: " + response.code());
            System.out.println("📦 Body: " + responseBody);

            if (response.isSuccessful()) {
                JSONArray jsonArray = new JSONArray(responseBody);

                if (jsonArray.length() > 0) {
                    return jsonArray.getJSONObject(0).getString("private_key");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public static JSONObject getUserById(String userId) throws IOException, JSONException {
    OkHttpClient client = new OkHttpClient();

    // ❌ Salah:
    // HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/users")

    // ✅ Benar:
    HttpUrl url = HttpUrl.parse(SUPABASE_URL)
        .newBuilder()
        .addQueryParameter("id", "eq." + userId)
        .build();

    Request request = new Request.Builder()
        .url(url)
        .get()
        .addHeader("apikey", API_KEY)
        .addHeader("Authorization", "Bearer " + API_KEY)
        .addHeader("Content-Type", "application/json")
        .build();

    try (Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful()) throw new IOException("Failed to get user: " + response.body().string());

        String responseBody = response.body().string();
        JSONArray resultArray = new JSONArray(responseBody);
        return resultArray.length() > 0 ? resultArray.getJSONObject(0) : null;
    }
}


}
