import java.io.IOException;
import java.net.*;
import java.util.*;

import org.snmp4j.*;
import org.snmp4j.PDU;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.transport.*;
import org.snmp4j.smi.*;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

public class SnmpclientSample {
    static int defaultPort = 4161;
    static String defaultIP = "192.168.0.43";
    static String defaultOID = ".1.3.6.1.4.1.4976.13.2.1";


    static TransportMapping transport;
    static MessageDispatcher disp;
    static CommunityTarget target = new CommunityTarget();
    static UdpAddress targetAddress = new UdpAddress();
    static Snmp snmp;

    static {
        try {
            transport = new DefaultUdpTransportMapping();
        } catch (IOException e) {
            e.printStackTrace();
        }
        disp = new MessageDispatcherImpl();
        disp.addMessageProcessingModel(new MPv2c());
        try {
            snmp = new Snmp(disp, transport);
           // snmp = new Snmp(new DefaultUdpTransportMapping());
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
//        try {
//            snmp.listen();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    static void testGet(String oid) throws java.io.IOException {
        //1. Make Protocol Data Unit
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(defaultOID)));
        pdu.setType(PDU.GET);

        OID custom = new OID();

        //2. Make target
        CommunityTarget target = new CommunityTarget();
        UdpAddress targetAddress = new UdpAddress();
        targetAddress.setInetAddress(InetAddress.getByName(defaultIP));
        targetAddress.setPort(defaultPort);
        target.setAddress(targetAddress);
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version1);

        //3. Make SNMP Message. Simple!
        Snmp snmp = new Snmp(new DefaultUdpTransportMapping());

        //4. Send Message and Recieve Response
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

    public static void main(String[] args) throws java.io.IOException {
        //test1();
        // Holder holder = getTable(1,2,".1.3.6.1.4.1.4976.13.2.1",new int[]{1,2,3,4,5});
        // System.out.println(holder);
        //querySingleSNMPTableByOID(".1.3.6.1.4.1.4976.13.2");
        test2();
       // test3();

    }

    private static void test1() {
        // get the SNMP port number
        /*if (args.length > 0) {
            defaultPort = Integer.parseInt (args[0]);
        }
        System.out.println ("PORT : " + defaultPort);

        // get the ip address of the machine that the SNMP agent runs on
        if (args.length > 1) {
            defaultIP = args[1];
        }
        System.out.println ("IP : " + defaultIP);

        // get the OID number that you want to get the value of
        if (args.length > 2) {
            defaultOID = args[2];
        }
        System.out.println ("OID : " + defaultOID);*/
        try {
            testGet(defaultOID);
        } catch (Exception ex) {
            System.out.println("ex *** : " + ex);
            ex.printStackTrace();
        }
    }

    private static Holder GET(String oid) throws IOException {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);

        OID custom = new OID();

        //2. Make target
        CommunityTarget target = new CommunityTarget();
        UdpAddress targetAddress = new UdpAddress();
        targetAddress.setInetAddress(InetAddress.getByName(defaultIP));
        targetAddress.setPort(defaultPort);
        target.setAddress(targetAddress);
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version1);

        //3. Make SNMP Message. Simple!
        Snmp snmp = new Snmp(new DefaultUdpTransportMapping());

        //4. Send Message and Recieve Response
        snmp.listen();
        ResponseEvent response = snmp.send(pdu, target);
        if (response.getResponse() == null) {
            System.out.println("Error: There is some problems.");
            snmp.close();
            return null;
        } else {
            Vector variableBindings = response.getResponse().getVariableBindings();
            for (int i = 0; i < variableBindings.size(); i++) {
                System.out.println(variableBindings.get(i));
            }
        }
        snmp.close();
        VariableBinding v = response.getResponse().getVariableBindings().get(0);
        return new Holder(getOID(v.getOid().getValue()), v.getVariable());
    }

    static class Holder {
        private String oid;
        private Variable value;
        final List<Holder> list;

        Holder(String oid, Variable value) {
            this.oid = oid;
            this.value = value;
            list = new ArrayList<Holder>();
        }

        void add(Holder obj) {
            list.add(obj);
        }

        int size() {
            return list.size();
        }

        Holder get(int holderNumb) {
            return list.get(holderNumb);
        }

        public String getOid() {
            return oid;
        }

        public Variable getValue() {
            return value;
        }
    }

    //rowIdS>=1!!!
    private static Holder getTable(int rowIdS, int rowIdE, String oid, int[] columnId) {
        Holder holder = new Holder(oid, null);
        Holder inner;
        for (int i = rowIdS, k = 0; i <= rowIdE; i++, k++) {
            holder.add(new Holder(oid + "." + i, null));
            for (int j = 1; j < columnId.length; j++) {
                inner = null;
                try {
                    inner = GET(oid + "." + columnId[j] + "." + i);
                } catch (IOException e) {
                    //e.printStackTrace();
                }
                holder.get(k).add(inner);
            }
        }


        return holder;
    }

    private static String getOID(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append('.');
            sb.append(arr[i]);
        }
        return sb.toString();
    }


    public static List<SNMPClient.SNMPTriple> querySingleSNMPTableByOID(String oid) throws IOException {
        Snmp snmp = new Snmp(new DefaultUdpTransportMapping());

        CommunityTarget target = new CommunityTarget();
        UdpAddress targetAddress = new UdpAddress();
        targetAddress.setInetAddress(InetAddress.getByName(defaultIP));
        targetAddress.setPort(defaultPort);
        target.setAddress(targetAddress);
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version1);

        if (oid == null || oid.isEmpty())
            return null;
        if (!oid.startsWith("."))
            oid = "." + oid;
        TableUtils tUtils = new TableUtils(snmp, new DefaultPDUFactory());
        snmp.listen();
        List<TableEvent> events = tUtils.getTable(target, new OID[]{new OID(oid)}, null, null);
        // List<SNMPClient.SNMPTriple> snmpList = new ArrayList<SNMPClient.SNMPTriple>();
        for (TableEvent event : events) {
            /*if (event.isError()) {
                logger.warning("SNMP event error: " + event.getErrorMessage());
                continue;
                //throw new RuntimeException(event.getErrorMessage());
            }
            for (VariableBinding vb : event.getColumns()) {
                String key = vb.getOid().toString();
                String value = vb.getVariable().toString();
                snmpList.add(new SNMPClient.SNMPTriple(key, "", value));
            }*/
        }
        return null;
    }

    private static void test2() throws IOException {


       /* PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(defaultOID)));
        pdu.setType(PDU.GET);*/

        TableUtils walker = new TableUtils(snmp, new DefaultPDUFactory());
        snmp.listen();

        CommunityTarget target = new CommunityTarget();
        UdpAddress targetAddress = new UdpAddress();
        targetAddress.setInetAddress(InetAddress.getByName(defaultIP));
        targetAddress.setPort(defaultPort);
        target.setAddress(targetAddress);
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(3000);
        target.setRetries(3);
        long time = System.currentTimeMillis();
        // Implements snmp4j API
        @SuppressWarnings("unchecked")
        List results = walker.getTable(target, new OID[]{new OID(defaultOID)}, null ,null);
        time = System.currentTimeMillis()-time;

        TableEvent lastEvent = (TableEvent) results.get(results.size() - 1);


    }

    private static TableUtils getTableUtils() {
        return new TableUtils(snmp, new DefaultPDUFactory());
    }

    public static List<TableEvent> getTable(OID oids[]) {
        TableUtils tableUtils = getTableUtils();
        return tableUtils.getTable(target, oids, null, null);
    }

    private static void test3() {
        OID oid1 = new OID(".1.3.6.1.4.1.4976.13.2.1"),
                oid2 = new OID(".1.3.6.1.4.1.4976.13.2.1.3"),
                oid3 = new OID(".1.3.6.1.4.1.4976.13.2.1.4"),
                oid4 = new OID(".1.3.6.1.4.1.4976.13.2.1.5");
        List<TableEvent> list = getTable(new OID[]{oid1});
        for (TableEvent event : list) {
            if(event==null||event.getColumns()==null){
                continue;
            }
            for (VariableBinding vb : event.getColumns()) {
                System.out.print(vb.getVariable());
            }

        }
    }


}