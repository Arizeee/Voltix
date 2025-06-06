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
