package edu.ap.spring.service;

import java.util.ArrayList;
import java.util.Date;

import edu.ap.spring.transaction.Transaction;

public class Block {

	public String hash;
	public String previousHash;
	public String merkleRoot;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); // our data will be a simple message.
	public long timeStamp; // as number of milliseconds since 1/1/1970.
	public int nonce;

	public Block() {
		this.setTimeStamp();
		this.calculateHash();
	}

	public void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}

	public void setTimeStamp() {
		this.timeStamp = new Date().getTime();
	}

	public ArrayList<Transaction> getTransactions() {
		return this.transactions;
	}

	// calculate new hash based on blocks contents
	public String calculateHash() {
		String calculatedhash = StringUtil
				.applySha256(previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + merkleRoot);
		this.hash = calculatedhash;

		return calculatedhash;
	}

	// increases nonce value until hash target is reached.
	public void mineBlock(int difficulty) {
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		String target = StringUtil.getDificultyString(difficulty); // create a string with difficulty * "0"
		while (!hash.substring(0, difficulty).equals(target)) {
			nonce++;
			hash = calculateHash();
		}
		// System.out.println("Block Mined : " + hash);
	}

	// add transactions to this block
	public boolean addTransaction(Transaction transaction, BlockChain bChain) {
		// process transaction and check if valid, unless block is genesis block then
		// ignore.
		if (transaction == null)
			return false;
		if ((previousHash != "0")) {
			if ((transaction.processTransaction(bChain) != true)) {
				System.out.println("# Transaction failed to process. Discarded.");
				return false;
			}
		}

		transactions.add(transaction);
		// System.out.println("Transaction successfully added to Block");
		return true;
	}

	public void clearTransactions() {
		this.transactions.clear();
	}
}
