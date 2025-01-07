package org.poo.Merchants.CashBackStrategy;

import org.poo.BankUsers.BankAccount;
import org.poo.Merchants.Merchant;

public class NrOfTransactions implements CashbackStrategy {
    static private final double FOOD_CASHBACK = 0.02;
    static private final int FOOD_PURCHASES = 2;
    static private final double CLOTHES_CASHBACK = 0.05;
    static private final int CLOTHES_PURCHASES = 5;
    static private final double TECH_CASHBACK = 0.1;
    static private final int TECH_PURCHASES = 10;

    @Override
    public void calculateCashback(double amount, BankAccount bankAccount, Merchant merchant,
                                  String plan) {
        switch (merchant.getType()) {
            case "Food":
                if (!bankAccount.isUsedFoodCB()
                    && bankAccount.getTransactions() > FOOD_PURCHASES) {
                    bankAccount.setUsedFoodCB(true);
                    bankAccount.addFunds(FOOD_CASHBACK * amount);
                }
                break;
            case "Clothes":
                if (!bankAccount.isUsedClothesCB()
                    && bankAccount.getTransactions() > CLOTHES_PURCHASES) {
                    bankAccount.setUsedClothesCB(true);
                    bankAccount.addFunds(CLOTHES_CASHBACK * amount);
                }
                break;
            case "Tech":
                if (!bankAccount.isUsedTechCB()
                    && bankAccount.getTransactions() > TECH_PURCHASES) {
                    bankAccount.setUsedTechCB(true);
                    bankAccount.addFunds(TECH_CASHBACK * amount);
                }
                break;
            default:
                break;
        }
    }
}
