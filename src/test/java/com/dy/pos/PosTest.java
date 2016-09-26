package com.dy.pos;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.*;

@RunWith(Theories.class)
public class PosTest {

    private Pos pos;

    private static Map<String,Integer> getPriceList() {
        Map<String, Integer> priceList = new HashMap<>();
        priceList.put("Tea", 15);
        priceList.put("Coffee", 25);
        priceList.put("Juice", 35);
        return priceList;
    }

    private static List<Integer> getSupportedCoins() {
        return Arrays.asList(1, 5, 10, 20, 25, 50);
    }

    private int sumOf(List<Integer> returnedCoins) {
        return returnedCoins.stream().reduce(0, (a, b) -> a + b);
    }

    @Before
    public void setup() {
        pos = new Pos();
    }

    @DataPoints("Coin values")
    public static List<Integer> getCoinValues() {
        return getSupportedCoins();
    }

    @DataPoints("Product names")
    public static Set<String> getProductNames() {
        return getPriceList().keySet();
    }

    @Theory
    public void canInsertValidCoinValues(@FromDataPoints("Coin values") int coinValue) throws Exception {
        pos.insert(coinValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void insertInvalidAmount_throwsIllegalArgumentException() throws Exception {
        int invalid_amount = 26;
        pos.insert(invalid_amount);
    }

    @Test
    public void returnsValidBalance() throws Exception {
        pos.insert(1);
        pos.insert(5);
        pos.insert(10);
        assertThat(pos.getBalance(), is(16));
    }

    @Test
    public void returnsPriceList() throws Exception {
        Map<String, Integer> expectedPriceList = getPriceList();
        Map<String, Integer> actualPriceList = pos.getPriceList();
        assertThat(actualPriceList.entrySet(), is(expectedPriceList.entrySet()));
    }

    @Test
    public void returnsSupportedCoins() throws Exception {
        List<Integer> expectedSupportedCoins = getSupportedCoins();
        List<Integer> actualSupportedCoins = pos.getSupportedCoins();
        assertThat(actualSupportedCoins, is(expectedSupportedCoins));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addInvalidProductName_throwsIllegalArgumentException() throws Exception {
        pos.addOrder("UnsupportedName", 2);
    }

    @Theory
    public void canBuy(@FromDataPoints("Product names") String productName) throws Exception {
        Map<String, Integer> expectedBucket = new HashMap<>();
        expectedBucket.put(productName, 1);
        pos.addOrder(productName, 1);
        pos.insert(50);
        Map<String, Integer> actualBucket = pos.buy();
        assertThat(actualBucket, is(expectedBucket));
    }

    @Test(expected = IllegalStateException.class)
    public void buyWithInsufficientFunds_throwsIllegalStateException() throws Exception {
        pos.addOrder("Tea", 1);
        pos.buy();
    }

    @Test
    public void canGetChangeIfNothingPurchased() throws Exception {
        List<Integer> insertedCoins = new ArrayList<>();
        insertedCoins.add(5);
        insertedCoins.add(10);
        insertedCoins.forEach(pos::insert);
        List<Integer> returnedCoins = pos.getChange();
        assertThat(returnedCoins.size(), is(2));
        assertTrue(returnedCoins.containsAll(insertedCoins));
    }

    @Test
    public void returnsCorrectChangeAfterPurchase() throws Exception {
        pos.insert(50);
        pos.addOrder("Tea", 1);
        pos.buy();
        List<Integer> returnedCoins = pos.getChange();
        assertThat(sumOf(returnedCoins), is(35));
    }

    @Test
    public void returnsTrueIfChangeIsAvailable() throws Exception {
        pos.insert(10);
        assertThat(pos.isChangeAvailable(), is(true));
    }

    @Test
    public void returnsFalseIfChangeIsUnavailable() throws Exception {
        pos.insert(50);
        pos.addOrder("Tea", 1);
        pos.buy();
        pos.getChange();
        pos.insert(50);
        pos.addOrder("Tea", 1);
        pos.buy();
        assertThat(pos.isChangeAvailable(), is(false));
    }
}
