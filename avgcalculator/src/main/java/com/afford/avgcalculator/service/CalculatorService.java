package com.afford.avgcalculator.service;

import com.afford.avgcalculator.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalculatorService {

    private final WebClient.Builder webClientBuilder;
    private static final String BASE_URL = "http://20.244.56.144/evaluation-service";
    private final Map<String, LinkedList<Integer>> numberWindows = new ConcurrentHashMap<>();

    public ResponseDto calculateAvg(String numberId) {
        String apiUrl = createUrl(numberId);
        LinkedList<Integer> window = numberWindows.computeIfAbsent(numberId, k -> new LinkedList<>());
        List<Integer> prevState = new ArrayList<>(window);

        List<Integer> newNumbers = fetchNumbers(apiUrl);
        if (!newNumbers.isEmpty()) {
            updateWindow(window, newNumbers);
        }

        double avg = window.isEmpty() ? 0 : window.stream().mapToDouble(i -> i).average().orElse(0);

        ResponseDto response = new ResponseDto();
        response.setWindowPrevState(prevState);
        response.setWindowCurrState(new ArrayList<>(window));
        response.setNumbers(newNumbers);
        response.setAvg((float) avg);

        return response;
    }

    private List<Integer> fetchNumbers(String url) {
        WebClient client = webClientBuilder.build();
        try {
            return client.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(500))
                    .map(response -> (List<Integer>) response.get("numbers"))
                    .onErrorReturn(Collections.emptyList())
                    .block();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private void updateWindow(LinkedList<Integer> window, List<Integer> newNumbers) {
        Set<Integer> uniqueNumbers = new LinkedHashSet<>(window);
        for (Integer num : newNumbers) {
            if (uniqueNumbers.add(num)) {
                if (uniqueNumbers.size() > 10) {
                    uniqueNumbers.remove(window.getFirst());
                    window.removeFirst();
                }
                window.add(num);
            }
        }
    }

    private String createUrl(String numberId) {
        return switch (numberId) {
            case "p" -> BASE_URL + "/primes";
            case "f" -> BASE_URL + "/fibo";
            case "e" -> BASE_URL + "/even";
            case "r" -> BASE_URL + "/random";
            default -> throw new IllegalArgumentException("Invalid numberId");
        };
    }
}

