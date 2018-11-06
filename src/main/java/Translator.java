import org.snmp4j.smi.*;

/**
 * Created by Bizon on 01.11.2018.
 */
public class Translator {

    public static int translate(Integer32 data){
        return data.getValue();
    }

    public static long translate(UnsignedInteger32 data){
        return data.getValue();
    }

    public static long translate(Counter64 data){
        return data.getValue();
    }
    public static long translate(Gauge32 data){
        return data.getValue();
    }
    public static long translate(Counter32 data){
        return data.getValue();
    }
    public static long translate(TimeTicks data){
        return data.getValue();
    }
    public static byte[] translate(OctetString data){
        return data.getValue();
    }
    public static byte[] translate(Opaque data){
        return data.getValue();
    }
}
