package config;

import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SupabaseWallet {
    private static final String USERS_URL = "https://ksimjlnjcuyuclcckimg.supabase.co/rest/v1/users";
    private static final String WALLETS_URL = "https://ksimjlnjcuyuclcckimg.supabase.co/rest/v1/wallets";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtzaW1qbG5qY3V5dWNsY2NraW1nIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0OTAwMDczMCwiZXhwIjoyMDY0NTc2NzMwfQ.njmN8y4e8lbgdSES_VlAyLONDdG4ZH5PTkE0TDjDODc";


    // ✅ Tambahkan method untuk register wallet ke tabel wallet
    public static void registerWalletToTable(String userId, String walletAddress) throws IOException {
    OkHttpClient client = new OkHttpClient();

    JSONObject json = new JSONObject();
    json.put("user_id", userId);
    json.put("wallet_address", walletAddress);
    json.put("balance", 0);

    RequestBody body = RequestBody.create(
        json.toString(),
        MediaType.parse("application/json; charset=utf-8")
    );

    Request request = new Request.Builder()
        .url(WALLETS_URL)
        .post(body)
        .addHeader("apikey", API_KEY)
        .addHeader("Authorization", "Bearer " + API_KEY)
        .addHeader("Content-Type", "application/json")
        .addHeader("Prefer", "return=representation") // 🛠️ Tambahkan ini!
        .build();

    try (Response response = client.newCall(request).execute()) {
        String responseBody = response.body().string();

        System.out.println("📥 Wallet POST Response Code: " + response.code());
        System.out.println("📥 Wallet POST Response Body: " + responseBody);

        if (!response.isSuccessful()) {
            throw new IOException("❌ Gagal menyimpan wallet: " + responseBody);
        } else {
            System.out.println("✅ Wallet berhasil disimpan: " + responseBody);
        }
    }
}


    // ✅ Ambil PRIVATE KEY berdasarkan user_id
    public static String getPrivateKeyByUserId(String userId) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
            .url(USERS_URL + "?id=eq." + userId + "&select=private_key")
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonArray = mapper.readTree(responseBody);

                if (jsonArray.isArray() && jsonArray.size() > 0) {
                    return jsonArray.get(0).get("private_key").asText();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ✅ Ambil semua data dari tabel users
    public static String getAllUsers() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
            .url(USERS_URL + "?select=*")
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ✅ Ambil semua data dari tabel wallets
    public static String getAllWallets() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
            .url(WALLETS_URL + "?select=*")
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public static JSONObject getWalletByUserId(String userId) throws IOException, JSONException {
    OkHttpClient client = new OkHttpClient();

    HttpUrl url = HttpUrl.parse(WALLETS_URL)
        .newBuilder()
        .addQueryParameter("user_id", "eq." + userId)
        .build();

    Request request = new Request.Builder()
        .url(url)
        .get()
        .addHeader("apikey", API_KEY)
        .addHeader("Authorization", "Bearer " + API_KEY)
        .addHeader("Content-Type", "application/json")
        .build();

    try (Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful()) throw new IOException("Failed to get wallet: " + response.body().string());

        String responseBody = response.body().string();
        JSONArray resultArray = new JSONArray(responseBody);
        return resultArray.length() > 0 ? resultArray.getJSONObject(0) : null;
    }
}


}
