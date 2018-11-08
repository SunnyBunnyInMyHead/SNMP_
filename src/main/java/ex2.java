import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableListener;
import org.snmp4j.util.TableUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class ex2 {
    static int defaultPort = 4161;
    static String defaultIP = "192.168.0.43";
    static String defaultOID = ".1.3.6.1.4.1.4976.13.1.0";
    static String defaultTableOID = "1.3.6.1.4.1.4976.13.2.1";


    public static void main(String[] args) throws IOException {
       // syncGet();
       // syncGetTable()
       // asyncGet();
       // asyncGetTable();
    }

    private static void asyncGet() {
        TransportMapping transport = null;
        MessageDispatcher disp;
        CommunityTarget target = new CommunityTarget();
        UdpAddress targetAddress = new UdpAddress();
        Snmp snmp = null;


        try {
            transport = new DefaultUdpTransportMapping();
        } catch (IOException e) {
            e.printStackTrace();
        }
        disp = new MessageDispatcherImpl();
        disp.addMessageProcessingModel(new MPv2c());
        try {
            snmp = new Snmp(disp, transport);
            // snmp =
            snmp.listen();
            targetAddress.setInetAddress(InetAddress.getByName(defaultIP));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        targetAddress.setPort(defaultPort);
        target.setAddress(targetAddress);
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(4000);
        target.setRetries(1);


        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(defaultOID)));
        pdu.setType(PDU.GET);


        MyResponseListener listener = new MyResponseListener();
        try {
            snmp.send(pdu, target, null, listener);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int i=0;
        while (!listener.isReady()) {

        }
        i=0;
    }

    private static void asyncGetTable() {
        TransportMapping transport = null;
        MessageDispatcher disp;
        UdpAddress targetAddress = new UdpAddress();
        Snmp snmp = null;


        try {
            transport = new DefaultUdpTransportMapping();
        } catch (IOException e) {
            e.printStackTrace();
        }
        disp = new MessageDispatcherImpl();
        disp.addMessageProcessingModel(new MPv2c());
        try {
            snmp = new Snmp(disp, transport);
            snmp.listen();
            targetAddress.setInetAddress(InetAddress.getByName(defaultIP));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        targetAddress.setPort(defaultPort);

        TableUtils walker = new TableUtils(snmp, new DefaultPDUFactory());


        CommunityTarget target = new CommunityTarget();
        target.setAddress(targetAddress);
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(3000);
        target.setRetries(3);


        TableListener tableListener = new MyTableListener();

        // Implements snmp4j API
        // @SuppressWarnings("unchecked")
        //List results = walker.getTable(target, new OID[]{new OID(defaultTableOID)}, null ,null);
        walker.getTable(target, new OID[]{new OID(defaultTableOID)}, tableListener, null, null, null);

        int i = 0;
        while (!tableListener.isFinished()) {

        }
        i = 0;

    }

    private static void syncGet() throws IOException {
        TransportMapping transport = null;
        MessageDispatcher disp;
        CommunityTarget target = new CommunityTarget();
        UdpAddress targetAddress = new UdpAddress();
        Snmp snmp = null;


        try {
            transport = new DefaultUdpTransportMapping();
        } catch (IOException e) {
            e.printStackTrace();
        }
        disp = new MessageDispatcherImpl();
        disp.addMessageProcessingModel(new MPv2c());
        try {
            snmp = new Snmp(disp, transport);
            // snmp =
            snmp.listen();
            targetAddress.setInetAddress(InetAddress.getByName(defaultIP));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        targetAddress.setPort(defaultPort);
        target.setAddress(targetAddress);
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(4000);
        target.setRetries(1);


        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(defaultOID)));
        pdu.setType(PDU.GET);

        snmp.listen();
        ResponseEvent response = snmp.send(pdu, target);
        if (response.getResponse() == null) {
            System.out.println("Error: There is some problems.");
        } else {
            Vector variableBindings = response.getResponse().getVariableBindings();
            for (int i = 0; i < variableBindings.size(); i++) {
                System.out.println(variableBindings.get(i));
            }
        }
        snmp.close();
    }

    private static class MyTableListener implements TableListener{

        private LinkedList<TableEvent> rows = new LinkedList();
        private boolean finished;

        public boolean next(TableEvent tableEvent) {
            rows.add(tableEvent);
            return true;
        }

        public void finished(TableEvent tableEvent) {
            finished = true;
        }

        public boolean isFinished() {
            return finished;
        }

        public List<TableEvent> getRows() {
            return rows;
        }
    }

    private static class MyResponseListener implements  ResponseListener{
        private boolean ready;
        private ResponseEvent response;
        public void onResponse(ResponseEvent event) {
            // Always cancel async request when response has been received
            // otherwise a memory leak is created! Not canceling a request
            // immediately can be useful when sending a request to a broadcast
            // address.
            ((Snmp) event.getSource()).cancel(event.getRequest(), this);
            PDU response = event.getResponse();
            PDU request = event.getRequest();
            if (response == null) {
                System.out.println("Request " + request + " timed out");
            } else {
                System.out.println("Received response " + response + " on request " +
                        request);
                this.response = event;
            }

            ready=true;
        }

        public boolean isReady() {
            return ready;
        }

        public ResponseEvent getResponse() {
            return response;
        }
    }

    private static void syncGetTable(){
        TransportMapping transport = null;
        MessageDispatcher disp;
        UdpAddress targetAddress = new UdpAddress();
        Snmp snmp = null;


        try {
            transport = new DefaultUdpTransportMapping();
        } catch (IOException e) {
            e.printStackTrace();
        }
        disp = new MessageDispatcherImpl();
        disp.addMessageProcessingModel(new MPv2c());
        try {
            snmp = new Snmp(disp, transport);
            snmp.listen();
            targetAddress.setInetAddress(InetAddress.getByName(defaultIP));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        targetAddress.setPort(defaultPort);

        TableUtils walker = new TableUtils(snmp, new DefaultPDUFactory());


        CommunityTarget target = new CommunityTarget();
        target.setAddress(targetAddress);
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(3000);
        target.setRetries(3);
        long time = System.currentTimeMillis();
        // Implements snmp4j API
        @SuppressWarnings("unchecked")
        List<TableEvent> results = walker.getTable(target, new OID[]{new OID(defaultTableOID)}, null ,null);
        time = System.currentTimeMillis()-time;

        System.out.println(results.size());
    }
}
