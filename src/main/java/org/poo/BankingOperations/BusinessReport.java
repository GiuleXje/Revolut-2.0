package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.AccountDB;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.User;
import org.poo.Merchants.Merchant;
import org.poo.fileio.CommandInput;
import org.poo.ExchangeRate.Pair;

import java.util.*;

public class BusinessReport implements BankingOperations {
    /**
     * creates a business' accounts transaction report
     * @param command -
     * @return -
     */
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

    /**
     * creates a business' account merchant report
     * @param command -
     * @return -
     */
    private ObjectNode merchantReport(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        int start = commandInput.getStartTimestamp();
        int end = commandInput.getEndTimestamp();
        String account = commandInput.getAccount();
        AccountDB accountDB = command.getAccountDB();
        BankAccount bankAccount = accountDB.getAccounts().get(account);
        if (bankAccount == null) {
            return null;
        }

        LinkedHashMap<Merchant, ArrayList<Pair<Integer,Pair<User, Double>>>> spentOn =
                bankAccount.getSpentOnMerchants();

        LinkedHashMap<Merchant, ArrayList<Pair<Integer, Pair<User, Double>>>> sortedSpentOn = spentOn.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Merchant::getName)))
                .collect(
                        LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll
                );
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("command", commandInput.getCommand());
        ObjectNode report = new ObjectMapper().createObjectNode();
        report.put("balance", bankAccount.getBalance());
        ArrayNode merchantsArray = new ObjectMapper().createArrayNode();
        LinkedHashSet<User> employees = bankAccount.getEmployees();
        LinkedHashSet<User> managers = bankAccount.getManagers();

        for (Merchant merchant : sortedSpentOn.keySet()) {
            ArrayList<Pair<Integer, Pair<User, Double>>> paid = sortedSpentOn.get(merchant);
            double totalAmountSpent = 0;
            ArrayNode emp = new ObjectMapper().createArrayNode();
            ArrayNode man = new ObjectMapper().createArrayNode();
            ArrayList<String> empList = new ArrayList<>();
            ArrayList<String> managerList = new ArrayList<>();
            ObjectNode merch = new ObjectMapper().createObjectNode();
            merch.put("commerciant", merchant.getName());
            if (paid != null) {
                for (Pair<Integer, Pair<User, Double>> pair : paid) {
                    if (pair.getKey() >= start && pair.getKey() <= end) {
                        Pair<User, Double> userPair = pair.getValue();
                        User user = userPair.getKey();
                        Double amount = userPair.getValue();
                        if (employees.contains(user)) {
                            empList.add(user.getLastName() + " " + user.getFirstName());
                            totalAmountSpent += amount;
                        } else if (managers.contains(user)) {
                            managerList.add(user.getLastName() + " " + user.getFirstName());
                            totalAmountSpent += amount;
                        }
                    }
                }
                Collections.sort(empList);
                Collections.sort(managerList);
                for (String s : empList) {
                    emp.add(s);
                }
                for (String s : managerList) {
                    man.add(s);
                }

                merch.set("employees", emp);
                merch.set("managers", man);
                merch.put("total received", totalAmountSpent);
                merchantsArray.add(merch);
            }
        }

        report.set("commerciants", merchantsArray);
        report.put("currency", bankAccount.getCurrency());
        report.put("deposit limit", bankAccount.getDepositLimit());
        report.put("IBAN", bankAccount.getIBAN());
        report.put("spending limit", bankAccount.getSpendingLimit());
        report.put("statistics type", "commerciant");
        output.set("output", report);
        output.put("timestamp", commandInput.getTimestamp());
        return output;
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
