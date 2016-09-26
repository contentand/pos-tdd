package com.dy.pos;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Pos {

    private int deposit;
    private List<Integer> supportedCoins;
    private Map<String,Integer> priceList;
    private Map<String, Integer> order;
    private NavigableMap<Integer, Integer> coins;

    public Pos() {
        this.supportedCoins = formSupportedCoins();
        this.order = formBlankOrder();
        this.priceList = formPriceList();
        this.coins = formCoins(this.supportedCoins);
    }

    private List<Integer> formSupportedCoins() {
        return Arrays.asList(1, 5, 10, 20, 25, 50);
    }

    private Map<String, Integer> formBlankOrder() {
        return new HashMap<>();
    }

    private NavigableMap<Integer, Integer> formCoins(List<Integer> supportedCoins) {
        NavigableMap<Integer, Integer> coins = new TreeMap<>(Comparator.reverseOrder());
        supportedCoins.forEach(coin -> coins.put(coin, 1));
        return coins;
    }

    private Map<String,Integer> formPriceList() {
        Map<String,Integer> priceList = new HashMap<>();
        priceList.put("Tea", 15);
        priceList.put("Coffee", 25);
        priceList.put("Juice", 35);
        return priceList;
    }

    private void insertCoins(List<Integer> change) {
        change.forEach(this::insert);
    }

    private boolean areEqual(int deposit, List<Integer> change) {
        return change.stream().reduce(0, (a, b) -> a + b).equals(deposit);
    }

    private List<Integer> withdrawCoins(int amount) {
        List<Integer> result = new ArrayList<>();
        for (Integer value : coins.navigableKeySet()) {
            int count = amount / value;
            if (count > 0) {
                int nc = coins.get(value);
                int subtract = (nc - count >= 0) ? count : nc;
                amount -= subtract * value;
                coins.merge(value, subtract, (previous, current) -> previous - current);
                for (int i = 0; i < subtract; i++) {
                    result.add(value);
                }
            }
        }
        return result;
    }

    public void insert(int amount) {
        if (supportedCoins.contains(amount)) {
            coins.merge(amount, 1, (previous, current) -> previous + current);
            deposit += amount;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public int getBalance() {
        return deposit;
    }

    public Map<String,Integer> getPriceList() {
        return priceList;
    }

    public List<Integer> getSupportedCoins() {
        return supportedCoins;
    }

    public void addOrder(String productName, int quantity) {
        if (priceList.containsKey(productName)) {
            order.merge(productName, quantity, (previous, current) -> previous + current);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Map<String,Integer> buy() {
        Map<String, Integer> purchase = new HashMap<>(order);
        AtomicInteger amount = new AtomicInteger();
        purchase.forEach((product, quantity) -> {
            int price = priceList.get(product);
            amount.addAndGet(price * quantity);
        });
        if (amount.get() > deposit) throw new IllegalStateException();
        deposit -= amount.get();
        order.clear();
        return purchase;
    }

    public boolean isChangeAvailable() {
        int amount = deposit;
        for (Integer value : coins.navigableKeySet()) {
            int count = amount / value;
            if (count > 0) {
                int nc = coins.get(value);
                int subtract = (nc - count >= 0) ? count : nc;
                amount -= subtract * value;
            }
        }
        return amount == 0;
    }

    public List<Integer> getChange() {
        if (deposit == 0) return Collections.emptyList();
        List<Integer> change = withdrawCoins(deposit);
        if (areEqual(deposit, change)) {
            deposit = 0;
            return change;
        } else {
            insertCoins(change);
            throw new IllegalStateException();
        }
    }
}
