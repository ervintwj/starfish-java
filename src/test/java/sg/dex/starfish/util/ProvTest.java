package sg.dex.starfish.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import java.io.FileOutputStream;

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
import java.util.ArrayList;

public class ProvTest {

    @Test public void testProvWrite() {
        Document d=Prov.iof.newDocument();
        d.setNamespace(Prov.defaultNS);
        Agent ag=Prov.createAgent("503a7b959f91ac691a0881ee724635427ea5f3862aa105040e30a0fee50cc1a00");
        d.getStatementOrBundle().add(ag);
        List<String> assets=new ArrayList<String>();
        assets.add("this");
        assets.add("26cb1a92e8a6b52e47e6e13d04221e9b005f70019e21c4586dad3810d46220135/26cb1a92e8a6b52e47e6e13d04221e9b005f70019e21c4586dad3810d46220136");
        List<Entity> la=Prov.createInputs(assets);
        d.getStatementOrBundle().addAll(la);
        Activity a= Prov.createActivity();
        d.getStatementOrBundle().add(a);
        d.getStatementOrBundle().add(Prov.getGeneratedBy(la.get(0),a));

        List<Entity> la2=new ArrayList<Entity>();
        la2.add(la.get(1));
        List<WasDerivedFrom> wdflist = Prov.wasDerivedFrom(la2,la.get(0));

        d.getStatementOrBundle().add(Prov.getAssociatedWith(a,ag));

        d.getStatementOrBundle().addAll(wdflist);
        InteropFramework iof=new InteropFramework();
        iof.writeDocument("/tmp/abcd.json", InteropFramework.ProvFormat.JSON ,d);

    }
}
