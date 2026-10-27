package service;
import exception.InsufficientFundsException;
import model.Account;
import model.SavingsAccount;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
public class BankServiceTest {
    @Test void depositIncreasesBalance() throws Exception {Account a=new SavingsAccount("Test",1000,0);a.deposit(250);assertEquals(1250,a.getBalance());}
    @Test void withdrawalOverLimitThrows() {Account a=new SavingsAccount("Test",1000,500);assertThrows(InsufficientFundsException.class,()->a.withdraw(600));assertEquals(1000,a.getBalance());}
    @Test void concurrentDepositsKeepCorrectBalance() throws InterruptedException {
        Account a=new SavingsAccount("Thread Test",0,0);List<Thread> threads=new ArrayList<>();
        for(int i=0;i<10;i++){Thread t=new Thread(()->{for(int j=0;j<1000;j++)try{a.deposit(1);}catch(Exception e){throw new RuntimeException(e);}});threads.add(t);t.start();}
        for(Thread t:threads)t.join();assertEquals(10000,a.getBalance());
    }
}
