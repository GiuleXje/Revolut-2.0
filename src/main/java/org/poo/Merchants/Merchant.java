package org.poo.Merchants;

import lombok.Getter;
import lombok.Setter;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.User;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.Merchants.CashBackStrategy.CashbackStrategy;
import org.poo.Merchants.CashBackStrategy.NrOfTransactions;
import org.poo.Merchants.CashBackStrategy.SpendingThreshold;

import java.util.HashMap;

@Getter
@Setter
public final class Merchant {
    private final String name;
    private final int id;
    private final String account;
    private final String type;
    private final String cashbackPlan;
    // this contains the total spent by each bank account on this specific merchant
    private HashMap<BankAccount, Double> buyers;
    private CashbackStrategy cashbackStrategy;
    public Merchant(final String name, final int id, final String account,
                    final String type, final String cashbackPlan) {
        this.name = name;
        this.id = id;
        this.account = account;
        this.type = type;
        this.cashbackPlan = cashbackPlan;
        cashbackStrategy = cashbackPlan.equals("nrOfTransactions") ? new NrOfTransactions()
                : new SpendingThreshold();
        buyers = new HashMap<>();
    }

    public void getCashback(final double amount, BankAccount account, final String plan,
                            final ExchangeRate exchangeRate) {
        cashbackStrategy.calculateCashback(amount, account, this, plan, exchangeRate);
    }

    public void spendMore(double amount, BankAccount bankAccount) {
        double amountSpent = buyers.getOrDefault(bankAccount, 0.0);
        amountSpent += amount;
        buyers.remove(bankAccount);
        buyers.put(bankAccount, amountSpent);
    }
}
