package org.poo.Merchants.CashBackStrategy;

import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.User;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.Merchants.Merchant;

public class SpendingThreshold implements CashbackStrategy {
    static private final double FIRST_CAP = 100;
    static private final double SECOND_CAP = 300;
    static private final double THIRD_CAP = 500;
    static private final double STANDARD_FIRST = 0.001;
    static private final double SILVER_FIRST = 0.003;
    static private final double GOLD_FIRST = 0.005;
    static private final double STANDARD_SECOND = 0.002;
    static private final double SILVER_SECOND = 0.004;
    static private final double GOLD_SECOND = 0.0055;
    static private final double STANDARD_THIRD = 0.0025;
    static private final double SILVER_THIRD = 0.005;
    static private final double GOLD_THIRD = 0.007;

    @Override
    public void calculateCashback(final double amount, final BankAccount bankAccount,
                                  final Merchant merchant,
                                  final String plan, final ExchangeRate exchangeRate) {
        double spentOn = bankAccount.getSpending();
        double exRate = exchangeRate.getExchangeRate("RON", bankAccount.getCurrency());
        if (spentOn < FIRST_CAP) {
            return;
        } else if (spentOn < SECOND_CAP) {
            switch (plan) {
                case "student", "standard":
                    bankAccount.addFunds(amount * STANDARD_FIRST * exRate);
                    break;
                case "silver":
                    bankAccount.addFunds(amount * SILVER_FIRST * exRate);
                    break;
                case "gold":
                    bankAccount.addFunds(amount * GOLD_FIRST * exRate);
                    break;
                default:
                    break;
            }
        } else if (spentOn < THIRD_CAP) {
            switch (plan) {
                case "student", "standard":
                    bankAccount.addFunds(amount * STANDARD_SECOND * exRate);
                    break;
                case "silver":
                    bankAccount.addFunds(amount * SILVER_SECOND * exRate);
                    break;
                case "gold":
                    bankAccount.addFunds(amount * GOLD_SECOND * exRate);
                    break;
                default:
                    break;
            }
        } else {
            switch (plan) {
                case "student", "standard":
                    bankAccount.addFunds(amount * STANDARD_THIRD * exRate);
                    break;
                case "silver":
                    bankAccount.addFunds(amount * SILVER_THIRD * exRate);
                    break;
                case "gold":
                    bankAccount.addFunds(amount * GOLD_THIRD * exRate);
                    break;
                default:
                    break;
            }
        }
    }
}
