package edu.ap.spring.transaction;

import java.security.PublicKey;
import edu.ap.spring.service.StringUtil;

public class TransactionOutput {

    public String id;
    public PublicKey recipient; // the new owner of these coins.
    public float value; // the amount of coins they own
    public String parentTransactionId; // the id of the transaction this output was created in

    public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil
                .applySha256(StringUtil.getStringFromKey(recipient) + Float.toString(value) + parentTransactionId);
    }

    // check if coin belongs to you
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient);
    }
}