package mail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.search.SearchTerm;
import java.util.Properties;
import java.util.function.Consumer;

public class EmailReader implements AutoCloseable {
    private final static Logger LOG = LoggerFactory.getLogger(EmailReader.class);

    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final FetchProfile fetchProfile;

    private Store store;
    private Session session;

    public EmailReader(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;

        fetchProfile = new FetchProfile();
        fetchProfile.add(FetchProfile.Item.ENVELOPE);
        fetchProfile.add(FetchProfile.Item.CONTENT_INFO);
    }

    public void read(String folder, SearchTerm searchTerm, Consumer<Message> task) throws MessagingException {
        long start = System.currentTimeMillis();

        Store store = getStore();
        start = logTime("Connecting to store", start);
        try (Folder inbox = store.getFolder(folder)) {
            inbox.open(Folder.READ_WRITE);
            start = logTime("Opening folder " + folder, start);

            Message messages[];
            if (searchTerm != null) {
                messages = inbox.search(searchTerm);
                start = logTime("Searching messages", start);
            } else {
                messages = inbox.getMessages();
            }

            inbox.fetch(messages, fetchProfile);
            start = logTime("Fetcing messages", start);

            LOG.info("Finding {} messages. Total count in {} = {}",
                    messages.length, folder, inbox.getMessageCount());

            for (Message message : messages) {
                try {
                    task.accept(message);
                } catch (Exception e) {
                    LOG.error("Error while post processing message. Mark message as 'UNSEEN'. Cause: {}: {}",
                            e.getClass().getName(), e.getMessage());
                    message.setFlag(Flags.Flag.SEEN, false);
                }
            }
            logTime("Processing messages", start);
        }
    }

    @Override
    public void close() {
        try {
            if (store != null && store.isConnected()) {
                LOG.info("Closing store {} ...", store);
                store.close();
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

    private Store getStore() throws MessagingException {
        if (store == null || !store.isConnected()) {
            LOG.info("Connecting to store ...");
            store = getSession().getStore();
            store.connect(host, port, user, password);
            LOG.info("SUCCESS connecting to store {}", store);
        }
        return store;
    }

    private Session getSession() {
        if (session == null) {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imap.ssl.enable", "true");
            session = Session.getInstance(props);
        }
        return session;
    }
}