package sg.dex.starfish.util;

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

import java.util.List;
import java.util.ArrayList;

public class Prov{

    /*
     * Write provenance info to an output stream
     */
    public void writeProvenance(OutputStream outputStream,
                                Document doc){
        InteropFramework ifr = new InteropFramework;
        ifr.writeDocument(outputStream,InteropFramework.ProvFormat/JSON,
                          doc);
    }

    public List<Entity> createInputs(List<String> inputAssets){
        InteropFramework iof = InteropFramework.newXMLProvFactory();
    }
}
