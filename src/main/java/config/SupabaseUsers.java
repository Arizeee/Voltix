/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import register.WalletData;
/**
 *
 * @author 62877
 */
public class SupabaseUsers {
    private static final String SUPABASE_URL = "https://ksimjlnjcuyuclcckimg.supabase.co/rest/v1/users";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtzaW1qbG5qY3V5dWNsY2NraW1nIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0OTAwMDczMCwiZXhwIjoyMDY0NTc2NzMwfQ.njmN8y4e8lbgdSES_VlAyLONDdG4ZH5PTkE0TDjDODc";

    public static void registerWallet() throws Exception {
    OkHttpClient client = new OkHttpClient();

    // Langkah 1: Cek apakah username sudah ada
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
        
    }


    // Langkah 2: Simpan wallet
    SupabaseWallet.registerWalletToTable(userId, WalletData.walletAddress);
}

}
