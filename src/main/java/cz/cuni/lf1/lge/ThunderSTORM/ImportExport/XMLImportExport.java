package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import ij.IJ;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
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
    public void importFromFile(String fp, GenericTable table, int startingFrame) throws IOException {
        assert(table != null);
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
            double [] values = null;
            String [] colnames = new String[1];
            int r = 0, nrows = molecules.size();
            for(HashMap<String,Double> mol : molecules) {
                if(mol.size() != colnames.length) {
                    if(mol.containsKey(MoleculeDescriptor.LABEL_ID)) {
                        colnames = new String[mol.size()-1];
                    } else {
                        colnames = new String[mol.size()-1];
                    }
                }
                int ci = 0;
                for(String key : mol.keySet()) {
                    if(MoleculeDescriptor.LABEL_ID.equals(key)) continue;
                    colnames[ci] = key;
                    ci++;
                }
                if(!table.columnNamesEqual(colnames)) {
                    throw new IOException("Labels in the file do not correspond to the header of the table (excluding '" + MoleculeDescriptor.LABEL_ID + "')!");
                }
                if(table.isEmpty()) {
                    table.setDescriptor(new MoleculeDescriptor(colnames));
                    if(units != null) {
                        for(Entry<String,String> col : units.entrySet()) {
                            table.setColumnUnits(col.getKey(), Units.fromString(col.getValue()));
                        }
                    }
                }
                if(values == null) {
                    values = new double[colnames.length];
                }
                //
                for(int c = 0; c < colnames.length; c++) {
                    if(MoleculeDescriptor.LABEL_ID.equals(colnames[c])) continue;
                    values[c] = mol.get(colnames[c]).doubleValue();
                    if(MoleculeDescriptor.LABEL_FRAME.equals(colnames[c])) {
                        values[c] += startingFrame-1;
                    }
                    IJ.showProgress((double)(r++) / (double)nrows);
                }
                table.addRow(values);
            }
        }
        table.insertIdColumn();
        table.copyOriginalToActual();
        table.setActualState();
    }

    @Override
    public void exportToFile(String fp, int floatPrecision, GenericTable table, List<String> columns) throws IOException {
        assert(table != null);
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
            
            int ncols = columns.size(), nrows = table.getRowCount();
            
            // Write columns headers with units
            StartElement unitsStartElement = eventFactory.createStartElement("", "", UNITS);
            eventWriter.add(tab);
            eventWriter.add(unitsStartElement);
            eventWriter.add(end);
            for(int c = 0; c < ncols; c++) {
                String units = table.getColumnUnits(columns.get(c)).toString();
                if((units != null) && !units.trim().isEmpty()) {
                    createNode(eventWriter, columns.get(c), units);
                }
            }
            eventWriter.add(tab);
            eventWriter.add(eventFactory.createEndElement("", "", UNITS));
            eventWriter.add(end);

            DecimalFormat df = new DecimalFormat();
            df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
            df.setRoundingMode(RoundingMode.HALF_EVEN);
            df.setMaximumFractionDigits(floatPrecision);

            // Write molecules
            for(int r = 0; r < nrows; r++) {
                StartElement moleculeStartElement = eventFactory.createStartElement("", "", ITEM);
                eventWriter.add(tab);
                eventWriter.add(moleculeStartElement);
                eventWriter.add(end);

                for(int c = 0; c < ncols; c++) {
                    createNode(eventWriter, columns.get(c), df.format(table.getValue(r,columns.get(c))));
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
