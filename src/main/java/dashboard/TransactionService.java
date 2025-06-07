/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dashboard;

import config.DatabaseConf;
import java.net.HttpURLConnection;
import java.net.*;
import java.io.*;
import com.fasterxml.jackson.databind.*;

/**
 *
 * @author moham
 */
public class TransactionService {
     /**
     * Insert transaksi ke tabel transactions pada Supabase.
     * @param senderAddress Alamat wallet pengirim
     * @param receiverAddress Alamat wallet penerima
     * @param amount Jumlah transfer
     * @param txType Tipe transaksi (misal: "transfer")
     * @param blockHash Hash blok (jika ada, boleh kosong)
     * @return true jika sukses, false jika gagal
     */
     public static String insertTransaction(
        String senderAddress,
        String receiverAddress,
        double amount,
        String txType,
        String blockHash
    ) {
        try {
            URL url = new URL(DatabaseConf.SUPABASE_URL + "/rest/v1/" + DatabaseConf.TRANSACTIONS_TABLE);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", DatabaseConf.SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + DatabaseConf.SUPABASE_API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=representation");
            conn.setDoOutput(true);

            String json = String.format(
                "{\"sender_address\":\"%s\",\"receiver_address\":\"%s\",\"amount\":%s,\"tx_type\":\"%s\",\"block_hash\":\"%s\"}",
                senderAddress, receiverAddress, Double.toString(amount), txType, blockHash
            );
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                        code >= 400 ? conn.getErrorStream() : conn.getInputStream()
                    ))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
            }

            if (code == 201 || code == 200) {
                // Ambil ID transaksi dari response Supabase (hasilnya array of object)
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode arr = mapper.readTree(responseBuilder.toString());
                String trxId = arr.get(0).get("id").asText();
                return trxId;
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Fungsi statis untuk mendapatkan address dari username
    public static String getAddressByUsername(String username) {
        try {
            // Pertama cari user_id dari username
            String endpointUser = DatabaseConf.SUPABASE_URL
                + "/rest/v1/users?username=eq." + username + "&select=id";
            HttpURLConnection connUser = (HttpURLConnection) new URL(endpointUser).openConnection();
            connUser.setRequestProperty("apikey", DatabaseConf.SUPABASE_API_KEY);
            connUser.setRequestProperty("Authorization", "Bearer " + DatabaseConf.SUPABASE_API_KEY);

            BufferedReader readerUser = new BufferedReader(new InputStreamReader(connUser.getInputStream()));
            String responseUser = readerUser.readLine();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode arrayUser = objectMapper.readTree(responseUser);

            if (!arrayUser.isArray() || arrayUser.size() == 0) return null;
            String userId = arrayUser.get(0).get("id").asText();

            // Kedua cari wallet_address dari user_id
            String endpointWallet = DatabaseConf.SUPABASE_URL
                + "/rest/v1/wallets?user_id=eq." + userId + "&select=wallet_address";
            HttpURLConnection connWallet = (HttpURLConnection) new URL(endpointWallet).openConnection();
            connWallet.setRequestProperty("apikey", DatabaseConf.SUPABASE_API_KEY);
            connWallet.setRequestProperty("Authorization", "Bearer " + DatabaseConf.SUPABASE_API_KEY);

            BufferedReader readerWallet = new BufferedReader(new InputStreamReader(connWallet.getInputStream()));
            String responseWallet = readerWallet.readLine();

            JsonNode arrayWallet = objectMapper.readTree(responseWallet);
            if (!arrayWallet.isArray() || arrayWallet.size() == 0) return null;
            return arrayWallet.get(0).get("wallet_address").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Fungsi untuk mendapatkan wallet address berdasarkan userId dari Supabase
    public static String getAddressByUserId(String userId) {
        try {
            String endpoint = DatabaseConf.SUPABASE_URL
                + "/rest/v1/wallets?user_id=eq." + userId + "&select=wallet_address";
            HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestProperty("apikey", DatabaseConf.SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + DatabaseConf.SUPABASE_API_KEY);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("HTTP error: " + responseCode);
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            String response = responseBuilder.toString();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode array = objectMapper.readTree(response);
            if (!array.isArray() || array.size() == 0) return null;
            return array.get(0).get("wallet_address").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String getTransactionJsonById(String transactionId) {
        try {
            String endpoint = DatabaseConf.SUPABASE_URL + "/rest/v1/transactions?id=eq." + transactionId;
            HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestProperty("apikey", DatabaseConf.SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + DatabaseConf.SUPABASE_API_KEY);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.readLine();
            if (response == null || response.length() < 5) return null;
            return response.substring(1, response.length() - 1); // hapus array [ ... ]
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
