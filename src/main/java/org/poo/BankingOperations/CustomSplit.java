package org.poo.BankingOperations;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class CustomSplit {
    private final List<String> accounts;
    private final List<Double> splitBill;
    private final Double totalAmount;
    private final String currency;
    private final int timestamp;
    private int accountsFound;

    public CustomSplit(final List<String> accounts, final List<Double> splitBill,
                       final String currency, final int timestamp,
                       final Double totalAmount) {
        this.accounts = accounts;
        this.splitBill = splitBill;
        this.currency = currency;
        this.timestamp = timestamp;
        this.totalAmount = totalAmount;
        accountsFound = 0;
    }

    public void incrementAccountsFound() {
        accountsFound++;
    }
}
