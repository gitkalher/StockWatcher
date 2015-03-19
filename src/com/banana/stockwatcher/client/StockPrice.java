package com.banana.stockwatcher.client;

public class StockPrice {

	private String symbol;
	private double price;
	private double change;

	public StockPrice() {
		super();
	}

	public StockPrice(String symbol, double price, double change) {
		super();
		this.symbol = symbol;
		this.price = price;
		this.change = change;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getChange() {
		return change;
	}

	public void setChange(double change) {
		this.change = change;
	}

	public double getChangePercent() {
		return 100.0 * this.change / this.price;
	}
}
