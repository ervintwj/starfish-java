package sg.dex.starfish.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import java.io.FileOutputStream;
import java.io.File;
import java.util.UUID;

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
import org.openprovenance.prov.model.WasAttributedTo;
import org.openprovenance.prov.model.WasDerivedFrom;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.json.JSONConstructor;

import java.util.List;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

public class ProvTest {

    @Test public void testProvInvoke() {
        Document d=Prov.iof.newDocument();
        d.setNamespace(Prov.defaultNS);
        Agent ag=Prov.createAgent("503a7b959f91ac691a0881ee724635427ea5f3862aa105040e30a0fee50cc1a00");
        d.getStatementOrBundle().add(ag);
        List<String> assets=new ArrayList<String>();
        assets.add("this");
        assets.add("26cb1a92e8a6b52e47e6e13d04221e9b005f70019e21c4586dad3810d46220135/26cb1a92e8a6b52e47e6e13d04221e9b005f70019e21c4586dad3810d46220136");
        List<Entity> la=Prov.createInputs(assets);
        d.getStatementOrBundle().addAll(la);

        String par = "{\"params\":{\"to-hash\": {\"did\":\"26cb1a92e8a6b52e47e6e13d04221e9b005f70019e21c4586dad3810d46220135\"}}}";
        String res="{\"results\":{\"hashval\": {\"did\":\"a6cb1a92e8a6b52e47e6e13d04221e9b005f70019e21c4586dad3810d46220135\"}}}";

        Activity a= Prov.createInvokeActivity(UUID.randomUUID().toString(),par,res);
        d.getStatementOrBundle().add(a);
        d.getStatementOrBundle().add(Prov.getGeneratedBy(la.get(0),a));

        List<Entity> la2=new ArrayList<Entity>();
        la2.add(la.get(1));
        List<WasDerivedFrom> wdflist = Prov.wasDerivedFrom(la2,la.get(0));

        d.getStatementOrBundle().add(Prov.getAssociatedWith(a,ag));

        d.getStatementOrBundle().addAll(wdflist);
        InteropFramework iof=new InteropFramework();

        try{
            
            File temp = new File("/tmp/invoke.json");
            iof.writeDocument(temp.getAbsolutePath(), InteropFramework.ProvFormat.JSON ,d);
            assertTrue(temp.length()>0);
        }catch(IOException ioe){
            fail("file has length = 0");
        }
    }

    @Test public void testProvPublish() {
        Document d=Prov.iof.newDocument();
        d.setNamespace(Prov.defaultNS);
        Agent ag=Prov.createAgent("503a7b959f91ac691a0881ee724635427ea5f3862aa105040e30a0fee50cc1a00");
        d.getStatementOrBundle().add(ag);
        List<String> assets=new ArrayList<String>();
        assets.add("this");
        List<Entity> la=Prov.createInputs(assets);
        d.getStatementOrBundle().addAll(la);
        Activity a= Prov.createPublishActivity(UUID.randomUUID().toString());
        d.getStatementOrBundle().add(a);
        d.getStatementOrBundle().add(Prov.getGeneratedBy(la.get(0),a));

        d.getStatementOrBundle().add(Prov.getAssociatedWith(a,ag));

        InteropFramework iof=new InteropFramework();

        try{
            
            File temp = new File("/tmp/publish.json");
            iof.writeDocument(temp.getAbsolutePath(), InteropFramework.ProvFormat.JSON ,d);
            assertTrue(temp.length()>0);
        }catch(IOException ioe){
            fail("file has length = 0");
        }
    }

    @Test public void testProvPublishString() {
        assertNotNull(Prov.publishMetadata("503a7b959f91ac691a0881ee724635427ea5f3862aa105040e30a0fee50cc1a00"));
    }

    @Test public void testInvokeMetaString() {

        List<String> assets=new ArrayList<String>();
        assets.add("26cb1a92e8a6b52e47e6e13d04221e9b005f70019e21c4586dad3810d46220135/26cb1a92e8a6b52e47e6e13d04221e9b005f70019e21c4586dad3810d46220136");
        String par = "{\"params\":{\"to-hash\": {\"did\":\"26cb1a92e8a6b52e47e6e13d04221e9b005f70019e21c4586dad3810d46220135\"}}}";
        String res="{\"results\":{\"hashval\": {\"did\":\"a6cb1a92e8a6b52e47e6e13d04221e9b005f70019e21c4586dad3810d46220135\"}}}";
        assertNotNull(Prov.invokeMetadata("503a7b959f91ac691a0881ee724635427ea5f3862aa105040e30a0fee50cc1a00",
                                          assets,
                                          par,
                                          res));
    }

}
