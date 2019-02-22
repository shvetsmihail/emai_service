package mail.model;


public class CertOrderEmailEntity {
    private String orderId;
    private String cn;
    private String cert;
    private String status;

    public CertOrderEmailEntity() {
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCn() {
        return cn;
    }

    public String getCert() {
        return cert;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "CertOrderEmailEntity{" +
                "orderId='" + orderId + '\'' +
                ", cn='" + cn + '\'' +
                ", cert='" + cert + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
