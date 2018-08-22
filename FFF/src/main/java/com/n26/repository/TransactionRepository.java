package com.n26.repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.springframework.stereotype.Service;

import com.n26.model.Transaction;

/**
 * Repository class for all the transactions
 * 
 * @author Shubhendu
 *
 */
@Service
public class TransactionRepository {
	private final ConcurrentNavigableMap<Instant, List<Transaction>> transactions = new ConcurrentSkipListMap<>(Instant::compareTo);

	public ConcurrentNavigableMap<Instant, List<Transaction>> getTransactions() {
		return transactions;
	}

	/**
	 * Used to add new {@link Transaction} to the repository
	 * 
	 * @param tx represents the Transaction
	 * @param instant represents the time instant
	 */
	public void addTransaction(Transaction tx, Instant instant) {
		if (transactions.containsKey(instant))
			transactions.get(instant).add(tx);
		else {
			List<Transaction> txs = new ArrayList<>();
			txs.add(tx);
			transactions.put(instant, txs);
		}
	}

	/**
	 * Used to clear the repository
	 */
	public void deleteTransactions() {
		transactions.clear();
	}
}
