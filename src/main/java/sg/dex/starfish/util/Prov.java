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
import java.util.UUID;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
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

    /*
     * Create a default namespace which includes the prefix and path for Oceanprotocol schema
     */
    public static Namespace defaultNamespace(){
        Namespace ins = new Namespace();
        ins.addKnownNamespaces();
        ins.register("ocn", "http://oceanprotocol.com/schemas");
        return ins;
    }

    public static Namespace defaultNS=defaultNamespace();

    /*
     * Given a list of input assets identified by the DID or AssetId,
     * return a list of Entities where the type is ocn:asset
     *
     */
    public static List<Entity> createInputs( List<String> inputAssets){

        List<Entity> ret=new ArrayList<Entity>();
        for (String i:inputAssets){
            Entity e=iof.newEntity(defaultNS.qualifiedName("ocn", i, iof));
            e.getType().add(iof.newType("ocn:asset",defaultNS.qualifiedName("xsd", "string", iof)));
            ret.add(e);
        }
        return ret;
    }

    /**
     * Returns an activity of type 'publish' identified by localname (and prefix is 'ocn')
     *
     */
    public static Activity createPublishActivity(String localname){

        Activity ret=iof.newActivity(defaultNS.qualifiedName("ocn", localname,iof));
        ret.setEndTime(iof.newTimeNow());
        ret.getType().add(iof.newType("ocn:publish",defaultNS.qualifiedName("xsd", "string", iof)));
        return ret;
    }

    /**
     * Returns an activity of type 'operation', given localname, input params and output results.
     */
    public static Activity createInvokeActivity(String localname, String params,String results){

        Activity ret=iof.newActivity(defaultNS.qualifiedName("ocn", localname,iof));
        ret.setEndTime(iof.newTimeNow());
        ret.getType().add(iof.newType("ocn:operation",defaultNS.qualifiedName("xsd", "string", iof)));
        ret.getOther().add(iof.newOther(defaultNS.qualifiedName("ocn", "results", iof),results ,
        defaultNS.qualifiedName("xsd", "string", iof)));
                ret.getOther().add(iof.newOther(defaultNS.qualifiedName("ocn", "params", iof),
                                                params,
        defaultNS.qualifiedName("xsd", "string", iof)));
        return ret;
    }

    /*
     * Returns an Agent given an agentId (identified by ethereum account)
     */
    public static Agent createAgent(String agentId){
        Agent e=iof.newAgent(defaultNS.qualifiedName("ocn", agentId, iof));
        e.getType().add(iof.newType("ocn:ethereum-account",defaultNS.qualifiedName("xsd", "string", iof)));
        return e;
    }

    /*
     * Returns a wasGeneratedBy attribute, given an Entity and Activity it was generated by.
     */
    public static WasGeneratedBy getGeneratedBy( Entity e , Activity a){
        WasGeneratedBy gb=iof.newWasGeneratedBy(null, e.getId(),a.getId());
        return gb;
    }

    /* Returns a wasAssociatedWith attribute, given an Agent that performed an Activity
     */
    public static WasAssociatedWith getAssociatedWith(  Activity a, Agent ag){
        WasAssociatedWith aw=iof.newWasAssociatedWith(null, a.getId(),ag.getId());
        return aw;
    }

    /*
     * Returns a List of wasDerivedFrom attributes, given a list of source entities, and the
     * destination entity (which was derived from the source entity)
     */
    public static List<WasDerivedFrom> wasDerivedFrom(List<Entity> from, Entity to){

        List<WasDerivedFrom> ret=new ArrayList<WasDerivedFrom>();
        for(Entity e:from){
            
            WasDerivedFrom wdf=iof.newWasDerivedFrom(to.getId(),e.getId());
            ret.add(wdf);
        }
        return ret;
    }

    /*
     * Returns a JSON encoded String containing metadata for publishing
     */
    public static String publishMetadata(String agentid){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        Document d=iof.newDocument();
        d.setNamespace(defaultNS);
        Agent ag=createAgent(agentid);
        d.getStatementOrBundle().add(ag);
        List<String> assets=new ArrayList<String>();
        assets.add("this");

        List<Entity> la=createInputs(assets);
        d.getStatementOrBundle().addAll(la);

        Activity a= createPublishActivity(UUID.randomUUID().toString());
        d.getStatementOrBundle().add(a);
        d.getStatementOrBundle().add(getGeneratedBy(la.get(0),a));

        d.getStatementOrBundle().add(getAssociatedWith(a,ag));

        InteropFramework iof=new InteropFramework();
        iof.writeDocument(baos, InteropFramework.ProvFormat.JSON ,d);
        return baos.toString();
    }

    /*
     * Creates Invoke Metadata
     */
    public static String invokeMetadata(String agentid,
                                        List<String> assetDependencies,
                                        String params,
                                        String results){
        Document d=Prov.iof.newDocument();
        d.setNamespace(Prov.defaultNS);
        Agent ag=Prov.createAgent(agentid);
        d.getStatementOrBundle().add(ag);
        List<String> assets=new ArrayList<String>();
        assets.add("this");
        for(String s:assetDependencies){
            assets.add(s); 
        }
        List<Entity> la=Prov.createInputs(assets);
        d.getStatementOrBundle().addAll(la);
        Activity a= Prov.createInvokeActivity(UUID.randomUUID().toString(),params,results);
        d.getStatementOrBundle().add(a);
        d.getStatementOrBundle().add(Prov.getGeneratedBy(la.get(0),a));

        List<Entity> la2=new ArrayList<Entity>();
        la2.add(la.get(1));
        List<WasDerivedFrom> wdflist = Prov.wasDerivedFrom(la2,la.get(0));
        d.getStatementOrBundle().add(Prov.getAssociatedWith(a,ag));

        d.getStatementOrBundle().addAll(wdflist);
        InteropFramework iof=new InteropFramework();

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        iof.writeDocument(baos, InteropFramework.ProvFormat.JSON ,d);
        return baos.toString();
    }
}
