package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.AccountDB;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.User;
import org.poo.fileio.CommandInput;
import org.poo.ExchangeRate.Pair;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class BusinessReport implements BankingOperations {
    private ObjectNode transactionReport(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        int start = commandInput.getStartTimestamp();
        int end = commandInput.getEndTimestamp();
        String account = commandInput.getAccount();
        int timestamp = commandInput.getTimestamp();

        AccountDB accountDb = command.getAccountDB();
        BankAccount bankAccount = accountDb.getAccounts().get(account);
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("command", commandInput.getCommand());
        ObjectNode out = new ObjectMapper().createObjectNode();
        out.put("balance", bankAccount.getBalance());
        out.put("currency", bankAccount.getCurrency());
        out.put("deposit limit", bankAccount.getDepositLimit());

        double totalDeposited = 0;
        double totalSpent = 0;
        LinkedHashSet<User> employees = bankAccount.getEmployees();
        ArrayNode employeesArray = new ObjectMapper().createArrayNode();
        if (employees != null) {
            for (User employee : employees) {
                ArrayList<Pair<Double, Integer>> employeeSpent = bankAccount.
                        getSpentByEmployees().get(employee);
                double spent = 0;
                if (employeeSpent != null) {
                    for (Pair<Double, Integer> doubleIntegerPair : employeeSpent) {
                        if (doubleIntegerPair.getValue() >= start
                                && doubleIntegerPair.getValue() <= end) {
                            spent += doubleIntegerPair.getKey();
                            totalSpent += doubleIntegerPair.getKey();
                        }
                    }
                }
                ArrayList<Pair<Double, Integer>> employeesAdded = bankAccount.
                        getAddedByEmployees().get(employee);
                double added = 0;
                if (employeesAdded != null) {
                    for (Pair<Double, Integer> doubleIntegerPair : employeesAdded) {
                        if (doubleIntegerPair.getValue() >= start
                                && doubleIntegerPair.getValue() <= end) {
                            added += doubleIntegerPair.getKey();
                            totalDeposited += doubleIntegerPair.getKey();
                        }
                    }
                }
                ObjectNode employeeJson = new ObjectMapper().createObjectNode();
                employeeJson.put("deposited", added);
                employeeJson.put("spent", spent);
                employeeJson.put("username", employee.getLastName() +
                        " " + employee.getFirstName());
                employeesArray.add(employeeJson);
            }
        }
        out.set("employees", employeesArray);
        ArrayNode managersArray = new ObjectMapper().createArrayNode();
        LinkedHashSet<User> managers = bankAccount.getManagers();
        if (managers != null) {
            for (User manager : managers) {
                ArrayList<Pair<Double, Integer>> managerSpent = bankAccount.
                        getSpentByManagers().get(manager);
                double spent = 0;
                if (managerSpent != null) {
                    for (Pair<Double, Integer> doubleIntegerPair : managerSpent) {
                        if (doubleIntegerPair.getValue() >= start
                                && doubleIntegerPair.getValue() <= end) {
                            spent += doubleIntegerPair.getKey();
                            totalSpent += doubleIntegerPair.getKey();
                        }
                    }
                }
                ArrayList<Pair<Double, Integer>> managersAdded = bankAccount.
                        getAddedByManagers().get(manager);
                double added = 0;
                if (managersAdded != null) {
                    for (Pair<Double, Integer> doubleIntegerPair : managersAdded) {
                        if (doubleIntegerPair.getValue() >= start && doubleIntegerPair.getValue() <= end) {
                            added += doubleIntegerPair.getKey();
                            totalDeposited += doubleIntegerPair.getKey();
                        }
                    }
                }
                ObjectNode managerJson = new ObjectMapper().createObjectNode();
                managerJson.put("deposited", added);
                managerJson.put("spent", spent);
                managerJson.put("username", manager.getLastName()
                        + " " + manager.getFirstName());
                managersArray.add(managerJson);
            }
        }
        out.put("IBAN", bankAccount.getIBAN());
        out.set("managers", managersArray);
        out.put("spending limit", bankAccount.getSpendingLimit());
        out.put("statistics type", "transaction");
        out.put("total deposited", totalDeposited);
        out.put("total spent", totalSpent);
        output.set("output", out);
        output.put("timestamp", timestamp);
        return output;
    }
    private ObjectNode merchantReport(final BankOpData command) {
        return null;
    }
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        String type = commandInput.getType();
        if (type.equals("transaction")) {
            return transactionReport(command);
        } else {
            return merchantReport(command);
        }
    }
}
