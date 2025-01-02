package org.poo.BankUsers.ServicePlan;

public class StandardPlan implements ServicePlan {
    static final double FEE = 0.002;
    @Override
    public double fee(double amount) {
        return FEE * amount;
    }
}
