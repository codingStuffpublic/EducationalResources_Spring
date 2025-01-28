package com.example.reactive;


import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Random;

public class StockPriceMonitor {

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

    record Stock(String symbol, double price) {}
}
