package org.poo.BankUsers;

import lombok.Getter;
import lombok.Setter;
import org.poo.BankingOperations.SplitPayment;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public final class CustomSplit {
    private final List<String> accounts;
    private final List<Double> splitBill;
    private List<Boolean> hasPayed;
    private final Double totalAmount;
    private final String currency;
    private final int timestamp;
    private final String type;
    private int accountsFound;

    public CustomSplit(final List<String> accounts, final List<Double> splitBill,
                       final String currency, final int timestamp,
                       final Double totalAmount, final String type) {
        this.accounts = accounts;
        this.splitBill = splitBill;
        this.currency = currency;
        this.timestamp = timestamp;
        this.totalAmount = totalAmount;
        this.hasPayed = new ArrayList<>(accounts.size());
        for (int i = 0; i < accounts.size(); i++) {
            hasPayed.add(false);
        }
        accountsFound = 0;
        this.type = type;
    }


    public void incrementAccountsFound() {
        accountsFound++;
    }

}