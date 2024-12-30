package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.CardDB;
import org.poo.BankUsers.IBANDB;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;

public final class DeleteCard implements BankingOperations {
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
