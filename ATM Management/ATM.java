import java.util.Scanner;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;

public class ATM {
    private static HashMap<String, Account> accounts = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);
    private static final String ACCOUNTS_FILE = "accounts.dat";

    public static void main(String[] args) {
        loadAccounts();
        runATM();
        saveAccounts();
    }

    private static void loadAccounts() {
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ACCOUNTS_FILE))) {
            @SuppressWarnings("unchecked")
            HashMap<String, Account> loadedAccounts = (HashMap<String, Account>) ois.readObject();
            accounts = loadedAccounts;
            System.out.println("Accounts loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("No existing accounts file found. Starting with empty accounts.");
            initializeAccounts();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading accounts. Starting with sample accounts.");
            initializeAccounts();
        }
    }

    private static void saveAccounts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ACCOUNTS_FILE))) {
            oos.writeObject(accounts);
            System.out.println("Accounts saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving accounts: " + e.getMessage());
        }
    }

    private static void initializeAccounts() {
        // For sample 
        accounts.put("123456789012345", new Account("123456789012345", "1234", 1000.0));
        accounts.put("987654321098765", new Account("987654321098765", "5678", 2000.0));
    }

    private static void runATM() {
        while (true) {
            System.out.println("\nWelcome to the ATM");
            System.out.println("1. Login");
            System.out.println("2. Create Account");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    login();
                    break;
                case "2":
                    createAccount();
                    break;
                case "3":
                    System.out.println("Thank you for using the ATM. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void login() {
        System.out.print("Enter account number (15 digits): ");
        String accountNumber = scanner.nextLine();
        
        if (accountNumber.length() != 15 || !accountNumber.matches("\\d+")) {
            System.out.println("Invalid account number. Please enter a 15-digit number.");
            return;
        }
        
        System.out.print("Enter PIN (4 digits): ");
        String pin = scanner.nextLine();
        
        if (pin.length() != 4 || !pin.matches("\\d+")) {
            System.out.println("Invalid PIN. Please enter a 4-digit number.");
            return;
        }

        Account account = accounts.get(accountNumber);
        if (account != null && account.validatePin(pin)) {
            performTransactions(account);
        } else {
            System.out.println("Invalid account number or PIN. Please try again.");
        }
    }

    private static void createAccount() {
        String accountNumber;
        do {
            System.out.print("Enter new account number (15 digits): ");
            accountNumber = scanner.nextLine();
            
            if (accountNumber.length() != 15 || !accountNumber.matches("\\d+")) {
                System.out.println("Invalid account number. Please enter a 15-digit number.");
                continue;
            }
            
            if (accounts.containsKey(accountNumber)) {
                System.out.println("Account number already exists. Please try a different one.");
                continue;
            }
            
            break;
        } while (true);
        
        String pin;
        do {
            System.out.print("Enter PIN for the new account (4 digits): ");
            pin = scanner.nextLine();
            
            if (pin.length() != 4 || !pin.matches("\\d+")) {
                System.out.println("Invalid PIN. Please enter a 4-digit number.");
            } else {
                break;
            }
        } while (true);
        
        Account newAccount = new Account(accountNumber, pin, 0.0);
        accounts.put(accountNumber, newAccount);
        System.out.println("Account created successfully. You can now login with your new account.");
        saveAccounts();
    }

    private static void performTransactions(Account account) {
        while (true) {
            System.out.println("\n1. Withdraw");
            System.out.println("2. Deposit");
            System.out.println("3. Balance Enquiry");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 4) {
                System.out.println("Thank you for using the ATM. Goodbye!");
                return;
            }

            switch (choice) {
                case 1:
                    withdraw(account);
                    break;
                case 2:
                    deposit(account);
                    break;
                case 3:
                    balanceEnquiry(account);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void withdraw(Account account) {
        System.out.print("Enter amount to withdraw: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        if (account.withdraw(amount)) {
            System.out.println("Withdrawal successful. New balance: INR " + account.getBalance());
            saveAccounts();
        } else {
            System.out.println("Insufficient funds or invalid amount.");
        }
    }

    private static void deposit(Account account) {
        System.out.print("Enter amount to deposit: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        if (account.deposit(amount)) {
            System.out.println("Deposit successful. New balance: INR " + account.getBalance());
            saveAccounts();
        } else {
            System.out.println("Invalid amount.");
        }
    }

    private static void balanceEnquiry(Account account) {
        System.out.println("Current balance: INR " + account.getBalance());
    }
}

class Account implements Serializable {
    private String accountNumber;
    private String pin;
    private double balance;
    private LocalDateTime lastTransaction;

    public Account(String accountNumber, String pin, double initialBalance) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = initialBalance;
        this.lastTransaction = LocalDateTime.now();
    }

    public boolean validatePin(String enteredPin) {
        return this.pin.equals(enteredPin);
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            updateLastTransaction();
            return true;
        }
        return false;
    }

    public boolean deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            updateLastTransaction();
            return true;
        }
        return false;
    }

    public double getBalance() {
        return balance;
    }

    private void updateLastTransaction() {
        lastTransaction = LocalDateTime.now();
    }

    public String getLastTransactionTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return lastTransaction.format(formatter);
    }
}