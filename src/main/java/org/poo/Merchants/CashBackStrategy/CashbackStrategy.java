package org.poo.Merchants.CashBackStrategy;

import org.poo.BankUsers.User;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.Merchants.Merchant;
import org.poo.BankUsers.BankAccount;

public interface CashbackStrategy {
    /**
     * calculated the cashback amount for each used depending on the merchant
     * and its cashback strategy
     * @param amount
     * the amount paid to the merchant
     * @param bankAccount
     * bankAccount
     * @param merchant
     * the merchant
     * @param plan
     * the user's plan
     */
    void calculateCashback(double amount, BankAccount bankAccount, Merchant merchant,
                           String plan, ExchangeRate exchangeRate);
}
