package config;

import okhttp3.*;
import org.json.JSONObject;

public class SupabaseWallet {
    private static final String SUPABASE_URL = "https://ksimjlnjcuyuclcckimg.supabase.co/rest/v1/wallets";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtzaW1qbG5qY3V5dWNsY2NraW1nIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0OTAwMDczMCwiZXhwIjoyMDY0NTc2NzMwfQ.njmN8y4e8lbgdSES_VlAyLONDdG4ZH5PTkE0TDjDODc";

    public static void registerWalletToTable(String userId, String walletAddress) throws Exception {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        json.put("user_id", userId);  // harus UUID valid
        json.put("wallet_address", walletAddress);
        json.put("balance", 0); // JANGAN dalam string! Harus number (bukan "0")

        RequestBody body = RequestBody.create(
            json.toString(),
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
            .url(SUPABASE_URL)
            .post(body)
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=representation")

            .build();

 try (Response response = client.newCall(request).execute()) {
    String resp = response.body().string();
    System.out.println("Wallet Response Code: " + response.code());
    System.out.println("Wallet Response Body: " + resp);

    if (!response.isSuccessful()) {
        throw new RuntimeException("Gagal simpan wallet: " + resp);
    }
}


    }
}
