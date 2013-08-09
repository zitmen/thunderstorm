package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import ij.IJ;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
    static final String UNITS = "units";
    
    @Override
    public void importFromFile(String fp, IJResultsTable rt) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        //
        // 1. Read the XML file into the hashmap
        HashMap<String,String> units = null;
        boolean filling_units = false;
        ArrayList<HashMap<String,Double>> molecules = null;
        HashMap<String,Double> molecule = null;
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
                        molecules = new ArrayList<HashMap<String,Double>>();
                        continue;
                    }
                    
                    // the units element?
                    if (startElement.getName().getLocalPart().equals(UNITS)) {
                        units = new HashMap<String, String>();
                        filling_units = true;
                        continue;
                    }
                    
                    // an item element?
                    if (startElement.getName().getLocalPart().equals(ITEM)) {
                        molecule = new HashMap<String, Double>();
                        continue;
                    }

                    // fill a new item
                    if (event.isStartElement()) {
                        String name = event.asStartElement().getName().getLocalPart();
                        String value = eventReader.nextEvent().asCharacters().getData();
                        if(filling_units) { // units
                            units.put(name, value);
                        } else {    // molecule
                            molecule.put(name, Double.parseDouble(value));
                        }
                        continue;
                    }
                } else if(event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    
                    // the units element?
                    if (endElement.getName().getLocalPart().equals(UNITS)) {
                        filling_units = false;
                        continue;
                    }
                    
                    // an item element?
                    if (endElement.getName().getLocalPart().equals(ITEM)) {
                        molecules.add(molecule);
                        continue;
                    }
                }
            }
        } catch (XMLStreamException ex) {
            throw new IOException(ex.toString());
        }
        
        //
        // 2. Fill the table by values from the hashmap
        if(molecules != null) {
            String [] headers = new String[1];
            int r = 0, nrows = molecules.size();
            for(HashMap<String,Double> mol : molecules) {
                if(mol.size() != headers.length)
                    headers = new String[mol.size()];
                mol.keySet().toArray(headers);
                if(!rt.columnNamesEqual(headers)) {
                    throw new IOException("Labels in the file do not correspond to the header of the table (excluding '" + IJResultsTable.COLUMN_ID + "')!");
                }
                //
                rt.addRow();
                for(Map.Entry<String,Double> entry : mol.entrySet()) {
                    if(IJResultsTable.COLUMN_ID.equals(entry.getKey())) continue;
                    rt.addValue(entry.getValue().doubleValue(), entry.getKey());
                    IJ.showProgress((double)(r++) / (double)nrows);
                }
            }
            if(units != null) {
                for(Entry<String,String> col : units.entrySet()) {
                    rt.setColumnUnits(col.getKey(), col.getValue());
                }
            }
        }
        rt.copyOriginalToActual();
        rt.setActualState();
    }

    @Override
    public void exportToFile(String fp, IJResultsTable rt, Vector<String> columns) throws IOException {
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
            
            // Write columns headers with units
            StartElement unitsStartElement = eventFactory.createStartElement("", "", UNITS);
            eventWriter.add(tab);
            eventWriter.add(unitsStartElement);
            eventWriter.add(end);
            for(int c = 0; c < ncols; c++) {
                String units = rt.getColumnUnits(columns.elementAt(c));
                if((units != null) && !units.trim().isEmpty()) {
                    createNode(eventWriter, columns.elementAt(c), units);
                }
            }
            eventWriter.add(tab);
            eventWriter.add(eventFactory.createEndElement("", "", UNITS));
            eventWriter.add(end);
            
            // Write molecules
            for(int r = 0; r < nrows; r++) {
                StartElement moleculeStartElement = eventFactory.createStartElement("", "", ITEM);
                eventWriter.add(tab);
                eventWriter.add(moleculeStartElement);
                eventWriter.add(end);

                for(int c = 0; c < ncols; c++) {
                    createNode(eventWriter, columns.elementAt(c), Double.toString((Double)rt.getValue(r,columns.elementAt(c))));
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

    @Override
    public String getSuffix() {
        return "xml";
    }

}
