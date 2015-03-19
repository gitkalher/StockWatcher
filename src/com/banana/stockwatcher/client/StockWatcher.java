package com.banana.stockwatcher.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class StockWatcher implements EntryPoint {

	private static final int REFRESH_INTERVAL = 5000;
	private static final int COLUMN_3 = 3;
	private static final int COLUMN_2 = 2;
	private static final int COLUMN_1 = 1;
	private static final int COLUMN_0 = 0;

	private VerticalPanel mainPanel = new VerticalPanel();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private FlexTable stockFlexiTable = new FlexTable();
	private TextBox newStockText = new TextBox();
	private Button addStockButton = new Button("Add");
	private Label lastUpdated = new Label();
	private List<String> stocks = new ArrayList<>();

	@Override
	public void onModuleLoad() {

		/* Widgets */

		stockFlexiTable.setText(0, COLUMN_0, "Symbol");
		stockFlexiTable.setText(0, COLUMN_1, "Price");
		stockFlexiTable.setText(0, COLUMN_2, "Change");
		stockFlexiTable.setText(0, COLUMN_3, "Remove");
		stockFlexiTable.setCellPadding(6);
		
		   // Add styles to elements in the stock list table.
		stockFlexiTable.getRowFormatter().addStyleName(0, "watchListHeader");
		stockFlexiTable.addStyleName("watchList");
		stockFlexiTable.getCellFormatter().addStyleName(0, COLUMN_1, "watchListNumericColumn");
		stockFlexiTable.getCellFormatter().addStyleName(0, COLUMN_2, "watchListNumericColumn");
		stockFlexiTable.getCellFormatter().addStyleName(0, COLUMN_3, "watchListRemoveColumn");
		 
		
		addPanel.add(newStockText);
		addPanel.add(addStockButton);
		addPanel.addStyleName("addPanel");
		
		mainPanel.add(stockFlexiTable);
		mainPanel.add(addPanel);
		mainPanel.add(lastUpdated);
		
		lastUpdated.setStyleName("lastUpdated");
		
		RootPanel.get("stockList").add(mainPanel);

		newStockText.setFocus(true);

		
		/* Refresh Timer */

		Timer refreshTimer = new Timer() {

			@Override
			public void run() {
				refreshWatchList();

			}
		};
		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);

		
		/* Events */

		addStockButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addStock();
			}
		});

		newStockText.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					addStock();
			}
		});

	}

	
	private void addStock() {

		final String symbol = newStockText.getText().toUpperCase().trim();
		newStockText.setFocus(true);

		if (!symbol.matches("^[0-9A-Z&#92;&#92;.]{1,10}$")) {
			Window.alert("'" + symbol + "' is not a valid symbol.");
			newStockText.selectAll();
			return;
		}

		if (stocks.contains(symbol)) {
			return;
		}

		int rowCount = stockFlexiTable.getRowCount();
		stocks.add(symbol);
		stockFlexiTable.setText(rowCount, COLUMN_0, symbol);
		stockFlexiTable.setWidget(rowCount, COLUMN_2, new Label());
		stockFlexiTable.getCellFormatter().addStyleName(rowCount, COLUMN_1, "watchListNumericColumn");
		stockFlexiTable.getCellFormatter().addStyleName(rowCount, COLUMN_2, "watchListNumericColumn");
		stockFlexiTable.getCellFormatter().addStyleName(rowCount, COLUMN_3, "watchListRemoveColumn");
		
		Image img = new Image("images/delete-icon.png");
		Button removeStockButton = new Button();
		removeStockButton.getElement().appendChild(img.getElement());
		removeStockButton.addStyleDependentName("remove");
		removeStockButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int indexOfStock = stocks.indexOf(symbol);
				stocks.remove(indexOfStock);
				stockFlexiTable.removeRow(++indexOfStock);
			}
		});

		stockFlexiTable.setWidget(rowCount, COLUMN_3, removeStockButton);
		newStockText.selectAll();

		refreshWatchList();
	}

	
	private void refreshWatchList() {
		System.out.println("Stock prices refreshed.");
		final double MAX_PRICE = 100.0; // $100.00
		final double MAX_PRICE_CHANGE = 0.02; // +/- 2%

		StockPrice[] prices = new StockPrice[stocks.size()];
		for (int i = 0; i < stocks.size(); i++) {
			double price = Random.nextDouble() * MAX_PRICE;
			double change = price * MAX_PRICE_CHANGE
					* (Random.nextDouble() * 2.0 - 1.0);

			prices[i] = new StockPrice(stocks.get(i), price, change);
		}

		updateTable(prices);
	}

	/**
     * Update the Price and Change fields all the rows in the stock table.
     *
     * @param prices Stock data for all rows.
     */
    private void updateTable(StockPrice[] prices) {
      for (int i = 0; i < prices.length; i++) {
        updateTable(prices[i]);
      }

      lastUpdated.setText("Last update : " + DateTimeFormat.getMediumDateTimeFormat().format(new Date()));

    }

	private void updateTable(StockPrice price) {
		
		// Make sure the stock is still in the stock table.
		if (!stocks.contains(price.getSymbol())) {
			return;
		}

		int rowCount = stocks.indexOf(price.getSymbol()) + 1;

		// Format the data in the Price and Change fields.
		String priceText = NumberFormat.getFormat("#,##0.00").format(price.getPrice());
		NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
		String changeText = changeFormat.format(price.getChange());
		String changePercentText = changeFormat.format(price.getChangePercent());

		// Populate the Price and Change fields with new data.
		stockFlexiTable.setText(rowCount, COLUMN_1, priceText);
	    Label changeWidget = (Label)stockFlexiTable.getWidget(rowCount, COLUMN_2);
	    changeWidget.setText(changeText + " (" + changePercentText + "%)");

	    // Change the color of text in the Change field based on its value.
	    String changeStyleName = "noChange";
	    if (price.getChangePercent() < -0.1f) {
	      changeStyleName = "negativeChange";
	    }
	    else if (price.getChangePercent() > 0.1f) {
	      changeStyleName = "positiveChange";
	    }

	    changeWidget.setStyleName(changeStyleName);
	}

}
