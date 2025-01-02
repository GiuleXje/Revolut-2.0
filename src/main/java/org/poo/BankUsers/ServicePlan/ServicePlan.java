package org.poo.BankUsers.ServicePlan;

public interface ServicePlan {
    /**
     * return the fee that has to be paid after each transaction
     * @param amount
     * the amount which will be charged
     * @return
     * the total take
     */
    double fee(double amount);
}
