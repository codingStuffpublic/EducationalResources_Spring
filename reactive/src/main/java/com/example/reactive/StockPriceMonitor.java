package com.example.reactive;

import java.time.Duration;
import java.util.Random;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class StockPriceMonitor {

	public static void startStockPriceMonitoring() {
		StockPriceMonitor.stockPriceStream()
	    .filter(stock -> stock.symbol().equals("AAPL"))  // Filter for Apple stocks
	    .doOnNext(stock -> System.out.printf("Received update: %s - $%.2f%n", stock.symbol(), stock.price()))
	    
	    // Calculate a running average price for trend analysis
	    .buffer(5)  // Group every 5 price updates
	    .map(prices -> prices.stream().mapToDouble(Stock::price).average().orElse(0))
	    .filter(avg -> avg > 120)  // Only alert if the average price is above $120
	    .doOnNext(avg -> System.out.printf("ALERT: AAPL's average price exceeded $120: $%.2f%n", avg))
	    .subscribeOn(Schedulers.parallel())  // Run asynchronously
	    .subscribe();  

		// Keep the application running to see the stream
		try {
		    Thread.sleep(10000);  // Simulate a running system
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	}
	
    // Simulating a stream of stock prices
    public static Flux<Stock> stockPriceStream() {
        Random random = new Random();
        String[] stockSymbols = {"AAPL", "GOOGL", "MSFT", "AMZN", "TSLA"};

        return Flux.interval(Duration.ofMillis(500)) // Emit every 500ms
                   .map(i -> new Stock(
                           stockSymbols[random.nextInt(stockSymbols.length)], 
                           100 + random.nextDouble() * 50  // Simulated stock price
                   ));
    }

}

record Stock(String symbol, double price) {}