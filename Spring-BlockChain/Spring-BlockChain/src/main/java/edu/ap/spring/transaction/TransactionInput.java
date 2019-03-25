package edu.ap.spring.transaction;

public class TransactionInput {
    public String transactionOutputId; // reference to TransactionOutputs -> transactionId
    public TransactionOutput UTXO; // contains the Unspent transaction output

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}