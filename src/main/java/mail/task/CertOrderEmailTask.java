package mail.task;

import mail.model.CertOrderEmailEntity;
import mail.model.ReceivedEmailEntity;
import mail.model.SendingEmailEntity;
import mail.parser.CertOrderEmailParser;
import mail.parser.EmailParser;
import mail.service.EmailSender;
import mail.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class CertOrderEmailTask implements Consumer<Message> {
    private final static Logger LOG = LoggerFactory.getLogger(EmailSender.class);

    private EmailService emailService;
    private EmailParser emailParser;
    private CertOrderEmailParser certOrderEmailParser;

    public CertOrderEmailTask(EmailService emailService, EmailParser emailParser, CertOrderEmailParser certOrderEmailParser) {
        this.emailService = emailService;
        this.emailParser = emailParser;
        this.certOrderEmailParser = certOrderEmailParser;
    }

    @Override
    public void accept(Message message) {
        try {
            ReceivedEmailEntity entity = emailParser.parse(message);
            CertOrderEmailEntity order = certOrderEmailParser.parse(entity);

            if (order == null) {
                LOG.warn("Message '{}' is not fit to {}", entity.getSubject(), CertOrderEmailEntity.class.getName());
            } else {
                LOG.debug("Saving order {} to database", order);
                if (order.getStatus().equals(CertOrderEmailParser.STATUS_RECEIVED)) {
                    String emailToSend = "shvetsmihail@gmail.com";
                    LOG.debug("Sending whole letter '{}' to {}", entity.getSubject(), emailToSend);
                    SendingEmailEntity sendingEmailEntity = new SendingEmailEntity(
                            new String[]{emailToSend},
                            entity.getSubject(),
                            entity.getPlainText(),
                            null);
                    Future future = emailService.sendMessageAsync(sendingEmailEntity);
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println(order);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not parse message", e);
        }
    }
}
