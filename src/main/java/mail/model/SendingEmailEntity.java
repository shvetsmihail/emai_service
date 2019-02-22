package mail.model;

public class SendingEmailEntity {
    private String[] emailTo;
    private String subject;
    private String plainText;
    private String filePath;

    public SendingEmailEntity(String[] emailTo, String subject, String plainText, String filePath) {
        this.emailTo = emailTo;
        this.subject = subject;
        this.plainText = plainText;
        this.filePath = filePath;
    }

    public String[] getEmailTo() {
        return emailTo;
    }

    public String getSubject() {
        return subject;
    }

    public String getPlainText() {
        return plainText;
    }

    public String getFilePath() {
        return filePath;
    }
}
