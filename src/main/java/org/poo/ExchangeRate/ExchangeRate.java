package org.poo.ExchangeRate;

import org.poo.fileio.ExchangeInput;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Queue;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ExchangeRate {
    private LinkedHashMap<String, ArrayList<Pair<String, Double>>> exchangeGraph;

    public ExchangeRate(final ExchangeInput[] exchangeInput) {
        exchangeGraph = new LinkedHashMap<>();
        for (ExchangeInput exRate : exchangeInput) {
            String to = exRate.getTo();
            String from = exRate.getFrom();
            double rate = exRate.getRate();
            exchangeGraph.putIfAbsent(from, new ArrayList<>());
            exchangeGraph.get(from).add(new Pair<>(to, rate));
            exchangeGraph.putIfAbsent(to, new ArrayList<>());
            exchangeGraph.get(to).add(new Pair<>(from, 1 / rate));
        }
    }

    /**
     * Performs BFS on a graph to find a transformation of one currency to another
     *
     * @param from the starting node
     * @param to   the end node
     * @return returns the exchange rate resulted
     */
    public double getExchangeRate(final String from, final String to) {
        if (from.equals(to)) {
            return 1.0;
        }
        Queue<String> queue = new LinkedList<>();
        queue.add(from);
        LinkedHashMap<String, Double> exRateFrom = new LinkedHashMap<>();
        LinkedHashSet<String> visited = new LinkedHashSet<>();
        exRateFrom.put(from, 1.0);
        visited.add(from);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            ArrayList<Pair<String, Double>> neighbours = exchangeGraph.get(current);
            if (neighbours != null) {
                for (Pair<String, Double> neighbour : neighbours) {
                    if (!visited.contains(neighbour.getKey())) {
                        visited.add(neighbour.getKey());
                        queue.add(neighbour.getKey());
                        double pathRate = exRateFrom.get(current) * neighbour.getValue();
                        exRateFrom.put(neighbour.getKey(),
                                pathRate);
                        if (neighbour.getKey().equals(to)) {
                            return exRateFrom.get(to);
                        }
                    }
                }
            }
        }
        return 0.0;
    }

}
