package org.poo.Merchants.CashBackStrategy;

import org.poo.BankUsers.User;

public interface CashbackStrategy {
    double calculateCashback(double amount, User user);
}
