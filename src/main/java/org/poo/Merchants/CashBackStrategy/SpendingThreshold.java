package org.poo.Merchants.CashBackStrategy;

import org.poo.BankUsers.User;

public class SpendingThreshold implements CashbackStrategy {
    @Override
    public double calculateCashback(double amount, User user) {
        return 0;
    }
}
