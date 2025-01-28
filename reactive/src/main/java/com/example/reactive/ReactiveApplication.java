package com.example.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.reactive.StockPriceMonitor.Stock;

import reactor.core.scheduler.Schedulers;

@SpringBootApplication
public class ReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveApplication.class, args);
		
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

}
