package edu.ap.spring.transaction;

import java.security.*;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

import edu.ap.spring.service.BlockChain;
import edu.ap.spring.service.StringUtil;

@Component
public class Transaction {

	public String transactionId; // contains a hash of transaction
	public PublicKey sender; // senders address/public key.
	public PublicKey recipient; // recipients address/public key.
	public float value; // contains the amount we wish to send to the recipient.
	public byte[] signature; // this is to prevent anybody else from spending funds in our wallet.

	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

	private static int sequence = 0;

	public Transaction() {
	}

	public Transaction(PublicKey sender, PublicKey recipient, float value) {
		this.sender = sender;
		this.recipient = recipient;
		this.value = value;
		this.transactionId = StringUtil.applySha256(sender.toString() + recipient.toString() + value);
	}

	public void setSender(PublicKey sender) {
		this.sender = sender;
	}

	public PublicKey getSender() {
		return this.sender;
	}

	public void setRecipient(PublicKey recipient) {
		this.recipient = recipient;
	}

	public PublicKey getRecipient() {
		return this.recipient;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public float getValue() {
		return this.value;
	}

	public void setInputs(ArrayList<TransactionInput> inputs) {
		this.inputs = inputs;
	}

	public ArrayList<TransactionInput> getInputs() {
		return this.inputs;
	}

	public boolean processTransaction(BlockChain bChain) {

		if (verifySignature() == false) {
			System.out.println("# Transaction Signature failed to verify");
			return false;
		}

		// gathers transaction inputs (Making sure they are unspent):
		for (TransactionInput i : inputs) {
			i.UTXO = bChain.getUTXOs().get(i.transactionOutputId);
		}

		// checks if transaction is valid:
		if (getInputsValue() < bChain.minimumTransaction) {
			System.out.println("Transaction Inputs too small: " + getInputsValue());
			return false;
		}

		// generate transaction outputs:
		float leftOver = getInputsValue() - value; // get value of inputs then the left over change:
		transactionId = calulateHash();
		outputs.add(new TransactionOutput(this.recipient, value, transactionId)); // send value to recipient
		outputs.add(new TransactionOutput(this.sender, leftOver, transactionId)); // send the left over 'change' back to
																					// sender

		// add outputs to Unspent list
		for (TransactionOutput o : outputs) {
			bChain.getUTXOs().put(o.id, o);
		}

		// remove transaction inputs from UTXO lists as spent:
		for (TransactionInput i : inputs) {
			if (i.UTXO == null)
				continue; // if Transaction can't be found skip it
			bChain.getUTXOs().remove(i.UTXO.id);
		}
		return true;
	}

	public float getInputsValue() {
		float total = 0;
		for (TransactionInput i : inputs) {
			if (i.UTXO == null)
				continue; // if Transaction can't be found skip it, This behavior may not be optimal.
			total += i.UTXO.value;
		}
		return total;
	}

	public float getOutputsValue() {
		float total = 0;
		for (TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}

	private String calulateHash() {
		sequence++; // increase the sequence to avoid 2 identical transactions having the same hash
		return StringUtil.applySha256(StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient)
				+ Float.toString(value) + sequence);
	}

	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient)
				+ Float.toString(value);
		signature = StringUtil.applyECDSASig(privateKey, data);
	}

	public boolean verifySignature() {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient)
				+ Float.toString(value);
		return StringUtil.verifyECDSASig(sender, data, signature);
	}
}
