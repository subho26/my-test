package com.n26.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;

import com.n26.model.Statistics;
import com.n26.model.Transaction;
import com.n26.repository.TransactionRepository;

/**
 * Utility class to process statistics based on transactions
 * 
 * @author Shubhendu
 *
 */
public final class StatisticsCalculatorUtil {

	/**
	 * Used to fetch Transactions in last 60 seconds
	 * 
	 * @param instant represents the given time instant
	 * @return {@link Statistics}
	 */
	public static Statistics getStatisticsData(final Instant instant, final TransactionRepository transactionRepository) {
		NavigableMap<Instant, List<Transaction>> subMap = transactionRepository.getTransactions().subMap(instant.minusSeconds(60), true, instant, true);

		List<Transaction> allTransactions = new ArrayList<>();
		subMap.values().forEach(allTransactions::addAll);

		long count = allTransactions.size();
		if (count == 0) {
			return new Statistics("0.00", "0.00", "0.00", "0.00", count);
		}

		BigDecimal max = new BigDecimal(0);
		Optional<BigDecimal> maxOptional = allTransactions.stream().map(a -> new BigDecimal(a.getAmount()))
																   .max(Comparator.naturalOrder());
		if (maxOptional.isPresent()) {
			max = maxOptional.get().setScale(2, BigDecimal.ROUND_HALF_UP);
		}

		BigDecimal min = new BigDecimal(0);
		Optional<BigDecimal> minOptional = allTransactions.stream().map(a -> new BigDecimal(a.getAmount()))
																   .min(Comparator.naturalOrder());
		if (minOptional.isPresent()) {
			min = minOptional.get().setScale(2, BigDecimal.ROUND_HALF_UP);
		}

		BigDecimal sum = new BigDecimal(0.0);

		for (Transaction transaction : allTransactions) {
			sum = sum.add(new BigDecimal(transaction.getAmount()));
		}

		BigDecimal avg = sum.divide(BigDecimal.valueOf(count), 2, BigDecimal.ROUND_HALF_UP);
		sum = sum.setScale(2, BigDecimal.ROUND_HALF_UP);

		return new Statistics(sum.toString(), avg.toString(), max.toString(), min.toString(), count);
	}

}
