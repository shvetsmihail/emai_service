package mail.service;

import mail.parser.EmailParser;
import mail.model.SendingEmailEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.*;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class EmailService {
    private final static Logger LOG = LoggerFactory.getLogger(EmailParser.class);
    private final static String DEFAULT_FOLDER = "INBOX";

    private final String imapHost;
    private final int imapPort;
    private final String smtpHost;
    private final int smtpPort;
    private final String user;
    private final String password;

    private volatile boolean reading = false;

    public EmailService() {
        imapHost = System.getProperty("mail.imap.host");
        imapPort = Integer.getInteger("mail.imap.port");
        smtpHost = System.getProperty("mail.smtp.host");
        smtpPort = Integer.getInteger("mail.smtp.port");
        user = System.getProperty("mail.user");
        password = System.getProperty("mail.password");
    }

    public Future startReadingAsync(Consumer<Message> task, int delay) {
        if (reading) {
            throw new IllegalStateException("Already in process");
        }
        reading = true;

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future future = executorService.submit(() -> {
            try (EmailReader emailReader = new EmailReader(imapHost, imapPort, user, password)) {
                while (reading) {
                    try {
                        emailReader.read(DEFAULT_FOLDER, getSearchTerm(), task);
                        LOG.debug("SUCCESS reading iteration. Sleeping {} ms", delay);
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        LOG.warn("InterruptedException while reading delay. Do nothing");
                    } catch (MessagingException e) {
                        LOG.error("Error while reading. Cause {}: {}", e.getClass().getName(), e.getMessage());
                    }
                }
            }
        }, null);
        executorService.shutdown();
        return future;

    }

    public void stopReading() {
        reading = false;
    }

    public Future sendMessageAsync(SendingEmailEntity msg) {
        return sendMessageAsync(Collections.singleton(msg));
    }

    public Future sendMessageAsync(Collection<SendingEmailEntity> msgs) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future future = executorService.submit(() -> {
            try (EmailSender emailSender = new EmailSender(smtpHost, smtpPort, user, password)) {
                for (SendingEmailEntity msg : msgs) {
                    try {
                        emailSender.sendMessage(msg);
                    } catch (MessagingException e) {
                        LOG.error("Error while sending message '{}' to {}. Cause: {}: {}", msg.getSubject(), msg.getEmailTo(), e.getClass().getName(), e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            }
        }, null);
        executorService.shutdown();
        return future;
    }

    private SearchTerm getSearchTerm() {
        SearchTerm searchTerm = null;

        boolean unseen = Boolean.getBoolean("mail.search.unseen");
        if (unseen) {
            Flags seenFlag = new Flags(Flags.Flag.SEEN);
            SearchTerm unseenTerm = new FlagTerm(seenFlag, false);
            searchTerm = addSearchTerm(searchTerm, unseenTerm);
        }

        int days = Integer.getInteger("mail.search.days", 0);
        if (days > 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -days);
            Date dateFrom = cal.getTime();
            SearchTerm dateTerm = new SentDateTerm(ComparisonTerm.GE, dateFrom);
            searchTerm = addSearchTerm(searchTerm, dateTerm);
        }

        String from = System.getProperty("mail.search.from");
        if (from != null && !from.isEmpty()) {
            SearchTerm emailFromTerm = new FromStringTerm(from);
            searchTerm = addSearchTerm(searchTerm, emailFromTerm);
        }

        String subject = System.getProperty("mail.search.subject");
        if (subject != null && !subject.isEmpty()) {
            SearchTerm subjectTerm = new SubjectTerm(subject);
            searchTerm = addSearchTerm(searchTerm, subjectTerm);
        }

        return searchTerm;
    }

    private SearchTerm addSearchTerm(SearchTerm t1, SearchTerm t2) {
        if (t1 == null) {
            return t2;
        } else {
            return new AndTerm(t1, t2);
        }
    }
}
