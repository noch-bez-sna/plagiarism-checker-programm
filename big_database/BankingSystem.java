import java.util.ArrayList;
import java.util.List;

class BankAccount {
    private String accountNumber;
    private String ownerName;
    private double balance;
    private List<String> transactionHistory;

    public BankAccount(String accountNumber, String ownerName, double initialBalance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = initialBalance;
        this.transactionHistory = new ArrayList<>();
        addTransaction("Account opened with balance: " + initialBalance);
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            addTransaction("Deposited: " + amount);
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            addTransaction("Withdrawn: " + amount);
            return true;
        }
        return false;
    }

    public void transfer(BankAccount recipient, double amount) {
        if (withdraw(amount)) {
            recipient.deposit(amount);
            addTransaction("Transferred " + amount + " to account " + recipient.accountNumber);
        }
    }

    public double getBalance() {
        return balance;
    }

    public List<String> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    private void addTransaction(String transaction) {
        transactionHistory.add(java.time.LocalDateTime.now() + ": " + transaction);
    }
}

public class BankingSystem {
    private List<BankAccount> accounts = new ArrayList<>();

    public BankAccount createAccount(String ownerName, double initialDeposit) {
        String accountNumber = "ACC" + (accounts.size() + 1000);
        BankAccount account = new BankAccount(accountNumber, ownerName, initialDeposit);
        accounts.add(account);
        return account;
    }

    public BankAccount findAccount(String accountNumber) {
        for (BankAccount account : accounts) {
            if (account.getTransactionHistory().toString().contains(accountNumber)) {
                return account;
            }
        }
        return null;
    }

    public double getTotalBankBalance() {
        double total = 0;
        for (BankAccount account : accounts) {
            total += account.getBalance();
        }
        return total;
    }
}