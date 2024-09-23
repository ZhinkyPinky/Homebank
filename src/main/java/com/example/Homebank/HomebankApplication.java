package com.example.Homebank;

import com.example.Homebank.businessLogic.services.TransactionHeadService;
import com.example.Homebank.dataAccess.entities.TransactionHead;
import com.example.Homebank.dataAccess.repositories.TransactionHeadRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@SpringBootApplication
public class HomebankApplication {
    private static ConfigurableApplicationContext configurableApplicationContext;

    public HomebankApplication(ConfigurableApplicationContext configurableApplicationContext) {
        HomebankApplication.configurableApplicationContext = configurableApplicationContext;
    }

    public static void main(String[] args) {
        configurableApplicationContext = SpringApplication.run(HomebankApplication.class, args);

        /*
        configurableApplicationContext.getBean(TransactionHeadRepository.class).saveTransactionHead(
                null,
                1L,
                2L,
                "abc",
                "sdf",
                LocalDate.now(),
                LocalDate.now(),
                LocalDate.now(),
                LocalDateTime.now()
        );

*/

        try {
            TransactionHead transactionHead = new TransactionHead();
            transactionHead.setBorrowerId(64L);
            transactionHead.setLenderId(2L);
            transactionHead.setStartDate(LocalDate.now());

            Map<String, Object> map =
                    configurableApplicationContext.getBean(TransactionHeadService.class).saveTransactionHead(
                            transactionHead
                    );

            System.out.println(map.get("p_OUT_Id"));
            System.out.println(map.get("p_OUT_RowVersion"));
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

}
