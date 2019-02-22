package mail;

import mail.model.SendingEmailEntity;
import mail.parser.CertOrderEmailParser;
import mail.parser.EmailParser;
import mail.service.EmailService;
import mail.task.CertOrderEmailTask;

import javax.mail.Message;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        Config.getInstance();

        EmailService emailService = new EmailService();
        EmailParser emailParser = new EmailParser();
        CertOrderEmailParser certOrderEmailParser = new CertOrderEmailParser();

        //Reading example
        Consumer<Message> task = new CertOrderEmailTask(emailService, emailParser, certOrderEmailParser);
        Future futureRead = emailService.startReadingAsync(task, 0);
        Thread.sleep(100);
        emailService.stopReading();
        futureRead.get();

        //Sending example;
        List<SendingEmailEntity> msgs = new ArrayList<>();
        msgs.add(new SendingEmailEntity(
                new String[]{"shvetsmihail@gmail.com"},
                "test",
                "Hello World",
                "src/main/resources/test.properties"));

        Future futureSend = emailService.sendMessageAsync(msgs);
        futureSend.get();

    }
}
