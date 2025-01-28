package com.example.reactive;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Random;

public class TrafficMonitoringSystem {

    public static void startTrafficMonitoring() {
    	
        Flux<TrafficData> trafficDataStream = generateTrafficData();

        trafficDataStream
            // Process traffic data (e.g., check for congestion)
            .groupBy(TrafficData::lane) // Group data by lane
            .flatMap(laneFlux -> laneFlux
                .buffer(Duration.ofSeconds(3)) // Collect data every 3 seconds
                .map(TrafficMonitoringSystem::analyzeCongestion) // Analyze congestion
            )
            .filter(trafficAlert -> trafficAlert.congestionLevel() > 70) // Alert if congestion level > 80%
            .doOnNext(alert -> System.out.printf(
                "ALERT: Heavy congestion detected on Lane %d: Congestion Level = %d%%%n",
                alert.lane(), alert.congestionLevel()
            ))
            .subscribeOn(Schedulers.parallel())
            .subscribe();

        // Keep the system running for a while to simulate real-time processing
        try {
            Thread.sleep(10000); // Simulate 15 seconds of monitoring
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Flux<TrafficData> generateTrafficData() {
        Random random = new Random();
        return Flux.interval(Duration.ofMillis(500)) // Emit data every 500ms
                   .map(i -> new TrafficData(
                           random.nextInt(5), // Lane ID (0-4)
                           20 + random.nextInt(80), // Vehicle count (20-100)
                           random.nextInt(120) // Average speed (0-120 km/h)
                   ));
    }

    // Analyze congestion based on traffic data
    private static TrafficAlert analyzeCongestion(java.util.List<TrafficData> dataBuffer) {
        int lane = dataBuffer.get(0).lane(); // All data in buffer belongs to the same lane
        double avgSpeed = dataBuffer.stream().mapToInt(TrafficData::avgSpeed).average().orElse(0);
        int vehicleCount = dataBuffer.stream().mapToInt(TrafficData::vehicleCount).sum();

        // Congestion level = based on vehicle density and low speeds
        int congestionLevel = (int) ((vehicleCount / 100.0) * (100 - avgSpeed) * 0.8);
        return new TrafficAlert(lane, congestionLevel);
    }

}

record TrafficData(int lane, int vehicleCount, int avgSpeed) {}

record TrafficAlert(int lane, int congestionLevel) {}
