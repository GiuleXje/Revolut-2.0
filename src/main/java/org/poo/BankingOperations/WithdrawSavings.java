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

import java.time.LocalDate;

public final class WithdrawSavings implements BankingOperations {
    static final int YEAR_LENGTH = 4;
    static final int MONTH_LENGTH = 2;
    static final int LEGAL_AGE = 21;

    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        String account = commandInput.getAccount();
        IBANDB ibanDB = command.getIbanDB();
        User user = ibanDB.getUserFromIBAN(account);
        if (user == null) { //account not found
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "withdrawSavings");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "Account not found");
            out.put("timestamp", commandInput.getTimestamp());
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return output;
        }

        BankAccount bankAccount = user.getBankAccounts().get(account);
        assert bankAccount != null;
        if (!bankAccount.getAccountType().equals("savings")) {
            // not a savings account
            ObjectNode output = new ObjectMapper().createObjectNode();
            output.put("command", "withdrawSavings");
            ObjectNode out = new ObjectMapper().createObjectNode();
            out.put("description", "Account is not of type savings");
            out.put("timestamp", commandInput.getTimestamp());
            output.set("output", out);
            output.put("timestamp", commandInput.getTimestamp());
            return null;
        }

        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        int currentDay = LocalDate.now().getDayOfMonth();
        String birthDate = user.getBirthDate();
        int birthYear = Integer.parseInt(birthDate.substring(0, YEAR_LENGTH));
        int birthMonth = Integer.parseInt(birthDate.
                substring(YEAR_LENGTH + 1, YEAR_LENGTH + MONTH_LENGTH + 1));
        int birthDay = Integer.parseInt(birthDate.
                substring(YEAR_LENGTH + 2 * MONTH_LENGTH));

        TransactionReport transactionReport = command.getTransactionReport();
        if (currentYear - birthYear < LEGAL_AGE
                || (currentYear - birthYear == LEGAL_AGE && currentMonth < birthMonth)
                || (currentYear - birthYear == LEGAL_AGE && currentMonth == birthMonth
                && currentDay < birthDay)) { // too young
            DataForTransactions data = new DataForTransactions().
                    withTimestamp(commandInput.getTimestamp()).
                    withCommand("tooYoung");
            ObjectNode output = transactionReport.executeOperation(data);
            assert output != null;
            user.addTransactionReport(output);
            bankAccount.addReport(output);
            return null;
        }

        String currency = commandInput.getCurrency();
        ExchangeRate exchangeRate = command.getExchangeRate();
        double amount = commandInput.getAmount();
        double exRate = exchangeRate.getExchangeRate(currency, bankAccount.getCurrency());
        double balance = bankAccount.getBalance();
        if (balance < amount * exRate) { // balance too low
            DataForTransactions data =  new DataForTransactions().
                    withTimestamp(commandInput.getTimestamp()).
                    withCommand("noFunds");
            ObjectNode output = transactionReport.executeOperation(data);
            assert output != null;
            user.addTransactionReport(output);
            bankAccount.addReport(output);
            return null;
        }

        BankAccount classicAccount = user.getFirstAccountWithCurrency(currency);
        if (classicAccount == null) { // you don't have a classic account
            DataForTransactions data = new DataForTransactions().
                    withTimestamp(commandInput.getTimestamp()).
                    withCommand("noClassic");
            ObjectNode report = transactionReport.executeOperation(data);
            assert report != null;
            user.addTransactionReport(report);
            bankAccount.addReport(report);
            return null;
        }

        double classicExRate = exchangeRate.getExchangeRate(currency, classicAccount.getCurrency());
        bankAccount.pay(amount * exRate);
        classicAccount.addFunds(amount * classicExRate);

        return null;
    }
}
