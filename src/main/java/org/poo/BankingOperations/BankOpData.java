package org.poo.BankingOperations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.BankUsers.EmailDB;
import org.poo.BankUsers.AliasDB;
import org.poo.BankUsers.IBANDB;
import org.poo.BankUsers.CardDB;
import org.poo.BankUsers.AccountDB;
import org.poo.ExchangeRate.ExchangeRate;
import org.poo.Transactions.TransactionReport;
import org.poo.fileio.CommandInput;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public final class BankOpData {
    private final CommandInput commandInput;
    private EmailDB emailDB;
    private ObjectNode returnVal;
    private IBANDB ibanDB;
    private CardDB cardDB;
    private final ExchangeRate exchangeRate;
    private AliasDB aliasDB;
    private AccountDB accountDB;
    private TransactionReport transactionReport;

    public BankOpData(final CommandInput commandInput, final EmailDB emailDB, final IBANDB ibanDB,
                      final CardDB cardDB,
                      final ExchangeRate exchangeRate,
                      final AliasDB aliasDB,
                      final AccountDB accountDB) {
        this.commandInput = commandInput;
        this.emailDB = emailDB;
        returnVal = null;
        this.ibanDB = ibanDB;
        this.cardDB = cardDB;
        this.exchangeRate = exchangeRate;
        this.aliasDB = aliasDB;
        this.accountDB = accountDB;
        transactionReport = new TransactionReport();
    }

    /**
     * selects the operation
     */
    public void execute() {
        switch (commandInput.getCommand()) {
            case "printUsers":
                PrintUsers printUsers = new PrintUsers();
                returnVal = printUsers.execute(this);
                break;
            case "addAccount":
                AddAccount addAccount = new AddAccount();
                returnVal = addAccount.execute(this);
                break;
            case "createCard":
                CreateCard createCard = new CreateCard();
                returnVal = createCard.execute(this);
                break;
            case "addFunds":
                AddFunds addFunds = new AddFunds();
                returnVal = addFunds.execute(this);
                break;
            case "deleteAccount":
                DeleteAccount deleteAccount = new DeleteAccount();
                returnVal = deleteAccount.execute(this);
                break;
            case "deleteCard":
                DeleteCard deleteCard = new DeleteCard();
                returnVal = deleteCard.execute(this);
                break;
            case "createOneTimeCard":
                CreateOneTimeCard createOneTimeCard = new CreateOneTimeCard();
                returnVal = createOneTimeCard.execute(this);
                break;
            case "payOnline":
                PayOnline payOnline = new PayOnline();
                returnVal = payOnline.execute(this);
                break;
            case "sendMoney":
                SendMoney sendMoney = new SendMoney();
                returnVal = sendMoney.execute(this);
                break;
            case "setAlias":
                SetAlias setAlias = new SetAlias();
                returnVal = setAlias.execute(this);
                break;
            case "printTransactions":
                PrintTransactions printTransactions = new PrintTransactions();
                returnVal = printTransactions.execute(this);
                break;
            case "checkCardStatus":
                CheckCardStatus checkCardStatus = new CheckCardStatus();
                returnVal = checkCardStatus.execute(this);
                break;
            case "setMinimumBalance":
                SetMinimumBalance setMinimumBalance = new SetMinimumBalance();
                returnVal = setMinimumBalance.execute(this);
                break;
            case "splitPayment":
                SplitPayment splitPayment = new SplitPayment();
                returnVal = splitPayment.execute(this);
                break;
            case "report":
                Report report = new Report();
                returnVal = report.execute(this);
                break;
            case "spendingsReport":
                SpendingsReport spendingsReport = new SpendingsReport();
                returnVal = spendingsReport.execute(this);
                break;
            case "changeInterestRate":
                ChangeInterestRate changeInterestRate = new ChangeInterestRate();
                returnVal = changeInterestRate.execute(this);
                break;
            case "addInterest":
                AddInterest addInterest = new AddInterest();
                returnVal = addInterest.execute(this);
                break;
            default:
                returnVal = null;
                break;
        }
    }

}
