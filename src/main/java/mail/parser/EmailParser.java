package mail.parser;

import mail.model.ReceivedEmailEntity;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.io.IOException;

public class EmailParser {

    public ReceivedEmailEntity parse(Message message) throws MessagingException, IOException {
        ReceivedEmailEntity receivedEmailEntity = new ReceivedEmailEntity();
        receivedEmailEntity.setFrom(getEmailAddresses(message.getFrom()));
        receivedEmailEntity.setSubject(message.getSubject());
        receivedEmailEntity.setReceivedDate(message.getReceivedDate());
        receivedEmailEntity.setPlainText(getPlainTextPart(message));
        return receivedEmailEntity;
    }


    public String[] getEmailAddresses(Address[] addresses) {
        if (addresses != null && addresses.length > 0) {
            String[] stringAddresses = new String[addresses.length];
            for (int i = 0; i < addresses.length; i++) {
                if (addresses[i] instanceof InternetAddress) {
                    stringAddresses[i] = ((InternetAddress) addresses[i]).getAddress();
                } else {
                    stringAddresses[i] = addresses[i].toString();
                }
            }
            return stringAddresses;
        }
        return null;
    }

    public String getPlainTextPart(Part part) throws IOException, MessagingException {
        StringBuilder plainTextBuilder = new StringBuilder();
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                plainTextBuilder.append(getPlainTextPart(mp.getBodyPart(i)));
            }
        }
        if (part.isMimeType("text/plain")) {
            plainTextBuilder.append(part.getContent().toString());
        }
        return plainTextBuilder.toString();
    }
}
