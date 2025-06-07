/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dashboard;

import config.DatabaseConf;
import util.HashUtil;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.time.Instant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

/**
 *
 * @author moham
 */
public class BlockService {
    public static String createBlockForTransaction(String transactionId, String transactionJsonData) {
        try {
            // 1. Ambil previous_hash (block_hash block terakhir)
            String previousHash = getLatestBlockHash();
            if (previousHash == null) previousHash = "GENESIS";

            // 2. Buat transactions array JSON (meskipun cuma 1, pakai array)
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode txArray = objectMapper.createArrayNode();
            txArray.add(objectMapper.readTree(transactionJsonData));
            String transactionsJson = objectMapper.writeValueAsString(txArray);

            // 3. Nonce (bisa 0 untuk off-chain)
            int nonce = 0;

            // 4. Buat hash block
            String blockContent = previousHash + transactionsJson + nonce + Instant.now().toString();
            String blockHash = HashUtil.sha256(blockContent);

            // 5. Insert block ke tabel blocks
            String blockInsertJson = String.format(
                "{\"block_hash\":\"%s\",\"previous_hash\":\"%s\",\"transactions\":%s,\"nonce\":%d,\"created_at\":\"%s\"}",
                blockHash, previousHash, transactionsJson, nonce, Instant.now().toString()
            );

            URL url = new URL(DatabaseConf.SUPABASE_URL + "/rest/v1/blocks");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", DatabaseConf.SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + DatabaseConf.SUPABASE_API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=representation");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = blockInsertJson.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            int code = conn.getResponseCode();

            // 6. Jika sukses, update transaksi block_hash
            if (code == 201 || code == 200) {
                updateTransactionBlockHash(transactionId, blockHash);
                return blockHash;
            } else {
                System.err.println("Block insert failed, code: " + code);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Mendapatkan block_hash dari block terakhir (block dengan id terbesar)
    public static String getLatestBlockHash() {
        try {
            URL url = new URL(DatabaseConf.SUPABASE_URL + "/rest/v1/blocks?select=block_hash&id=order.desc&limit=1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("apikey", DatabaseConf.SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + DatabaseConf.SUPABASE_API_KEY);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.readLine();
            if (response == null || response.length() < 5) return null;
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode array = (ArrayNode) objectMapper.readTree(response);
            if (array.size() == 0) return null;
            return array.get(0).get("block_hash").asText();
        } catch (Exception e) {
            return null;
        }
    }

    // Update field block_hash pada transaksi
    public static void updateTransactionBlockHash(String transactionId, String blockHash) {
        OkHttpClient client = new OkHttpClient();
        String url = DatabaseConf.SUPABASE_URL + "/rest/v1/transactions?id=eq." + transactionId;
        String json = String.format("{\"block_hash\":\"%s\"}", blockHash);

        RequestBody body = RequestBody.create(
            json, MediaType.parse("application/json")
        );
        Request request = new Request.Builder()
            .url(url)
            .patch(body)
            .addHeader("apikey", DatabaseConf.SUPABASE_API_KEY)
            .addHeader("Authorization", "Bearer " + DatabaseConf.SUPABASE_API_KEY)
            .addHeader("Content-Type", "application/json")
            .build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println("PATCH response: " + response.code() + " / " + response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
