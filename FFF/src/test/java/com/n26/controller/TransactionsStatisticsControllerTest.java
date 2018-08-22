package com.n26.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.n26.model.Transaction;
import com.n26.repository.TransactionRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(TransactionsStatisticsController.class)
public class TransactionsStatisticsControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TransactionRepository transactionRepository;

	private Instant instant1 = Instant.now();
	private Instant instant2 = Instant.now().minusSeconds(10);
	private Instant instant3 = Instant.now().minusSeconds(900);

	private Transaction tx1;
	private Transaction tx2;
	private Transaction tx3;
	private Transaction tx4;
	private Transaction tx5;
	private Transaction tx6;

	ConcurrentNavigableMap<Instant, List<Transaction>> mockTransactions = new ConcurrentSkipListMap<>(Instant::compareTo);

	@Before
	public void onSetup() {
		String stringInstant1 = instant1.toString();
		tx1 = new Transaction("12", stringInstant1);
		tx2 = new Transaction("14", stringInstant1);

		String stringInstant2 = instant2.toString();
		tx3 = new Transaction("14", stringInstant2);
		tx4 = new Transaction("16", stringInstant2);

		String stringInstant3 = instant3.toString();
		tx5 = new Transaction("16", stringInstant3);
		tx6 = new Transaction("18", stringInstant3);
	}

	/**
	 * Test to check whether Transactions within 60 seconds are returned(positive
	 * scenario)
	 */
	@Test
	public void testGetStatisticsWithinSixtySeconds() throws Exception {

		mockTransactions.put(instant1, Arrays.asList(tx1, tx2));
		mockTransactions.put(instant2, Arrays.asList(tx3, tx4));

		Mockito.when(transactionRepository.getTransactions()).thenReturn(mockTransactions);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/statistics").accept(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"sum\": \"56.00\",\"avg\": \"14.00\",\"max\": \"16.00\",\"min\": \"12.00\",\"count\": 4}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
	}

	/**
	 * Test to check whether Transactions within 60 seconds are returned(negative
	 * scenario)
	 */
	@Test
	public void testGetStatisticsBeforeSixtySeconds() throws Exception {
		mockTransactions.put(instant1, Arrays.asList(tx1, tx2));
		mockTransactions.put(instant3, Arrays.asList(tx5, tx6));

		Mockito.when(transactionRepository.getTransactions()).thenReturn(mockTransactions);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/statistics").accept(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		String expected = "{\"sum\": \"26.00\",\"avg\": \"13.00\",\"max\": \"14.00\",\"min\": \"12.00\",\"count\": 2}";
		String notExpected = "{\"sum\": \"60.00\",\"avg\": \"15.00\",\"max\": \"18.00\",\"min\": \"12.00\",\"count\": 4}";

		JSONAssert.assertEquals(expected, result.getResponse().getContentAsString(), false);
		JSONAssert.assertNotEquals(notExpected, result.getResponse().getContentAsString(), false);
	}
	
	/**
	 * Test to check deletion of all transactions. Expecting a status code of 204
	 */
	@Test
	public void testDeleteTransaction() throws Exception {
		mockTransactions.put(instant1, Arrays.asList(tx1, tx2));
		mockTransactions.put(instant3, Arrays.asList(tx5, tx6));

		Mockito.when(transactionRepository.getTransactions()).thenReturn(mockTransactions);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/transactions");

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		assertEquals("Invalid response code ", result.getResponse().getStatus(), HttpStatus.NO_CONTENT.value());
	}
	
	/**
	 * Test to check the POST request
	 */
	@Test
	public void testCreateTransaction() throws Exception {
		String transactionString = "{\"amount\": \"30\",\"timestamp\": \"" + Instant.now().minusSeconds(10).toString() + "\"}";
		mockMvc.perform(post("/transactions").content(transactionString).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());

		transactionString = "{\"amount\": \"30\",\"timestamp\": " + Instant.now().minusSeconds(10).toString() + "}";
		mockMvc.perform(post("/transactions").content(transactionString).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

		transactionString = "{\"amount\": \"30\",\"timestamp\": \"" + Instant.now().minusSeconds(100).toString()
				+ "\"}";
		mockMvc.perform(post("/transactions").content(transactionString).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());

		transactionString = "{\"amount\": \"30\",\"timestamp\": \"" + Instant.now().plusSeconds(100).toString() + "\"}";
		mockMvc.perform(post("/transactions").content(transactionString).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity());
	}

}