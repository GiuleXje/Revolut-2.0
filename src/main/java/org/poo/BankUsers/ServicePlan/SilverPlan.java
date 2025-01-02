package org.poo.BankUsers.ServicePlan;

public class SilverPlan implements ServicePlan {
    static final double FEE = 0.001;
    @Override
    public double fee(double amount) {
        return amount * FEE;
    }
}
