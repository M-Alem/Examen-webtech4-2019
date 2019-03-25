package edu.ap.spring.service;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import edu.ap.spring.transaction.Transaction;
import edu.ap.spring.transaction.TransactionInput;
import edu.ap.spring.transaction.TransactionOutput;
import net.minidev.json.JSONObject;

@Service
@Scope("singleton")
public class BlockChain {

	private ArrayList<Block> blocks = new ArrayList<Block>();
	private HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

	private int difficulty = 1;
	public float minimumTransaction = 0.1f;

	public BlockChain() {
	}

	public void setSecurity() {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); // setup Bouncey castle as a
																						// Security Provider
	}

	public HashMap<String, TransactionOutput> getUTXOs() {
		return this.UTXOs;
	}

	public ArrayList<Block> getBlocks() {
		return this.blocks;
	}

	public Boolean isChainValid(Transaction genesisTransaction) {
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>(); // a temporary working
																									// list of unspent
																									// transactions at a
																									// given block state
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

		// loop through blockchain to check hashes:
		for (int i = 1; i < blocks.size(); i++) {

			currentBlock = blocks.get(i);
			previousBlock = blocks.get(i - 1);
			// compare registered hash and calculated hash:
			if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
				System.out.println("# Current Hashes not equal");
				return false;
			}
			// compare previous hash and registered previous hash
			if (!previousBlock.hash.equals(currentBlock.previousHash)) {
				System.out.println("# Previous Hashes not equal");
				return false;
			}
			// check if hash is solved
			if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
				System.out.println("# This block hasn't been mined");
				return false;
			}
			// loop through blockchains transactions:
			TransactionOutput tempOutput;
			for (int t = 0; t < currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);

				if (!currentTransaction.verifySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is invalid");
					return false;
				}
				if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are not equal to outputs on Transaction(" + t + ")");
					return false;
				}

				for (TransactionInput input : currentTransaction.inputs) {
					tempOutput = tempUTXOs.get(input.transactionOutputId);

					if (tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is missing");
						return false;
					}

					if (input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is invalid");
						return false;
					}

					tempUTXOs.remove(input.transactionOutputId);
				}

				for (TransactionOutput output : currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}

				if (currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
					System.out.println("#Transaction(" + t + ") output recipient is not who it should be");
					return false;
				}
				if (currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
			}
		}
		System.out.println("Blockchain is valid");
		return true;
	}

	public void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		this.blocks.add(newBlock);
	}

	public String toJSON() {

		JSONObject jsonObj = new JSONObject();
		for (int i = 0; i < blocks.size(); i++) {

			Block currentBlock = blocks.get(i);
			JSONObject blockObj = new JSONObject();
			blockObj.put("hash", currentBlock.hash);
			blockObj.put("merkleRoot", currentBlock.merkleRoot);
			blockObj.put("nonce", currentBlock.nonce);
			blockObj.put("previousHash", currentBlock.previousHash);
			blockObj.put("timeStamp", currentBlock.timeStamp);
			JSONObject[] trs = new JSONObject[currentBlock.getTransactions().size()];
			int j = 0;
			for (Transaction t : currentBlock.getTransactions()) {
				JSONObject tr = new JSONObject();
				tr.put("recipient", t.recipient.toString());
				tr.put("sender", t.sender.toString());
				tr.put("transactionId", t.transactionId);
				tr.put("value", t.value);

				trs[j] = tr;
				j++;
			}

			blockObj.put("transactions", trs);
			jsonObj.put("block" + i, blockObj.toString());
		}

		return jsonObj.toJSONString();
	}

	public String getLastHash() {
		return this.blocks.get(this.blocks.size() - 1).hash;
	}
}
