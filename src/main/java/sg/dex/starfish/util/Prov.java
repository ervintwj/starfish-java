package sg.dex.starfish.util;

import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.Namespace;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Type;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.IndexedDocument;
import org.openprovenance.prov.model.StatementOrBundle;
import org.openprovenance.prov.model.ProvUtilities;
import org.openprovenance.prov.model.WasGeneratedBy;
import org.openprovenance.prov.model.WasAttributedTo;
import org.openprovenance.prov.model.WasAssociatedWith;
import org.openprovenance.prov.model.WasDerivedFrom;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.json.JSONConstructor;

import java.util.List;
import java.io.OutputStream;
import java.util.ArrayList;

public class Prov{

    public static ProvFactory iof = InteropFramework.newXMLProvFactory();

    /*
     * Write provenance info to an output stream
     */
    public static void writeProvenance(OutputStream outputStream,
                                Document doc){
        InteropFramework ifr = new InteropFramework();
        ifr.writeDocument(outputStream,InteropFramework.ProvFormat.JSON,
                          doc);
    }

    public static Namespace defaultNamespace(){
        Namespace ins = new Namespace();
        ins.addKnownNamespaces();
        ins.register("ocn", "http://oceanprotocol.com/schemas");
        return ins;
    }

    public static Namespace defaultNS=defaultNamespace();

    public static List<Entity> createInputs( List<String> inputAssets){

        List<Entity> ret=new ArrayList<Entity>();
        for (String i:inputAssets){
            Entity e=iof.newEntity(defaultNS.qualifiedName("ocn", i, iof));
            e.getType().add(iof.newType("ocn:asset",defaultNS.qualifiedName("xsd", "string", iof)));
            ret.add(e);
        }
        return ret;
    }

    public static Activity createActivity(){

        Activity ret=iof.newActivity(defaultNS.qualifiedName("ocn", "randstring",iof));
        ret.setEndTime(iof.newTimeNow());
        ret.getType().add(iof.newType("ocn:operation",defaultNS.qualifiedName("xsd", "string", iof)));
        ret.getOther().add(iof.newOther(defaultNS.qualifiedName("ocn", "results", iof), "longstringofresults",
        defaultNS.qualifiedName("xsd", "string", iof)));
                ret.getOther().add(iof.newOther(defaultNS.qualifiedName("ocn", "params", iof), "longstringofparams",
        defaultNS.qualifiedName("xsd", "string", iof)));
        return ret;
    }

    public static Agent createAgent(String agentId){
        Agent e=iof.newAgent(defaultNS.qualifiedName("ocn", agentId, iof));
        e.getType().add(iof.newType("ocn:ethereum-account",defaultNS.qualifiedName("xsd", "string", iof)));
        return e;
    }

    public static WasGeneratedBy getGeneratedBy( Entity e , Activity a){
        WasGeneratedBy gb=iof.newWasGeneratedBy(null, e.getId(),a.getId());
        return gb;
    }

    public static WasAssociatedWith getAssociatedWith(  Activity a, Agent ag){
        WasAssociatedWith aw=iof.newWasAssociatedWith(null, a.getId(),ag.getId());
        return aw;
    }

    public static List<WasDerivedFrom> wasDerivedFrom(List<Entity> from, Entity to){

        List<WasDerivedFrom> ret=new ArrayList<WasDerivedFrom>();
        for(Entity e:from){
            
            WasDerivedFrom wdf=iof.newWasDerivedFrom(to.getId(),e.getId());
            ret.add(wdf);
        }
        return ret;
    }

}
