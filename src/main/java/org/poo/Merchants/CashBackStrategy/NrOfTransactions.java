package org.poo.Merchants.CashBackStrategy;

import org.poo.BankUsers.BankAccount;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.Merchants.Merchant;

public class NrOfTransactions implements CashbackStrategy {
    static private final double FOOD_CASHBACK = 0.02;
    static private final int FOOD_PURCHASES = 2;
    static private final double CLOTHES_CASHBACK = 0.05;
    static private final int CLOTHES_PURCHASES = 5;
    static private final double TECH_CASHBACK = 0.1;
    static private final int TECH_PURCHASES = 10;

    @Override
    public void calculateCashback(final double amount, final BankAccount bankAccount,
                                  final Merchant merchant,
                                  final String plan, final ExchangeRate exchangeRate) {
        double exRate = exchangeRate.getExchangeRate("RON", bankAccount.getCurrency());
        String type = merchant.getType();

        switch (type) {
            case "Food":
                if (bankAccount.getUsedFoodCB().equals("unlocked")) {
                    bankAccount.setUsedFoodCB("used");
                    bankAccount.addFunds(amount * exRate * FOOD_CASHBACK);
                }
                break;
            case "Clothes":
                if (bankAccount.getUsedClothesCB().equals("unlocked")) {
                    bankAccount.setUsedClothesCB("used");
                    bankAccount.addFunds(amount * CLOTHES_CASHBACK * exRate);
                }
                break;
            case "Tech":
                if (bankAccount.getUsedTechCB().equals("unlocked")) {
                    bankAccount.setUsedTechCB("used");
                    bankAccount.addFunds(amount * TECH_CASHBACK * exRate);
                }
                break;
            default:
                break;
        }
    }
}
