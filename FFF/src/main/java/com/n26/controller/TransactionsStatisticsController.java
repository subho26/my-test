package com.n26.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.n26.model.Statistics;
import com.n26.model.Transaction;
import com.n26.repository.TransactionRepository;
import com.n26.util.StatisticsCalculatorUtil;

/**
 * The controller class for transaction related requests
 * 
 * @author Shubhendu
 *
 */
@RestController
@RequestMapping("/")
public class TransactionsStatisticsController {
	
	@Autowired
	TransactionRepository transactionRepository;

	@RequestMapping(value = "transactions", method = RequestMethod.POST)
	public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {

		try {
			new BigDecimal(transaction.getAmount());
			Instant.parse(transaction.getTimestamp());
		} catch (NumberFormatException | DateTimeParseException | NullPointerException ex) {
			return new ResponseEntity<Transaction>(HttpStatus.UNPROCESSABLE_ENTITY);
		}

		Instant transactionInstant = Instant.parse(transaction.getTimestamp());

		if (transactionInstant.isBefore(Instant.now().minusSeconds(60))) {
			return new ResponseEntity<Transaction>(HttpStatus.NO_CONTENT);
		}

		if (transactionInstant.isAfter(Instant.now())) {
			return new ResponseEntity<Transaction>(HttpStatus.UNPROCESSABLE_ENTITY);
		}

		transactionRepository.addTransaction(transaction, Instant.parse(transaction.getTimestamp()));
		return new ResponseEntity<Transaction>(HttpStatus.CREATED);
	}

	@RequestMapping(value = "statistics", method = RequestMethod.GET)
	public Statistics getStatistics() {
		return StatisticsCalculatorUtil.getStatisticsData(Instant.now(), transactionRepository);
	}

	@RequestMapping(value = "transactions", method = RequestMethod.DELETE)
	public ResponseEntity<Transaction> deleteTransaction() {
		transactionRepository.deleteTransactions();
		return new ResponseEntity<Transaction>(HttpStatus.NO_CONTENT);
	}
}