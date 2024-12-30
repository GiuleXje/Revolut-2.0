package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface BankingOperations {
    /**
     * generates the JSON for an output
     *
     * @param command data needed for out command
     * @return an ObjectNode if an output is needed, null otherwise
     */
    ObjectNode execute(BankOpData command);
}

