/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dashboard;

import config.DatabaseConf;

/**
 *
 * @author ariqa
 */
public class SendAction {

    private String userId;

//    public SendAction() {
//
//    }

    public SendAction(String userId) {
        this.userId = userId;
    }
    
    public String transfer(String senderAddress, String receiverAddress, double amount) {
        // 1. Validasi input & saldo (harus kamu tambahkan sendiri)
        // 2. Update saldo wallet pengirim dan penerima (harus kamu tambahkan sendiri)

        // 3. Insert transaction ke Supabase
        String txType = "transfer";
        String blockHash = "";
        String trxId = TransactionService.insertTransaction(
            senderAddress, receiverAddress, amount, txType, blockHash
        );
        return trxId;
    }
}
