package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import ij.IJ;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XMLImportExport implements IImportExport {
    
    static final String ROOT = "results";
    static final String ITEM = "molecule";
    
    @Override
    public void importFromFile(String fp, IJResultsTable rt) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        rt.reset();
        
        try {
            // First create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            // Setup a new eventReader
            InputStream in = new FileInputStream(fp);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            
            while(eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if(event.isStartElement()) {
                    StartElement startElement = event.asStartElement();

                    // the root element?
                    if (startElement.getName().getLocalPart().equals(ROOT)) {
                        // skip over it
                        continue;
                    }
                    
                    // an item element?
                    if (startElement.getName().getLocalPart().equals(ITEM)) {
                        rt.addRow();
                        continue;
                    }

                    // fill a new item
                    if (event.isStartElement()) {
                        String name = event.asStartElement().getName().getLocalPart();
                        String value = eventReader.nextEvent().asCharacters().getData();
                        if(!IJResultsTable.COLUMN_ID.equals(name)) {
                            rt.addValue(name, Double.parseDouble(value));
                        }
                        continue;
                    }
                }
            }
        } catch (XMLStreamException ex) {
            throw new IOException(ex.toString());
        }
    }

    @Override
    public void exportToFile(String fp, IJResultsTable.View rt, Vector<String> columns) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        // Create a XMLOutputFactory
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        // Create XMLEventWriter
        XMLEventWriter eventWriter;
        try {
            eventWriter = outputFactory.createXMLEventWriter(new FileOutputStream(fp));
            
            // Create a EventFactory
            XMLEventFactory eventFactory = XMLEventFactory.newInstance();
            XMLEvent tab = eventFactory.createDTD("\t");
            XMLEvent end = eventFactory.createDTD("\n");
            // Create and write Start Tag
            StartDocument startDocument = eventFactory.createStartDocument();
            eventWriter.add(startDocument);
            eventWriter.add(end);

            // Create open tag of the root element
            StartElement resultsStartElement = eventFactory.createStartElement("", "", ROOT);
            eventWriter.add(resultsStartElement);
            eventWriter.add(end);
            
            int ncols = columns.size(), nrows = rt.getRowCount();
            String [] headers = new String[ncols];
            columns.toArray(headers);
            
            for(int r = 0; r < nrows; r++) {
                StartElement moleculeStartElement = eventFactory.createStartElement("", "", ITEM);
                eventWriter.add(tab);
                eventWriter.add(moleculeStartElement);
                eventWriter.add(end);

                for(int c = 0; c < ncols; c++) {
                    createNode(eventWriter, headers[c], Double.toString(rt.getValue(headers[c],r)));
                }
                
                eventWriter.add(tab);
                eventWriter.add(eventFactory.createEndElement("", "", ITEM));
                eventWriter.add(end);
                IJ.showProgress((double)r / (double)nrows);
            }

            // Close the root element
            eventWriter.add(eventFactory.createEndElement("", "", ROOT));
            eventWriter.add(end);
            
            eventWriter.add(eventFactory.createEndDocument());
            eventWriter.close();
        } catch(XMLStreamException ex) {
            throw new IOException(ex.toString());
        }
    }
    
    private void createNode(XMLEventWriter eventWriter, String name, String value) throws XMLStreamException {
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent end = eventFactory.createDTD("\n");
        XMLEvent tab = eventFactory.createDTD("\t\t");
        // Create Start node
        StartElement sElement = eventFactory.createStartElement("", "", name);
        eventWriter.add(tab);
        eventWriter.add(sElement);
        // Create Content
        Characters characters = eventFactory.createCharacters(value);
        eventWriter.add(characters);
        // Create End node
        EndElement eElement = eventFactory.createEndElement("", "", name);
        eventWriter.add(eElement);
        eventWriter.add(end);
    }

    @Override
    public String getName() {
        return "XML";
    }

}
