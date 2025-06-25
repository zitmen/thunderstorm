package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.StringFormatting;
import ij.IJ;

import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;

public class XMLImportExport implements IImportExport {

    static final String ROOT = "results";
    static final String ITEM = "molecule";
    static final String UNITS = "units";

    @Override
    public void importFromFile(String fp, GenericTable table, int startingFrame) throws IOException {
        assert (table != null);
        assert (fp != null && !fp.isEmpty());

        HashMap<String, String> units = null;
        boolean filling_units = false;
        ArrayList<HashMap<String, Double>> molecules = null;
        HashMap<String, Double> molecule = null;
        DecimalFormat df = StringFormatting.getDecimalFormat();
        boolean skipMolecule = false;

        try (InputStream in = new FileInputStream(fp)) {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    String name = startElement.getName().getLocalPart();

                    switch (name) {
                        case ROOT:
                            molecules = new ArrayList<>();
                            break;
                        case UNITS:
                            units = new HashMap<>();
                            filling_units = true;
                            break;
                        case ITEM:
                            molecule = new HashMap<>();
                            break;
                        default:
                            String value = eventReader.nextEvent().asCharacters().getData();
                            if (filling_units) {
                                units.put(name, value);
                            } else {
                                try {
                                    molecule.put(name, df.parse(value).doubleValue());
                                } catch (ParseException e) {
                                    skipMolecule = true;
                                }
                            }
                    }
                } else if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    String name = endElement.getName().getLocalPart();

                    switch (name) {
                        case UNITS:
                            filling_units = false;
                            break;
                        case ITEM:
                            if (skipMolecule) {
                                IJ.log("\\Update:Invalid number format! Skipping over...");
                                skipMolecule = false;
                            } else {
                                molecules.add(molecule);
                            }
                            break;
                    }
                }
            }
        } catch (XMLStreamException ex) {
            throw new IOException(ex.toString());
        }

        if (molecules != null) {
            double[] values = null;
            String[] colnames = new String[1];
            int r = 0, nrows = molecules.size();

            for (HashMap<String, Double> mol : molecules) {
                if (mol.size() != colnames.length) {
                    if (mol.containsKey(MoleculeDescriptor.LABEL_ID)) {
                        colnames = new String[mol.size() - 1];
                    } else {
                        colnames = new String[mol.size()];
                    }
                }

                int ci = 0;
                for (String key : mol.keySet()) {
                    if (MoleculeDescriptor.LABEL_ID.equals(key)) continue;
                    colnames[ci++] = key;
                }

                if (!table.columnNamesEqual(colnames)) {
                    throw new IOException("Labels in the file do not correspond to the header of the table (excluding '" + MoleculeDescriptor.LABEL_ID + "')!");
                }

                if (table.isEmpty()) {
                    table.setDescriptor(new MoleculeDescriptor(colnames));
                    if (units != null) {
                        for (Map.Entry<String, String> col : units.entrySet()) {
                            table.setColumnUnits(col.getKey(), Units.fromString(col.getValue()));
                        }
                    }
                }

                if (values == null) {
                    values = new double[colnames.length];
                }

                for (int c = 0; c < colnames.length; c++) {
                    if (MoleculeDescriptor.LABEL_ID.equals(colnames[c])) continue;
                    values[c] = mol.get(colnames[c]);
                    if (MoleculeDescriptor.LABEL_FRAME.equals(colnames[c])) {
                        values[c] += startingFrame - 1;
                    }
                    IJ.showProgress((double) (r++) / (double) nrows);
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
        try (FileOutputStream fileOut = new FileOutputStream(fp)) {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(fileOut);
            writeToStream(eventWriter, floatPrecision, table, columns);
            eventWriter.close();
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    public void writeToStream(XMLEventWriter eventWriter, int floatPrecision, GenericTable table, List<String> columns) throws XMLStreamException {
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent tab = eventFactory.createDTD("\t");
        XMLEvent end = eventFactory.createDTD("\n");

        DecimalFormat df = StringFormatting.getDecimalFormat(floatPrecision);
        int nrows = table.getRowCount();

        eventWriter.add(eventFactory.createStartDocument());
        eventWriter.add(end);

        eventWriter.add(eventFactory.createStartElement("", "", ROOT));
        eventWriter.add(end);

        // Units
        eventWriter.add(tab);
        eventWriter.add(eventFactory.createStartElement("", "", UNITS));
        eventWriter.add(end);
        for (String column : columns) {
            String units = table.getColumnUnits(column).toString();
            if ((units != null) && !units.trim().isEmpty()) {
                createNode(eventWriter, column, units);
            }
        }
        eventWriter.add(tab);
        eventWriter.add(eventFactory.createEndElement("", "", UNITS));
        eventWriter.add(end);

        // Molecules
        for (int r = 0; r < nrows; r++) {
            eventWriter.add(tab);
            eventWriter.add(eventFactory.createStartElement("", "", ITEM));
            eventWriter.add(end);
            for (String column : columns) {
                createNode(eventWriter, column, df.format(table.getValue(r, column)));
            }
            eventWriter.add(tab);
            eventWriter.add(eventFactory.createEndElement("", "", ITEM));
            eventWriter.add(end);
            IJ.showProgress((double) r / nrows);
        }

        eventWriter.add(eventFactory.createEndElement("", "", ROOT));
        eventWriter.add(end);
        eventWriter.add(eventFactory.createEndDocument());
    }

    private void createNode(XMLEventWriter eventWriter, String name, String value) throws XMLStreamException {
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent end = eventFactory.createDTD("\n");
        XMLEvent tab = eventFactory.createDTD("\t\t");

        StartElement sElement = eventFactory.createStartElement("", "", name);
        eventWriter.add(tab);
        eventWriter.add(sElement);
        eventWriter.add(eventFactory.createCharacters(value));
        eventWriter.add(eventFactory.createEndElement("", "", name));
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
