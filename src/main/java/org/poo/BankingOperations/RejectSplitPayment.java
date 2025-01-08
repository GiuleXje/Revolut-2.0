package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class RejectSplitPayment implements BankingOperations {
    @Override
    public ObjectNode execute(BankOpData command) {
        CustomSplit customSplit = command.getSplit();

        return null;
    }
}
