import java.net.*;
import java.util.*;
import org.snmp4j.*;
import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.transport.*;
import org.snmp4j.smi.*;

public class SnmpclientSample{
    static int defaultPort = 4161;
    static String defaultIP = "192.168.0.43";
    static String defaultOID = ".1.3.6.1.2.1.1.7.0";

    static void testGetNext(String oid) throws java.io.IOException {
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
            for( int i = 0; i < variableBindings.size(); i++){
                System.out.println(variableBindings.get(i));
            }
        }
        snmp.close();
    }

    public static void main(String[] args) throws java.io.IOException {
        // get the SNMP port number
        if (args.length > 0) {
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
        System.out.println ("OID : " + defaultOID);
        try {
            testGetNext(defaultOID);
        } catch (Exception ex) {
            System.out.println ("ex *** : " + ex);
            ex.printStackTrace ();
        }
    }
}