package mail.parser;

import mail.model.CertOrderEmailEntity;
import mail.model.ReceivedEmailEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CertOrderEmailParser {
    public static final String STATUS_RECEIVED = "RECEIVED";
    public static final String STATUS_COMPLETE = "COMPLETE";

    private static final String ORDER_ID_START_LINE = "Номер заказа:";
    private static final String CN_START_LINE = "FQDN (домен):";
    private static final String CERT_START_LINE = "-----BEGIN CERTIFICATE-----";
    private static final String CERT_STOP_LINE = "-----END CERTIFICATE-----";



    public CertOrderEmailEntity parse(ReceivedEmailEntity message) {
        CertOrderEmailEntity order = new CertOrderEmailEntity();

        String subject = message.getSubject().toLowerCase();
        if (subject.contains(STATUS_RECEIVED.toLowerCase())) {
            order.setStatus(STATUS_RECEIVED);
        } else if (subject.contains(STATUS_COMPLETE.toLowerCase())) {
            order.setStatus(STATUS_COMPLETE);
        } else {
            return null;
        }

        List<String> lines = Stream.of(message.getPlainText().split("\n"))
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toList());

        boolean findCert = false;
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {

            if (order.getStatus().equals(STATUS_COMPLETE)) {
                if (order.getCert() == null && line.startsWith(CERT_START_LINE)) {
                    findCert = true;
                }
                if (findCert) {
                    sb.append(line).append("\n");
                    if (line.startsWith(CERT_STOP_LINE)) {
                        findCert = false;
                        order.setCert(sb.toString());
                    }
                    continue;
                }
            }

            if (order.getOrderId() == null) {
                int index = line.indexOf(ORDER_ID_START_LINE);
                if (index >= 0) {
                    order.setOrderId(line.substring(index + ORDER_ID_START_LINE.length()).trim());
                    continue;
                }
            }

            if (order.getCn() == null) {
                int index = line.indexOf(CN_START_LINE);
                if (index >= 0) {
                    order.setCn(line.substring(index + CN_START_LINE.length()).trim());
                }
            }

            if (order.getOrderId() != null
                    && order.getCn() != null
                    && (order.getCert() != null || order.getStatus().equals(STATUS_RECEIVED))) {
                break;
            }
        }

        return order;
    }
}
