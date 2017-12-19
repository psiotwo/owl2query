/*******************************************************************************
 * Copyright (C) 2013 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If notSPA, see <http://www.gnu.org/licenses/>. 
 *****************************************************************************
 */
package cz.cvut.kbss.owl2query.protege;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.semanticweb.owlapi.model.OWLObject;

import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWL2Query;
import cz.cvut.kbss.owl2query.model.OWL2Rule;
import cz.cvut.kbss.owl2query.model.owlapi.OWLAPIv3OWL2Ontology;
import cz.cvut.kbss.owl2query.parser.arq.SparqlARQParser;

public class OWL2QueryRulesPanel extends JPanel {

	private static final long serialVersionUID = -8883469080896399271L;

	private JScrollPane scpQuery;
	private JTextPane txpQuery;
	private JScrollPane scpRules;
	private JTable tblRules;
	private SPARQLDLNotTableModel model;
	
	private JButton btnReload;
	
	private JFileChooser fc;
	private File currentDir = new File("");
	private JButton btnOpenDir = new JButton("Rule Base Dir: ");
	private JLabel lblCurrentDir = new JLabel("");
	
	private SPARQLDLNOTRule selectedRule;
	
	private OWLWorkspace w;
	
	private OWL2Ontology<OWLObject> o;
	
	OWL2QueryRulesPanel(final OWLWorkspace w, final OWL2Ontology<OWLObject> o) {
		System.out.println("CONSTRUCTING");
		this.w = w;
		this.o = o;
		
		this.init();
		this.fillMockModel();
	}
	
	public void setOWLOntology(OWLAPIv3OWL2Ontology o) {
		this.o = o;
	}
	
	private void updateRule(SPARQLDLNOTRule rule, final String s) {
		OWL2Rule<OWLObject> r = new SparqlARQParser().parseConstruct(s, o);
		rule.setRule(r);
		rule.setSparqlString(s);
	}
	
	private SPARQLDLNOTRule createRule(final String s) {
		final SPARQLDLNOTRule rr = new SPARQLDLNOTRule();
		updateRule(rr, s);
		return rr;
	}
	
	private void fillMockModel() {
		model.addRule(createRule("CONSTRUCT {?x a ?z} WHERE {?x a ?z}"));
		model.addRule(createRule("CONSTRUCT {?x a ?z. ?z a ?x.} WHERE {?x a ?z}"));		
	}
	
	private JPanel createTopPanel() {
		fc = new JFileChooser();
		
		final JPanel pnlTop = new JPanel();
		
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		pnlTop.add(btnOpenDir, gbc);

		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		pnlTop.add(lblCurrentDir, gbc);
		
		return pnlTop;
	}
	
	private JComponent createMainPanel() {
		scpQuery = new JScrollPane();
		txpQuery = new ExpressionEditor<OWL2Query<OWLObject>>(
				w.getOWLEditorKit(),
				new OWLExpressionChecker<OWL2Query<OWLObject>>() {
					public void check(String arg0)
							throws OWLExpressionParserException {
					}

					public OWL2Query<OWLObject> createObject(String arg0)
							throws OWLExpressionParserException {
						return null;
					}
				});
		scpQuery.setViewportView(txpQuery);
		
		scpRules = new JScrollPane();
		tblRules = new JTable();
		tblRules.setFillsViewportHeight(true);
		tblRules.setPreferredSize(new Dimension(100,100));
		scpRules.setSize(tblRules.getPreferredSize());
		
		scpRules.setViewportView(tblRules);
		JSplitPane spp = new JSplitPane();
		spp.setRightComponent(scpQuery);
		spp.setDividerLocation(0.3);
		spp.setLeftComponent(tblRules);
		return spp;
	}
	
	private void initView() {
		this.setLayout(new GridBagLayout());

		final GridBagConstraints gbc = new GridBagConstraints();
		
		btnReload=new JButton("RELOAD");		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth=2;
		gbc.anchor=GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add(btnReload, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth=2;
		gbc.anchor=GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add(createTopPanel(), gbc);

		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth=1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.3;
		gbc.weighty = 1;
		
		this.add(createMainPanel(), gbc);
	}
	
	private void initControllers() {
		btnOpenDir.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = fc.showOpenDialog(OWL2QueryRulesPanel.this);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			       currentDir = fc.getSelectedFile();
			       lblCurrentDir.setText(currentDir.getPath());			     
			    }
			}
		});
		btnReload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run( ) {
						OWL2QueryRulesPanel.this.invalidate();						
						OWL2QueryRulesPanel.this.removeAll();
						init();
						fillMockModel();
						OWL2QueryRulesPanel.this.validate();
					}
				});
			}
		});
		tblRules.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				
				SPARQLDLNOTRule r = model.getRule(tblRules.getSelectedRow());
				if ( r != null ) {
					selectedRule = r;
					txpQuery.setText(r.getSparqlString());
				}
			}
		});
		txpQuery.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				updateRule(selectedRule,txpQuery.getText());
			}
			
			@Override
			public void focusGained(FocusEvent e) {}
		});
	}
	
	private void initModels() {
		model = new SPARQLDLNotTableModel();

		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);		
		tblRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblRules.setEditingColumn(0);
		tblRules.setAutoCreateColumnsFromModel(true);
		tblRules.setModel(model);
	}
	
	private void init() {
		System.out.println("INIT");
		initView();
		initControllers();
		initModels();
	}

}
