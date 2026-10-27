package service;
import exception.*;
import model.*;
import model.annotation.Audit;
import util.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
public class BankService {
    private final Repository<Account> repository=new Repository<>();
    private final ConcurrentHashMap<String,Account> accounts=new ConcurrentHashMap<>();
    private final AccountStore store=new AccountStore();
    private final Path stateFile=Path.of("data","accounts.bin");
    private final Path logFile=Path.of("data","logs","transactions.log");
    public BankService(){try{for(Account a:StatePersister.load(stateFile)) register(a);}catch(IOException e){System.err.println("Could not load saved accounts: "+e.getMessage());}}
    private void register(Account account){Account.syncCounterFrom(account);repository.save(account);accounts.put(account.getAccountNumber(),account);store.add(account);}
    public synchronized Account openAccount(String owner, String type, long openingBalance) throws BankException {
        if(owner==null||owner.isBlank()) throw new BankException("Account holder name is required");
        if(openingBalance<0) throw new InvalidAmountException("Opening balance cannot be negative");
        Account account=switch(type.toUpperCase()) {
            case "SAVINGS" -> new SavingsAccount(owner,openingBalance);
            case "CURRENT" -> new CurrentAccount(owner,openingBalance,10000);
            case "FIXED", "FIXED_DEPOSIT" -> new FixedDepositAccount(owner,openingBalance);
            default -> throw new BankException("Unknown account type. Use SAVINGS, CURRENT or FIXED");
        };
        register(account); log(account,"OPEN",openingBalance); return account;
    }
    public Account findAccount(String number) throws AccountNotFoundException {Account a=accounts.get(number);if(a==null)throw new AccountNotFoundException("No account found with number "+number);return a;}
    @Audit("deposit") public void deposit(String number,long amount) throws BankException {Account a=findAccount(number);a.deposit(amount);log(a,"DEPOSIT",amount);}
    @Audit("withdraw") public void withdraw(String number,long amount) throws BankException {Account a=findAccount(number);a.withdraw(amount);log(a,"WITHDRAW",amount);}
    @Audit("transfer") public void transfer(String from,String to,long amount) throws BankException {Account source=findAccount(from),destination=findAccount(to);source.transfer(destination,amount);log(source,"TRANSFER_OUT",amount);log(destination,"TRANSFER_IN",amount);}
    public String statement(String number) throws AccountNotFoundException {return StatementFormatter.buildStatement(findAccount(number));}
    public ArrayList<Account> listAccounts(){ArrayList<Account> list=new ArrayList<>(accounts.values());list.sort(Comparator.comparing(Account::getAccountNumber));return list;}
    public String report() throws IOException {return ReportGenerator.generate(logFile.getParent(),Path.of("data","report.txt"));}
    public synchronized void save() throws IOException {StatePersister.save(accounts.values().toArray(Account[]::new),stateFile);}
    public Repository<Account> getRepository(){return repository;}
    public ConcurrentHashMap<String,Account> getAccounts(){return accounts;}
    public AccountStore getStore(){return store;}
    private void log(Account a,String type,long amount){try{TransactionLog.append(logFile,LocalDateTime.now()+"|"+type+"|"+amount+"|"+a.getAccountNumber()+"|"+a.getBalance());store.addHistory(type+" "+amount+" for "+a.getAccountNumber());}catch(IOException e){System.err.println("Warning: transaction succeeded but log could not be written: "+e.getMessage());}}
}
