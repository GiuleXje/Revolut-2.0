package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.User;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;

public class UpgradePlan implements BankingOperations {
    static final double STANDARD_TO_SILVER = 100;
    static final double STANDARD_TO_GOLD = 350;
    static final double SILVER_TO_GOLD = 250;

    private double downgrade(final String currPlan, final String newPlan) {
        return switch (currPlan) {
            case "student", "standard" -> {
                if (newPlan.equals("silver")) {
                    yield STANDARD_TO_SILVER;
                } else if (newPlan.equals("gold")) {
                    yield STANDARD_TO_GOLD;
                }
                yield 0;
            }
            case "silver" -> {
                if (newPlan.equals("gold")) {
                    yield SILVER_TO_GOLD;
                }
                if (newPlan.equals("silver")) {
                    yield 0;
                }
                yield -1;
            }
            case "gold" -> {
                if (newPlan.equals("gold")) {
                    yield 0;
                }
                yield -1;
            }
            default -> 0;
        };
    }
    @Override
    public ObjectNode execute(BankOpData command) {
        IBANDB ibanDB = command.getIbanDB();
        CommandInput commandInput = command.getCommandInput();
        String account = commandInput.getAccount();
        User user = ibanDB.getUserFromIBAN(account);
        if (user == null) {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "upgradePlan");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "Account not found");
            out.put("timestamp", commandInput.getTimestamp());
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }

        BankAccount bankAccount = user.getBankAccounts().get(account);
        assert bankAccount != null;
        String currPlan = user.getPlan();
        String newPlan = commandInput.getNewPlanType();
        double upgradeCost = downgrade(currPlan, newPlan);

        if (upgradeCost > 0) {

            String accCurrency = bankAccount.getCurrency();
            ExchangeRate exchangeRate = command.getExchangeRate();
            double exRate = exchangeRate.getExchangeRate("RON", accCurrency);
            TransactionReport transactionReport = new TransactionReport();
            if (upgradeCost * exRate > bankAccount.getBalance()) {
                DataForTransactions data = new DataForTransactions().
                        withCommand("noFunds").
                        withTimestamp(commandInput.getTimestamp());
                ObjectNode report = transactionReport.executeOperation(data);
                user.addTransactionReport(report);
                bankAccount.addReport(report);
                return null;
            }
            // this also covers the payment
            user.changeServicePlan(newPlan);
            bankAccount.pay(upgradeCost * exRate);
            DataForTransactions data = new DataForTransactions().
                    withTimestamp(commandInput.getTimestamp()).
                    withCommand("changeOfPlan").
                    withNewPlan(newPlan).withAccount(account);
            ObjectNode report = transactionReport.executeOperation(data);
            user.addTransactionReport(report);
            bankAccount.addReport(report);
        } else if (upgradeCost < 0) {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "upgradePlan");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "You cannot downgrade your plan");
            out.put("timestamp", commandInput.getTimestamp());
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        } else {
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "upgradePlan");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "The user already has the " + currPlan + " plan.");
            out.put("timestamp", commandInput.getTimestamp());
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }

        return null;
    }
}
