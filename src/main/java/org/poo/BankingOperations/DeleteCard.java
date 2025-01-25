package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.*;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;

import java.util.HashSet;

public final class DeleteCard implements BankingOperations {
    /**
     * handles a business account card deletion
     * @param bankAccount -
     * @param command -
     */
    private void handleBusinessAccount(final BankAccount bankAccount, final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        String cardNumber = commandInput.getCardNumber();
        CardDB cardDB = command.getCardDB();
        TransactionReport transactionReport = command.getTransactionReport();
        String email = commandInput.getEmail();
        EmailDB emailDB = command.getEmailDB();
        User user = emailDB.getUser(email);
        User createdBy = bankAccount.getBusinessCards().get(cardNumber);
        HashSet<User> employees = bankAccount.getEmployees();
        if (!user.equals(createdBy) && employees.contains(user)) {
            // an employee can't delete a card that wasn't created by him
            return;
        }

        bankAccount.getBusinessCards().remove(cardNumber);
        cardDB.deleteCard(cardNumber);
        DataForTransactions data = new DataForTransactions().
                withCommand("deleteCard").
                withAccount(bankAccount.getIBAN()).
                withCardNumber(cardNumber).
                withEmail(bankAccount.getEmail()).
                withTimestamp(commandInput.
                        getTimestamp());
        ObjectNode output = transactionReport.executeOperation(data);
        if (output != null) {
            bankAccount.addReport(output);
        }
    }
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        CardDB cardDB = command.getCardDB();
        TransactionReport transactionReport = command.getTransactionReport();
        IBANDB ibanDB = command.getIbanDB();

        String cardNumber = commandInput.getCardNumber();
        BankAccount bankAccount = cardDB.getAssociatedCards().get(cardNumber);
        // delete the card from the bank account
        if (bankAccount != null) {
            if (bankAccount.getAccountType().equals("business")) {
                handleBusinessAccount(bankAccount, command);
                return null;
            }
            bankAccount.deleteCard(bankAccount.getCards().get(cardNumber));
            // delete the card from the database
            cardDB.deleteCard(cardNumber);
            DataForTransactions data = new DataForTransactions().
                    withCommand("deleteCard").
                    withAccount(bankAccount.getIBAN()).
                    withCardNumber(cardNumber).
                    withEmail(bankAccount.getEmail()).
                    withTimestamp(commandInput.
                            getTimestamp());
            ObjectNode output = transactionReport.executeOperation(data);
            if (output != null) {
                ibanDB.
                        getUserFromIBAN(bankAccount.getIBAN()).
                        addTransactionReport(output);
                bankAccount.addReport(output);
            }
        }

        return null;
    }
}
