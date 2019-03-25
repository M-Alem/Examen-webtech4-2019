package edu.ap.spring.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.assertEquals;

import java.io.PrintStream;

import org.junit.*;
import org.junit.runners.MethodSorters;

import edu.ap.spring.service.*;
import edu.ap.spring.transaction.Transaction;
import edu.ap.spring.transaction.TransactionOutput;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpringTest1 {

	@Autowired
	private BlockChain bChain;
	@Autowired
	private Wallet coinbase, walletA, walletB;
	@Autowired
	private Block genesis, block1;
	@Autowired
	private Transaction genesisTransaction;;

	PrintStream console;

	@Before
	public void init() {
		bChain.setSecurity();
		coinbase.generateKeyPair();
		walletA.generateKeyPair();
		walletB.generateKeyPair();

		// create genesis transaction, which sends 100 coins to walletA:
		genesisTransaction.setSender(coinbase.publicKey);
		genesisTransaction.setRecipient(walletA.publicKey);
		genesisTransaction.setValue(100f);
		genesisTransaction.setInputs(null);
		genesisTransaction.generateSignature(coinbase.privateKey); // manually sign the genesis transaction
		genesisTransaction.transactionId = "0"; // manually set the transaction id
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value,
				genesisTransaction.transactionId)); // manually add the Transactions Output
		bChain.getUTXOs().put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); // it's
																										// important to
																										// store our
																										// first
																										// transaction
																										// in the UTXOs
																										// list.
		// creating and Mining Genesis block
		genesis.setPreviousHash("0");
		genesis.setTimeStamp();
		genesis.calculateHash();
		genesis.addTransaction(genesisTransaction, bChain);
		bChain.addBlock(genesis);
	}

	@After
	public void after() {
		System.setOut(console);
	}

	@Test
	public void transaction1() {
		Block block = new Block();
		block.setPreviousHash(bChain.getLastHash());

		try {
			block.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 40f), bChain);
		} catch (Exception e) {
		}

		bChain.addBlock(block);

		assertEquals(60f, walletA.getBalance(), 0);
		assertEquals(40f, walletB.getBalance(), 0);
	}
}
