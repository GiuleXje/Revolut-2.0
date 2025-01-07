package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.User;
import org.poo.BankUsers.AliasDB;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.EmailDB;
import org.poo.BankUsers.IBANDB;
import org.poo.Transactions.DataForTransactions;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;


public final class AddAccount implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        AliasDB aliasDB = command.getAliasDB();
        IBANDB ibanDB = command.getIbanDB();
        TransactionReport transactionReport = command.getTransactionReport();
        EmailDB emailDB = command.getEmailDB();
        if (commandInput.getAccountType().equals("classic")) {
            commandInput.setInterestRate(0);
        }


        User user = emailDB.getUser(commandInput.getEmail());
        BankAccount bankAccount = new BankAccount(commandInput.getEmail(),
                commandInput.getCurrency(),
                commandInput.getAccountType(), commandInput.getTimestamp(),
                commandInput.getInterestRate());
        user.addBankAccount(bankAccount);
        aliasDB.addAlias(bankAccount.getIBAN() + user.getEmail(), bankAccount);
        ibanDB.addIBANReference(bankAccount.getIBAN(), user);
        DataForTransactions data = new DataForTransactions().
                withCommand("addAccount").
                withTimestamp(commandInput.getTimestamp());
        ObjectNode output = transactionReport.executeOperation(data);
        if (output != null) {
            user.addTransactionReport(output);
            bankAccount.addReport(output);
        }
        return null;
    }
}
