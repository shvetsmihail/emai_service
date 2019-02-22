package mail.service;

import mail.model.SendingEmailEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Arrays;
import java.util.Properties;

public class EmailSender implements AutoCloseable {
    private final static Logger LOG = LoggerFactory.getLogger(EmailSender.class);

    private final String host;
    private final int port;
    private final String user;
    private final String password;

    private Transport transport;
    private Session session;

    public EmailSender(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public void sendMessage(SendingEmailEntity msg) throws MessagingException {

        long start = System.currentTimeMillis();

        MimeMessage message = new MimeMessage(getSession());

        message.setRecipients(Message.RecipientType.TO, toAddress(msg.getEmailTo()));

        message.setSubject(msg.getSubject());

        Multipart mmp = new MimeMultipart();
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(msg.getPlainText(), "text/plain; charset=utf-8");
        mmp.addBodyPart(bodyPart);

        start = logTime("Creating message", start);

        if (msg.getFilePath() != null) {
            MimeBodyPart mbr = createFileAttachment(msg.getFilePath());
            mmp.addBodyPart(mbr);
            start = logTime("Adding attachment bodyPart", start);
        }

        message.setContent(mmp);

        Transport transport = getTransport();
        start = logTime("Connecting transport", start);
        transport.sendMessage(message, message.getAllRecipients());
        logTime("Sending message '" + msg.getSubject() + "' to " + Arrays.toString(msg.getEmailTo()), start);
    }

    private Address[] toAddress(String[] emailTo) throws AddressException {
        Address[] addresses = new Address[emailTo.length];
        for (int i = 0; i < emailTo.length; i++) {
            addresses[i] = new InternetAddress(emailTo[i]);
        }
        return addresses;
    }

    private MimeBodyPart createFileAttachment(String filepath) throws MessagingException {
        MimeBodyPart mbp = new MimeBodyPart();
        FileDataSource fds = new FileDataSource(filepath);
        mbp.setDataHandler(new DataHandler(fds));
        mbp.setFileName(fds.getName());
        return mbp;
    }

    private Session getSession() {
        if (session == null) {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.user", user);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            session = Session.getInstance(props);
        }
        return session;
    }

    private Transport getTransport() throws MessagingException {
        if (transport == null || !transport.isConnected()) {
            transport = getSession().getTransport("smtp");
            transport.connect(user, password);
        }
        return transport;
    }

    @Override
    public void close() {
        try {
            if (transport != null && transport.isConnected()) {
                LOG.info("Closing transport {} ...", transport);
                transport.close();
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private long logTime(String title, long start) {
        long finish = System.currentTimeMillis();
        LOG.debug(title + ": " + (finish - start) + " ms");
        return finish;
    }
}