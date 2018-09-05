
package cz.cuni.lf1.lge.ThunderSTORM.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.SystemColor;
import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.apache.commons.io.FilenameUtils;

import cz.cuni.lf1.lge.ThunderSTORM.util.IOUtils;
import ij.IJ;
import ij.plugin.LutLoader;
import ij.process.LUT;

public class LutPicker extends JComboBox {

	private DefaultComboBoxModel model;
	private HashMap<String, LUT> luts;

	public LutPicker() {
		model = new DefaultComboBoxModel();
		setModel(model);
		setRenderer(new LutPickerItemRenderer());
		setEditor(new LutPickerItemEditor());

		initialize();
	}

	private void initialize() {
		String path = IJ.getDirectory("luts");
		List<File> files = IOUtils.listFilesInFolder(new File(path), false);
		luts = new HashMap<String, LUT>();
		for (File f : files) {
			if (FilenameUtils.getExtension(f.getName()).toLowerCase().equals("lut")) {
				String lutName = FilenameUtils.removeExtension(f.getName()).replace('_', ' ');
				LUT lut = LutLoader.openLut(f.getAbsolutePath());
				luts.put(lutName, lut);
				model.addElement(lutName);
			}
		}
	}

	public boolean lutExists(String input) {
		return luts.containsKey(input);
	}

	public LUT getLut(String lut) {
		return luts.get(lut);
	}

	private class LutPickerItemRenderer extends JPanel implements ListCellRenderer {

		private JLabel labelItem = new JLabel();

		public LutPickerItemRenderer() {
			setLayout(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 1.0;
			constraints.insets = new Insets(2, 2, 2, 2);

			labelItem.setOpaque(true);
			labelItem.setHorizontalAlignment(JLabel.LEFT);

			add(labelItem, constraints);
			setBackground(Color.LIGHT_GRAY);
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus)
		{
			if (value != null) {
				labelItem.setText((String) value);
				labelItem.setIcon(new ImageIcon(LutLoader.createImage(luts.get(value)).createImage()
					.getScaledInstance(150, 15, Image.SCALE_DEFAULT)));
			}
			if (isSelected) {
				labelItem.setBackground(SystemColor.textHighlight);
				labelItem.setForeground(SystemColor.textHighlightText);
			}
			else {
				labelItem.setBackground(SystemColor.text);
				labelItem.setForeground(SystemColor.textText);
			}
			return this;
		}
	}

	private class LutPickerItemEditor extends BasicComboBoxEditor {

		private JPanel panel = new JPanel();
		private JLabel labelItem = new JLabel();
		private String selectedValue;

		public LutPickerItemEditor() {
			panel.setLayout(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 1.0;
			constraints.insets = new Insets(2, 5, 2, 2);

			labelItem.setOpaque(false);
			labelItem.setHorizontalAlignment(JLabel.LEFT);
			labelItem.setForeground(Color.WHITE);

			panel.add(labelItem, constraints);
			panel.setBackground(Color.BLUE);
		}

		public Component getEditorComponent() {
			return this.panel;
		}

		public Object getItem() {
			return this.selectedValue;
		}

		public void setItem(Object item) {
			if (item == null) {
				return;
			}
			String[] lutItem = (String[]) item;
			selectedValue = lutItem[0];
			labelItem.setText(selectedValue);
			labelItem.setIcon(new ImageIcon(lutItem[1]));
		}
	}
}
