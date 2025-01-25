package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.BankAccount;
import org.poo.BankUsers.Card;
import org.poo.BankUsers.EmailDB;
import org.poo.BankUsers.User;
import org.poo.fileio.CommandInput;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PrintUsers implements BankingOperations {
    @Override
    public ObjectNode execute(final BankOpData command) {
        EmailDB emailDB = command.getEmailDB();
        CommandInput commandInput = command.getCommandInput();
        ObjectNode output = new ObjectMapper().createObjectNode();
        output.put("command", "printUsers");
        ArrayNode users = new ObjectMapper().createArrayNode();
        for (User user : emailDB.getAssociatedEmails().values()) {
            ObjectNode userInfo = new ObjectMapper().createObjectNode();
            userInfo.put("firstName", user.getFirstName());
            userInfo.put("lastName", user.getLastName());
            userInfo.put("email", user.getEmail());
            ArrayNode bankAccInfo = new ObjectMapper().createArrayNode();
            for (BankAccount bankAccount : user.getBankAccounts().values()) {
                ObjectNode bankAcc = new ObjectMapper().createObjectNode();
                bankAcc.put("balance", bankAccount.getBalance());
                ArrayNode cards = new ObjectMapper().createArrayNode();
                for (Card card : bankAccount.getCards().values()) {
                    ObjectNode cardInfo = new ObjectMapper().createObjectNode();
                    cardInfo.put("cardNumber", card.getNumber());
                    cardInfo.put("status", card.getStatus());
                    cards.add(cardInfo);
                }
                bankAcc.put("IBAN", bankAccount.getIBAN());
                bankAcc.put("currency", bankAccount.getCurrency());
                bankAcc.put("type", bankAccount.getAccountType());
                bankAcc.set("cards", cards);
                bankAccInfo.add(bankAcc);
            }
            userInfo.set("accounts", bankAccInfo);
            users.add(userInfo);
        }
        output.set("output", users);
        output.put("timestamp", commandInput.getTimestamp());
        return output;
    }
}
