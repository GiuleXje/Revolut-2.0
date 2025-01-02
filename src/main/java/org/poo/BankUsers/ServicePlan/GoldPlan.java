package org.poo.BankUsers.ServicePlan;

public class GoldPlan implements ServicePlan {
    @Override
    public double fee(double amount) {
        return 0;
    }
}
