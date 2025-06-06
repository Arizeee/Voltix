package dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static config.DatabaseConf.SUPABASE_API_KEY;
import static config.DatabaseConf.SUPABASE_URL;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * DashboardData.java
 *
 * Contoh mengambil semua data wallet berdasarkan userId yang sudah diset di
 * objek DashboardData melalui konstruktor atau method UserId(...).
 */
public class DashboardData {

    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Field untuk menyimpan userId (UUID) setelah login - harus public agar bisa diakses dari Dashboard.form
    public String IdUser;
    public Double balance;
    public String id, address, createdAt;

    /**
     * Konstruktor default (tanpa parameter)
     */
    public DashboardData() {
        // Konstruktor kosong untuk backward compatibility
    }

    /**
     * Konstruktor dengan parameter userId
     *
     * @param userId - ID user yang akan digunakan untuk mengambil data wallet
     */
    public DashboardData(String userId) {
        this.IdUser = userId;
    }

    /**
     * Simpan userId (misalnya dari getUserByPrivateKey) ke field ini.
     */
    public void setUserId(String val) {
        this.IdUser = val;
    }

    /**
     * Return userId yang sudah disimpan (bisa null jika belum diset).
     */
    public String getUserId() {
        return this.IdUser;
    }

    public void setBalance(Double val) {
        this.balance = val;
    }

    public Double getBalance() {
        return this.balance;
    }

    /**
     * Instance method untuk mengambil data wallets dimana user_id =
     * this.IdUser.
     *
     * @throws IOException
     */
    public void getWallets() throws IOException {
        // Pastikan IdUser tidak null atau kosong
        if (this.IdUser == null || this.IdUser.isEmpty()) {
            System.err.println("Error: userId belum diset. Panggil dulu UserId(...) atau gunakan konstruktor dengan parameter.");
            return;
        }

        // Endpoint Supabase untuk tabel "wallets" dengan filter user_id=eq.<IdUser>
        String endpoint = SUPABASE_URL
                + "/rest/v1/wallets"
                + "?user_id=eq." + this.IdUser
                + "&select=*";

        Request request = new Request.Builder()
                .url(endpoint)
                .get()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("GET wallets failed: HTTP "
                        + response.code()
                        + " | "
                        + response.message());
            }

            String responseBody = response.body().string();
            // Parse responseBody menjadi JsonNode (harap berupa array JSON)
            JsonNode jsonArray = objectMapper.readTree(responseBody);

            if (!jsonArray.isArray() || jsonArray.size() == 0) {
                System.out.println("Tidak ada wallet untuk user_id = " + this.IdUser);
                return;
            }

            // Looping setiap elemen (objek) dalam array
            for (JsonNode wallet : jsonArray) {
                id = wallet.get("id").asText();
                address = wallet.get("wallet_address").asText();
                Double balget = wallet.get("balance").asDouble();
                setBalance(balget);
                createdAt = wallet.get("created_at").asText();

                System.out.println(
                        "Wallet ID    : " + id + "\n"
                        + "User ID      : " + IdUser + "\n"
                        + "Address      : " + address + "\n"
                        + "Balance      : " + balance + "\n"
                        + "Created At   : " + createdAt + "\n"
                        + "--------------------------------------"
                );
            }
        }
    }

    /**
     * Contoh penggunaan sesuai dengan pattern dari kode Anda
     */
//    public static void main(String[] args) {
//        try {
//            // Simulasi seperti di kode Anda
//            String keyStoreText = "some_private_key_here";
//
//            // Simulasi loginWithPrivateKey dan getUserByPrivateKey
//            String idData = getUserByPrivateKey(keyStoreText); // Ini menghasilkan userId
//
//            // Langsung gunakan di konstruktor seperti di kode Anda
//            DashboardData dfd = new DashboardData(idData);
//
//            // Panggil method yang dibutuhkan
//            dfd.getWallets(); // Atau bisa langsung setVisible(true) jika GUI
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            System.err.println("Gagal mengambil data wallet: " + ex.getMessage());
//        }
//    }

    /**
     * Dummy method untuk simulasi - ganti dengan method asli Anda
     */
//    private static String getUserByPrivateKey(String privateKey) {
//        // Simulasi return userId
//        return "4c1583b4-9005-4b2b-afd8-b21de76c63df";
//    }
}
