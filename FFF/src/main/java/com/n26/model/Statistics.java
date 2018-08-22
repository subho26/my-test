package com.n26.model;

/**
 * The POJO class for statistics data to be sent back to the client
 * 
 * @author Shubhendu
 *
 */
public class Statistics {
	private String sum;
	private String avg;
	private String max;
	private String min;
	private long count;

	public Statistics(String sum, String avg, String max, String min, long count) {
		super();
		this.sum = sum;
		this.avg = avg;
		this.max = max;
		this.min = min;
		this.count = count;
	}

	public String getSum() {
		return sum;
	}

	public void setSum(String sum) {
		this.sum = sum;
	}

	public String getAvg() {
		return avg;
	}

	public void setAvg(String avg) {
		this.avg = avg;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public Statistics() {
	}
}