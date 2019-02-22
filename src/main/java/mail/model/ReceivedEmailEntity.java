package mail.model;


import java.util.Arrays;
import java.util.Date;

public class ReceivedEmailEntity {
    private String[] from;
    private String subject;
    private Date receivedDate;
    private String plainText;

    public String[] getFrom() {
        return from;
    }

    public void setFrom(String[] from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getPlainText() {
        return plainText;
    }

    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }

    @Override
    public String toString() {
        return "ReceivedEmailEntity : {" +
                "\nfrom=" + Arrays.toString(from) +
                "\nsubject=" + subject +
                "\nreceivedDate=" + receivedDate +
                "\nplainText=" + plainText +
                "\n}";
    }
}
