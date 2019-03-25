package edu.ap.spring.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.assertEquals;

import org.junit.*;
import org.junit.runners.MethodSorters;

import edu.ap.spring.service.*;
import edu.ap.spring.transaction.Transaction;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpringTest1 {

	@Autowired
	private BlockChain bChain;
	@Autowired
	private Wallet coinbase, walletA, walletB;
	private Transaction genesisTransaction;

	@Before
	public void init() {
		bChain.setSecurity();
		coinbase.generateKeyPair();
		walletA.generateKeyPair();
		walletB.generateKeyPair();

		//create genesis transaction, which sends 100 coins to walletA:
		genesisTransaction = new Transaction(coinbase.getPublicKey(), walletA.getPublicKey(), 100f);
		genesisTransaction.generateSignature(coinbase.getPrivateKey());	 // manually sign the genesis transaction	
		genesisTransaction.transactionId = "0"; // manually set the transaction id
						
		//creating and Mining Genesis block
		Block genesis = new Block();
		genesis.setPreviousHash("0");
		genesis.addTransaction(genesisTransaction, bChain);
		bChain.addBlock(genesis);
	}
	
	@After
	public void after() {
		// cleanup
 	}

	@Test
	public void transaction1() {
		Block block = new Block();
		block.setPreviousHash(bChain.getLastHash());
			
		try {
			block.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 40f), bChain);
		} 
		catch(Exception e) {}
		
		bChain.addBlock(block);
		
		assertEquals(60f, walletA.getBalance(), 0);
		assertEquals(40f, walletB.getBalance(), 0);
	}
}
