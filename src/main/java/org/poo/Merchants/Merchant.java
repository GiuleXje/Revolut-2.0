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
    private static final double FOOD_CB = 0.02;
    private static final double CLOTHES_CB = 0.05;
    private static final double TECH_CB = 0.1;
    private final String name;
    private final int id;
    private final String account;
    private final String type;
    private final String cashbackPlan;
    // this contains the total spent by each bank account on this specific merchant
    private HashMap<BankAccount, Double> buyers;
    private HashMap<BankAccount, Integer> transactions;
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
        transactions = new HashMap<>();
    }

    /**
     * calculates the cashback depending on the type of the merchant
     * @param amount -
     * @param account -
     * @param plan -
     * @param exchangeRate -
     */
    public void getCashback(final double amount, BankAccount account, final String plan,
                            final ExchangeRate exchangeRate) {
        cashbackStrategy.calculateCashback(amount, account, this, plan, exchangeRate);
    }

    /**
     * adds more spendings related to a certain bank account
     * @param amount -
     * @param bankAccount -
     */
    public void spendMore(final double amount, final BankAccount bankAccount) {
        double amountSpent = buyers.getOrDefault(bankAccount, 0.0);
        amountSpent += amount;
        buyers.remove(bankAccount);
        buyers.put(bankAccount, amountSpent);
    }

    /**
     * in case the merchant is of type spendingThreshold, and we also can apply
     * the nrOfTransaction cashback
     * @param amount -
     * @param bankAccount -
     */
    public void forceCashback(final double amount, final BankAccount bankAccount) {

        if (type.equals("Food")
                && bankAccount.getUsedFoodCB().equals("unlocked")) {
            bankAccount.setUsedFoodCB("used");
            bankAccount.addFunds(amount * FOOD_CB);
        } else if (type.equals("Clothes")
                && bankAccount.getUsedClothesCB().equals("unlocked")) {
            bankAccount.setUsedClothesCB("used");
            bankAccount.addFunds(amount * CLOTHES_CB);
        } else if (type.equals("Tech")
                && bankAccount.getUsedTechCB().equals("unlocked")) {
            bankAccount.setUsedTechCB("used");
            bankAccount.addFunds(amount * TECH_CB);
        }
    }
}
