package app;
import exception.BankException;
import model.*;
import model.annotation.Audit;
import service.BankService;
import service.TransactionProcessor;
import util.AnnotationValidator;
import util.CommandParser;
import util.Validator;
import java.io.IOException;
import java.util.Scanner;
public class MiniBank {
    private static final BankInfo BANK=new BankInfo("MiniBank", "Main Branch");
    private static final Scanner SCANNER=new Scanner(System.in);
    private static final BankService SERVICE=new BankService();
    public static void main(String[] args) {
        System.out.println("====================================");System.out.println(BANK);System.out.println("Safe, simple console banking");
        boolean running=true;
        while(running){showMenu();int choice=readInt("Choose an option: ");
            try {
                MenuOption option=switch(choice){case 1->MenuOption.OPEN_ACCOUNT;case 2->MenuOption.DEPOSIT;case 3->MenuOption.WITHDRAW;case 4->MenuOption.TRANSFER;case 5->MenuOption.LIST_ACCOUNTS;case 6->MenuOption.STATEMENT;case 7->MenuOption.REPORT;case 8->MenuOption.EXIT;default->null;};
                if(option==null){System.out.println("Please enter a number from 1 to 8.");continue;}
                switch(option){
                    case OPEN_ACCOUNT -> openAccount();
                    case DEPOSIT -> {String id=readText("Account number: ");long amount=readLong("Deposit amount (₹): ");SERVICE.deposit(id,amount);System.out.println("Deposit successful.");}
                    case WITHDRAW -> {String id=readText("Account number: ");long amount=readLong("Withdrawal amount (₹): ");SERVICE.withdraw(id,amount);System.out.println("Withdrawal successful.");}
                    case TRANSFER -> {String from=readText("From account: ");String to=readText("To account: ");long amount=readLong("Amount (₹): ");SERVICE.transfer(from,to,amount);System.out.println("Transfer successful.");}
                    case LIST_ACCOUNTS -> {if(SERVICE.listAccounts().isEmpty())System.out.println("No accounts yet.");else SERVICE.listAccounts().forEach(System.out::println);}
                    case STATEMENT -> System.out.println(SERVICE.statement(readText("Account number: ")));
                    case REPORT -> System.out.println(SERVICE.report());
                    case EXIT -> {SERVICE.save();System.out.println("Saved account data. Thank you for banking with MiniBank. Goodbye!");running=false;}
                }
            } catch(BankException e){System.out.println("Banking error: "+e.getMessage());if(e instanceof exception.InsufficientFundsException f)System.out.println("Shortfall: ₹"+f.getShortfall());}
              catch(IOException e){System.out.println("File error: "+e.getMessage());}
              catch(IllegalArgumentException e){System.out.println("Input error: "+e.getMessage());}
        }
    }
    private static void showMenu(){System.out.println("\n1. Open account\n2. Deposit\n3. Withdraw\n4. Transfer\n5. List accounts\n6. Account statement\n7. End-of-day report\n8. Exit");}
    private static void openAccount() throws BankException {
        String name=readText("Account holder name: ");String type=readText("Type (SAVINGS/CURRENT/FIXED): ");long balance=readLong("Opening balance (₹): ");
        Account a=SERVICE.openAccount(name,type,balance);System.out.println("Account created: "+a);System.out.println("Keep your account number safe: "+a.getAccountNumber());
    }
    private static String readText(String prompt){System.out.print(prompt);return SCANNER.nextLine().trim();}
    private static int readInt(String prompt){while(true)try{return Integer.parseInt(readText(prompt));}catch(NumberFormatException e){System.out.println("Enter a whole number.");}}
    private static long readLong(String prompt){while(true)try{return Long.parseLong(readText(prompt));}catch(NumberFormatException e){System.out.println("Enter a whole number amount.");}}

    /** Small self-test for concepts covered in early practicals; not run by the banking menu. */
    public static void runConceptDemo(){
        System.out.println("Mobile valid? "+Validator.isValidMobile("9876543210"));
        System.out.println("Email valid? "+Validator.isValidEmail("student@example.com"));
        Command c=CommandParser.parse("DEPOSIT AC0001 500");System.out.println(c.type()+" / "+c.accountNumber()+" / "+c.amount());
        Account a=new SavingsAccount("Demo",1000);System.out.println(String.join(", ",AnnotationValidator.validate(a)));
        WithdrawRule rule=new WithdrawRule(){@Override public boolean allow(Account account,long amount){return account.canWithdraw(amount);}};
        WithdrawRule lambda=(account,amount)->account.canWithdraw(amount);System.out.println("Rules: "+rule.allow(a,100)+", "+lambda.allow(a,100));
        try(TransactionNote note=new TransactionNote()){System.out.println("Try-with-resources demo active");}
        try(TransactionProcessor processor=new TransactionProcessor()){processor.register(a);for(int i=0;i<10;i++)processor.submit(()->{try{a.deposit(1);}catch(exception.InvalidAmountException e){throw new RuntimeException(e);}});} 
    }
    private static class TransactionNote implements AutoCloseable {@Override public void close(){System.out.println("Resource closed.");}}
}
