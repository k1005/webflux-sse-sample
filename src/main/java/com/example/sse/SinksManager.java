package com.example.sse;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SampleChannel {

    private final Map<String, Sinks.Many<ServerSentEvent<?>>> map;

    public SampleChannel() {
        this.map = new ConcurrentHashMap<>();
    }

    public Sinks.Many<ServerSentEvent<?>> getSink(String key) {
        if (!map.containsKey(key)) {
            map.put(key, Sinks.many().multicast().directAllOrNothing());
        }

        return map.get(key);
    }

    public Flux<ServerSentEvent<?>> asFlux(String key) {
        return this.getSink(key).asFlux().startWith(
                ServerSentEvent.builder()
                        .comment("start sse")
                        .build()
        );
    }

    @Scheduled(fixedDelay = 20000)
    public void sendTest() {
        map.forEach((key, value) -> {
            value.tryEmitNext(
                    ServerSentEvent.builder()
                            .comment("alive-ping")
                            .build()
            );
        });
    }
}
