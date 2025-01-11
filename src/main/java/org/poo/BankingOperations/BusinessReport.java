package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public class BusinessReport implements BankingOperations {
    private void transactionReport(final BankOpData command) {

    }
    private void merchantReport(final BankOpData command) {

    }
    @Override
    public ObjectNode execute(final BankOpData command) {
        CommandInput commandInput = command.getCommandInput();
        String type = commandInput.getType();
        if (type.equals("transaction")) {
            transactionReport(command);
        } else {
            merchantReport(command);
        }
        return null;
    }
}
