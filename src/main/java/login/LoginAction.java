package login;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import static config.DatabaseConf.*;
import java.io.IOException;
import java.util.*;
import okhttp3.*;

public class LoginAction {

    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
//        try {
//            loginWithPrivateKey();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.err.println("Terjadi kesalahan: " + e.getMessage());
//        }
    }

    public static void loginWithPrivateKey(String privateKey) throws IOException {
//        System.out.print("Masukkan Private Key: ");
//        String privateKey = "PRIVKEY_TEST_123";
//        String privateKey = scanner.nextLine().trim();

        if (privateKey.isEmpty()) {
            System.out.println("❌ Private key tidak boleh kosong!");
            return;
        }

        System.out.println("\n🔍 Memverifikasi private key...");

        // Cek apakah private key ada di database
        String userData = getUserByPrivateKey(privateKey);

        if (userData != null) {
            // Private key ditemukan = login berhasil
            System.out.println("✅ LOGIN BERHASIL!");
            System.out.println("=".repeat(50));

        } else {
            // Private key tidak ditemukan
            System.out.println("❌ LOGIN GAGAL!");
            System.out.println("Private key tidak ditemukan di database.");
        }
    }

    /**
     * Ambil data user berdasarkan private key
     */
    public static String getUserByPrivateKey(String privateKey) throws IOException {
        // Build endpoint URL, hanya memilih kolom "id"
        String endpoint = SUPABASE_URL
                + "/rest/v1/" + USERS_TABLE
                + "?private_key=eq." + privateKey
                + "&select=id";

        Request request = new Request.Builder()
                .url(endpoint)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Query failed: HTTP " + response.code() + " | " + response.message());
            }

            String responseBody = response.body().string();
            // Contoh responseBody: [ { "id": "123e4567-e89b-12d3-a456-426614174000" } ]

            // Parse JSON menjadi List<Map<String,Object>>
            List<Map<String, Object>> list = objectMapper.readValue(
                    responseBody,
                    new TypeReference<List<Map<String, Object>>>() {
            }
            );

            if (!list.isEmpty()) {
                Object rawId = list.get(0).get("id");
                if (rawId != null) {
                    
                    System.out.println(rawId.toString());
                    return rawId.toString();
                }
            }
            // Tidak ada record atau key "id" tidak ditemukan
            return null;
        }
    }
    
    public static String getUsernameByUserId(String userId) throws IOException {
        String endpoint = SUPABASE_URL + "/rest/v1/users?id=eq." + userId + "&select=username";
        Request request = new Request.Builder()
                .url(endpoint)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;
            String responseBody = response.body().string();
            List<Map<String, Object>> list = objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>(){});
            if (!list.isEmpty()) {
                return list.get(0).get("username").toString();
            }
            return null;
        }
    }

    // Fungsi transfer saldo ke username tujuan
    public static boolean transferToUsername(String senderId, String destUsername, double amount) throws IOException {
        // Cari userId tujuan dari username
        String endpoint = SUPABASE_URL + "/rest/v1/users?username=eq." + destUsername + "&select=id";
        Request request = new Request.Builder()
                .url(endpoint)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        String recipientId = null;
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) return false;
            String responseBody = response.body().string();
            List<Map<String, Object>> list = objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>(){});
            if (!list.isEmpty()) {
                recipientId = list.get(0).get("id").toString();
            } else {
                return false; // username tujuan tidak ditemukan
            }
        }

        // Cek saldo pengirim
        double senderBalance = getUserBalance(senderId);
        if (senderBalance < amount) return false;

        // Update saldo
        boolean senderUpdate = updateUserBalance(senderId, senderBalance - amount);
        double recipientBalance = getUserBalance(recipientId);
        boolean recipientUpdate = updateUserBalance(recipientId, recipientBalance + amount);

        return senderUpdate && recipientUpdate;
    }

    public static double getUserBalance(String userId) throws IOException {
        String endpoint = SUPABASE_URL + "/rest/v1/wallets?user_id=eq." + userId + "&select=balance";
        Request request = new Request.Builder()
                .url(endpoint)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Failed to get balance");
            String responseBody = response.body().string();
            List<Map<String, Object>> list = objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>() {});
            if (!list.isEmpty()) {
                Object bal = list.get(0).get("balance");
                if (bal != null) return Double.parseDouble(bal.toString());
            }
        }
        return 0.0;
    }

    public static boolean updateUserBalance(String userId, double newBalance) throws IOException {
        String endpoint = SUPABASE_URL + "/rest/v1/wallets?user_id=eq." + userId;
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("balance", newBalance);
        String jsonBody = objectMapper.writeValueAsString(updateMap);
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(endpoint)
                .patch(body)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    /**
     * INSERT data ke tabel "users".
     */
//    private static String insertUser(Map<String, Object> dataMap) throws IOException {
//        String endpoint = SUPABASE_URL + "/rest/v1/" + USERS_TABLE;
//
//        String jsonBody = objectMapper.writeValueAsString(dataMap);
//        RequestBody body = RequestBody.create(
//                jsonBody,
//                MediaType.parse("application/json")
//        );
//
//        Request request = new Request.Builder()
//                .url(endpoint)
//                .post(body)
//                .addHeader("apikey", SUPABASE_API_KEY)
//                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
//                .addHeader("Content-Type", "application/json")
//                .build();
//
//        try (Response response = httpClient.newCall(request).execute()) {
//            if (!response.isSuccessful()) {
//                throw new IOException("INSERT user failed: HTTP "
//                        + response.code() + " | " + response.message());
//            }
//            return response.body().string();
//        }
//    }
}
