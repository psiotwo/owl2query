/*******************************************************************************
 * Copyright (C) 2010 Czech Technical University in Prague
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

import cz.cvut.kbss.owl2query.engine.OWL2QueryEngine;
import cz.cvut.kbss.owl2query.engine.QueryGraphFactory;
import cz.cvut.kbss.owl2query.graph.gui.QueryGraphPanel;
import cz.cvut.kbss.owl2query.graph.gui.components.OWL2QueryResultTableModel;
import cz.cvut.kbss.owl2query.graph.gui.components.ResultPanel;
import cz.cvut.kbss.owl2query.graph.gui.components.StatusHandler;
import cz.cvut.kbss.owl2query.graph.gui.jgraph.SDLJGraph;
import cz.cvut.kbss.owl2query.graph.model.GroundTerm;
import cz.cvut.kbss.owl2query.graph.model.IQueryGraph;
import cz.cvut.kbss.owl2query.graph.model.QueryGraph;
import cz.cvut.kbss.owl2query.graph.model.QueryGraphToOWL2QueryConverter;
import cz.cvut.kbss.owl2query.graph.model.QuerySymbolDomain;
import cz.cvut.kbss.owl2query.graph.model.SymbolDomain;
import cz.cvut.kbss.owl2query.graph.model.Term;
import cz.cvut.kbss.owl2query.graph.resources.ResourceLoader;
import cz.cvut.kbss.owl2query.graph.util.GUIUtil;
import cz.cvut.kbss.owl2query.graph.util.SDLSystem;
import cz.cvut.kbss.owl2query.graph.util.TermSelection;
import cz.cvut.kbss.owl2query.model.OWL2Ontology;
import cz.cvut.kbss.owl2query.model.OWL2Query;
import cz.cvut.kbss.owl2query.model.QueryResult;
import cz.cvut.kbss.owl2query.model.ResultBinding;
import cz.cvut.kbss.owl2query.model.owlapi.OWLAPIv3OWL2Ontology;
import cz.cvut.kbss.owl2query.parser.arq.SparqlARQParser;
import cz.cvut.kbss.owl2query.parser.arq.SparqlARQWriter;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.inference.NoOpReasoner;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.view.AbstractActiveOntologyViewComponent;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

/**
 * @author kostob1
 */
public class OWL2QueryView extends AbstractActiveOntologyViewComponent {

	// register the resourcebundle of this class to the
	static {
		Locale.setDefault(Locale.ENGLISH);
		ResourceLoader.registerBundle("cz.cvut.kbss.owl2query.protege",
				"cz.cvut.kbss.owl2query.protege.resources");
	}
	private static final Logger log = Logger.getLogger(OWL2QueryView.class
			.getName());
	private static final long serialVersionUID = -4515710047558710080L;
	// Exception: UnsupportedQueryException: Complex query patterns are not
	// supported yet.
	// private static final String demoQuery =
	// "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX  owl:  <http://www.w3.org/2002/07/owl#>\n\n SELECT * WHERE {?x ?y ?z}";
	private static final String demoQuery = "SELECT * WHERE {?x ?y ?z}";
	private JTextPane txpQuery;
	private JTextArea txaSparqlDL;
	private IQueryGraph graph;
	private OWL2Ontology<OWLObject> owl2Ontology;
	private SymbolDomain domain;
	private QueryGraphPanel qgp;
	// must unregister in OWLModelManager in the dispose method
	private OWLModelManagerListener modelListener;
	private OWLOntologyChangeListener ontologyChangeListener;
	private OWLSelectionModelListener selectionListener;
	/**
	 * @uml.property name="fileChooser"
	 */
	private JFileChooser fileChooser = null;
	private File currentFile;
	private boolean queryChanged = false;
	private String serializedLayouts = "";
	private ExecutorService s = Executors.newCachedThreadPool();
	private Action saveAction;
	private JButton btnRunQuery;
	private ResultPanel pnlResult;
	private boolean acceptEdits = false;
	private SDLSystem sys;
	private JSplitPane sppPrefix;
	// this counter shows how many threads executing a query are running at the
	// moment
	protected Runnable currentQueryExecutionThread;
	
	private OWL2QueryRulesPanel pnlQueryRules;
	// protected Object mutex = new Object();

	protected void initialiseOntologyView() throws Exception {
		sys = new SDLSystem<OWL2Ontology>(new QuerySymbolDomain());
		owl2Ontology = createOWLOntology();
		setCurrentFile(null);
		initComponents();
		modelListener = new OWLModelManagerListener() {
			public void handleChange(OWLModelManagerChangeEvent event) {
				switch (event.getType()) {
				case ONTOLOGY_LOADED:
				case ONTOLOGY_RELOADED:
				case ONTOLOGY_CREATED:
				case ACTIVE_ONTOLOGY_CHANGED:
					owl2Ontology = createOWLOntology();
					sys.updateDomain(owl2Ontology);
					handleNewActiveOntology();

					// sys.getPrefixStore().init(getPrefixes(getOWLModelManager()));
					break;
				case REASONER_CHANGED:
				case ONTOLOGY_CLASSIFIED:
					btnRunQuery.setEnabled(!isNullReasoner());
					log.log(Level.SEVERE,
							"The ontology has been clasified or the reasoner has changed.");
					owl2Ontology = createOWLOntology();
					sys.updateDomain(owl2Ontology);
					
					// sys.fireReasonerChanged();
					break;
				}
			}
		};
		getOWLModelManager().addListener(modelListener);
		ontologyChangeListener = new OWLOntologyChangeListener() {
			public void ontologiesChanged(List<? extends OWLOntologyChange> arg0)
					throws OWLException {
				owl2Ontology = createOWLOntology();
				sys.updateDomain(owl2Ontology);
				SDLJGraph g = qgp.getGraph();
				g.getQuery().importIntoDomain(sys.getDomain());
				GUIUtil.refreshGraphView(g);
			}
		};
		getOWLModelManager().addOntologyChangeListener(ontologyChangeListener);

		final TermSelection.SelectionPartner partner = new TermSelection.StringSelectionPartner(
				sys.getSelection()) {
			public void selectionChanged(Object[] sel) {

				for (Object obj : sel) {
					if (obj instanceof GroundTerm) {
						OWLSelectionModel model = getOWLWorkspace()
								.getOWLSelectionModel();
						OWLDataFactory factory = getOWLDataFactory();
						SymbolDomain domain = sys.getDomain();
						Object wrappedobj = (GroundTerm) obj;
						IRI iri = IRI.create(wrappedobj.toString());
						if (domain.isClass(wrappedobj)) {
							model.setSelectedObject(factory.getOWLClass(iri));
						}

						if (domain.isIndividual(wrappedobj)) {
							model.setSelectedObject(factory
									.getOWLNamedIndividual(iri));
						}

						if (domain.isObjectProperty(wrappedobj)) {
							model.setSelectedObject(factory
									.getOWLObjectProperty(iri));
						}

						if (domain.isDataProperty(wrappedobj)) {
							model.setSelectedObject(factory
									.getOWLDataProperty(iri));
						}
					}
				}

				// Object[] cells= qgp.getGraph().getSelectionCells();
				// for(Object obj : cells){
				// DefaultGraphCell cell = (DefaultGraphCell)obj;
				// Object val = cell.getUserObject();
				// if(val != null && val instanceof IGraphElement){
				// if(val.getClass().equals(CompoundEdge.class)){
				// // find uris and deside wether they are data or object
				// properties
				// List<Term> props = ((CompoundEdge)val).getProperties();
				// for(Term prop : props){
				// if(prop.isGround()){
				// IRI iri =
				// IRI.create(prop.asGroundTerm().getWrappedObject().toString());
				// OWLObject owlobject = null;
				// if(prop.getType() == Term.OBJPROPERTY)
				// owlobject = getOWLDataFactory().getOWLObjectProperty(iri);
				// else if(prop.getType() == Term.DATAPROPERTY)
				// owlobject = getOWLDataFactory().getOWLDataProperty(iri);
				// if(owlobject != null)
				// getOWLWorkspace().getOWLSelectionModel().setSelectedObject(owlobject);
				// }
				// }
				// }else if(val instanceof Node){
				// Term t = ((Node)val).getId();
				// if(t.isGround()){
				// IRI iri =
				// IRI.create(t.asGroundTerm().getWrappedObject().toString());
				// OWLObject owlobject = null;
				// if(val instanceof ABoxNode){
				// owlobject = getOWLDataFactory().getOWLNamedIndividual(iri);
				// } else if(val instanceof TBoxNode){
				// owlobject = getOWLDataFactory().getOWLClass(iri);
				// } else if(val instanceof RBoxNode){
				// if(t.getType() == Term.OBJPROPERTY)
				// owlobject = getOWLDataFactory().getOWLObjectProperty(iri);
				// else if(t.getType() == Term.DATAPROPERTY)
				// owlobject = getOWLDataFactory().getOWLDataProperty(iri);
				// }
				// if(owlobject != null){
				// getOWLWorkspace().getOWLSelectionModel().setSelectedObject(owlobject);
				// }
				// }
				// }
				// }
				// }
			}
		};

		selectionListener = new OWLSelectionModelListener() {
			public void selectionChanged() throws Exception {
				if (!partner.isAdjusting()) {
					List l = new ArrayList(4);
					OWLSelectionModel model = getOWLWorkspace()
							.getOWLSelectionModel();
					Object obj = model.getSelectedObject();
					if (obj != null) {
						if (obj.equals(model.getLastSelectedClass())) {
							l.add(new GroundTerm(model.getLastSelectedClass()
									.getIRI().toString(), Term.CLASS));
						} else if (obj.equals(model
								.getLastSelectedDataProperty())) {
							l.add(new GroundTerm(model
									.getLastSelectedDataProperty().getIRI()
									.toString(), Term.DATAPROPERTY));
						} else if (obj.equals(model
								.getLastSelectedObjectProperty())) {
							l.add(new GroundTerm(model
									.getLastSelectedObjectProperty().getIRI()
									.toString(), Term.OBJPROPERTY));
						} else if (obj
								.equals(model.getLastSelectedIndividual())) {
							l.add(new GroundTerm(model
									.getLastSelectedIndividual().getIRI()
									.toString(), Term.INDIVIDUAL));
						}
						// System.out.println(l.size());
						// if(l.size() > 0)
						// System.out.println(l.get(0).toString());
					}
					partner.setSelection(l.toArray());
				}
			}
		};

		getOWLWorkspace().getOWLSelectionModel().addListener(selectionListener);

	}

	public void handleNewActiveOntology() {
		qgp.stopEditing();
		int ret = JOptionPane.showOptionDialog(this,
				ResourceLoader.getResource("new_ontology_prompt", this),
				ResourceLoader.getResource("save", this),
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, null, null);

		switch (ret) {
		case JOptionPane.YES_OPTION:
			saveAction.actionPerformed(null);
		case JOptionPane.NO_OPTION:
			createNewQuery();
			break;
		case JOptionPane.CANCEL_OPTION:
			SDLJGraph g = qgp.getGraph();
			g.getQuery().importIntoDomain(sys.getDomain());
			GUIUtil.refreshGraphView(g);
			break;
		}

	}

	public static Map<String, String> getPrefixes(OWLModelManager modelManager) {
		OWLOntologyManager owlManager = modelManager.getOWLOntologyManager();
		Map<String, String> prefixes = new HashMap<String, String>();
		List<OWLOntology> ontologies = new ArrayList<OWLOntology>(
				modelManager.getActiveOntologies());
		for (OWLOntology ontology : ontologies) {
			OWLOntologyFormat format = owlManager.getOntologyFormat(ontology);
			if (format instanceof PrefixOWLOntologyFormat) {
				PrefixOWLOntologyFormat newPrefixes = (PrefixOWLOntologyFormat) format;
				for (Map.Entry<String, String> entry : newPrefixes
						.getPrefixName2PrefixMap().entrySet()) {
					String prefixName = entry.getKey();
					String prefix = entry.getValue();
					prefixes.put(prefixName, prefix);
				}
			}
		}
		return prefixes;
	}

	// protected void dumpPrefixes(){
	// PrefixManager pm = getPrefixOWLOntologyFormat(getOWLModelManager());
	// Map<String, String> map = pm.getPrefixName2PrefixMap();
	// for(Map.Entry<String, String> e: map.entrySet()){
	// System.out.println(e.getKey() + " , " + e.getValue());
	// // log.log(Level.SEVERE,"(key,map) : (" + e.getKey() + " , " +
	// e.getValue() + ")");
	// }
	// }
	protected void disposeOntologyView() {
		getOWLModelManager().removeListener(modelListener);
		getOWLModelManager().removeOntologyChangeListener(
				ontologyChangeListener);
		getOWLWorkspace().getOWLSelectionModel().removeListener(
				selectionListener);
	}

	/**
	 * @return @uml.property name="fileChooser"
	 */
	private JFileChooser getFileChooser() {
		if (fileChooser == null) {
			final File fd = new File("examples/data");
			fileChooser = new JFileChooser(fd);
			fileChooser.setFileFilter(new FileNameExtensionFilter(
					ResourceLoader.getResource("file_description", this),
					"sparql"));
		}
		return fileChooser;
	}

	private JComponent createGraphPanel(final JPanel toolbars) {
		sys.updateDomain(owl2Ontology);
		// SDL
		domain = sys.getDomain();
		qgp = new QueryGraphPanel(sys);
		qgp.layoutGraphPanel(false, false);
		final Icon undo = Icons.getIcon("undo.gif");
		final Icon redo = Icons.getIcon("redo.gif");
		qgp.setIcons(undo, redo);

		toolbars.add(qgp.getToolbar());

		// graph = toQueryGraph(demoQuery);
		// qgp.setQuery(graph);
		//
		// graph.addChangeListener(new ChangeListener() {
		//
		// public void stateChanged(ChangeEvent e) {
		// System.out.println("'"+QueryGraphToOWL2QueryConverter
		// .convertQueryGraph(graph, domain).toString()+"'");
		// txaSparqlDL.setText("xxxx"+QueryGraphToOWL2QueryConverter
		// .convertQueryGraph(graph, domain).toString());
		// }
		// });

		// TODO update graph

		// TODO change SPARQL
		// // try {

		return qgp;
	}

	private IQueryGraph toQueryGraph(String query) {
		QueryGraphFactory f = QueryGraphFactory.getFactory();
		OWL2Query<OWLObject> q = null;
		try {
			q = f.parseFile(query, owl2Ontology);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
					this,
					ResourceLoader.getResource("parse_query_warning", this),
					ResourceLoader.getResource("parsing", this) + ": "
							+ e.getMessage(), JOptionPane.WARNING_MESSAGE);
		}
		return f.createQuery(q);
	}

	private boolean isNullReasoner() {
		OWLModelManager modelmanager = getOWLModelManager();
		OWLReasoner reasoner = modelmanager.getReasoner();
		if (reasoner == null) {
			return true;
		}
		return reasoner instanceof NoOpReasoner;
	}

	protected void interruptQueryExecution() {
		OWLModelManager modelmanager = getOWLModelManager();
		OWLReasoner reasoner = modelmanager.getReasoner();
		reasoner.interrupt();
	}

	private OWL2Ontology<OWLObject> createOWLOntology() {
		final OWLModelManager modelmanager = getOWLModelManager();
		// PrefixManager pm = new PrefixMapperImpl();
		// modelmanager.
		final OWLOntologyManager manager = modelmanager.getOWLOntologyManager();
		OWLOntology ontology = modelmanager.getActiveOntology();
		// try {
		// ontology = new OWLOntologyMerger(new
		// OWLOntologyImportsClosureSetProvider(manager,
		// ontology)).createMergedOntology(manager,
		// IRI.create(ontology.getOntologyID().getOntologyIRI().toString()+
		// "_MERGED"));
		// } catch (OWLOntologyCreationException e) {
		// log.log(Level.SEVERE,"Problem occured while merging the active ontology, returning ",e);
		// }
		final OWLReasoner reasoner = modelmanager.getReasoner();
		if (currentQueryExecutionThread != null) {
			reasoner.interrupt();
		}
		log.info("reasoner : " + reasoner);
		return new OWLAPIv3OWL2Ontology(manager, ontology, reasoner);
	}

	private JComponent createSparqlPanel() {
		final JPanel pnl = new JPanel(new BorderLayout());
		final JScrollPane scpQuery = new JScrollPane();
		txpQuery = new ExpressionEditor<OWL2Query<OWLObject>>(getOWLWorkspace()
				.getOWLEditorKit(),
				new OWLExpressionChecker<OWL2Query<OWLObject>>() {
					public void check(String arg0)
							throws OWLExpressionParserException {
					}

					public OWL2Query<OWLObject> createObject(String arg0)
							throws OWLExpressionParserException {
						return null;
					}
				});
		((AbstractDocument) txpQuery.getDocument())
				.setDocumentFilter(new DocumentFilter() {
					@Override
					public void remove(FilterBypass fb, int offset, int length)
							throws BadLocationException {
						// TODO Auto-generated method stub
						handleChange(fb.getDocument().getText(offset, length));
						super.remove(fb, offset, length);
					}

					@Override
					public void insertString(FilterBypass fb, int offset,
							String string, AttributeSet attr)
							throws BadLocationException {
						handleChange(string);
						super.insertString(fb, offset, string, attr);
					}

					@Override
					public void replace(FilterBypass fb, int offset,
							int length, String text, AttributeSet attrs)
							throws BadLocationException {
						handleReplace(fb.getDocument().getText(offset, length),
								text);
						super.replace(fb, offset, length, text, attrs);
					}

					public void handleChange(String str) {
						if (isSignificantChange(str)) {
							pnlResult.setValidResults(false);
						}
					}

					public void handleReplace(String str, String with) {
						if (isSignificantChange(str)
								&& isSignificantChange(with)) {
							pnlResult.setValidResults(false);
						}
					}

					public boolean isSignificantChange(String str) {
						// return pnlResult != null && acceptEdits &&
						// !str.matches("^\\s*$");
						return pnlResult != null && acceptEdits;
					}
				});

		// txpQuery.getDocument().addDocumentListener(new DocumentListener(){
		// @Override
		// public void insertUpdate(DocumentEvent e) {
		// // System.out.println("insertUpdate");
		// if(pnlResult != null && acceptEdits){
		// updateLog(e, "inserted into");
		// if(isSignificantChange(e))
		// pnlResult.setValidResults(false);
		// }
		// }
		//
		// @Override
		// public void removeUpdate(DocumentEvent e) {
		// System.out.println("removeUpdate");
		// if(pnlResult != null && acceptEdits){
		// updateLog(e, "removed from");
		// if(isSignificantChange(e))
		// pnlResult.setValidResults(false);
		// }
		// }
		//
		// public void updateLog(DocumentEvent e, String action) {
		// Document doc = (Document)e.getDocument();
		// int changeLength = e.getLength();
		// System.out.println(
		// changeLength + " character" +
		// ((changeLength == 1) ? " " : "s ") +
		// action + " " + doc.getProperty("name") + "." + "\n" +
		// "  Text length = " + doc.getLength());
		// }
		//
		//
		// @Override
		// public void changedUpdate(DocumentEvent e) {
		// // TODO Auto-generated method stub
		// // System.out.println("changedUpdate");
		// // System.out.println(e.toString());
		// // System.out.println(e.getLength());
		// // System.out.println(e.getOffset());
		// }
		//
		// public boolean isSignificantChange(DocumentEvent e){
		// try {
		// // pnlResult != null && acceptEdits && !str.matches("^\\s*$");
		// String str = e.getDocument().getText(0,e.getDocument().getLength());
		// StringTokenizer st = new StringTokenizer(str);
		// boolean ret =!str.matches("^\\s*$");
		// return ret;
		// } catch (BadLocationException ex) {
		// ex.printStackTrace();
		// }
		// return false;
		// }
		//
		// });
		scpQuery.setViewportView(txpQuery);
		pnl.add(scpQuery, BorderLayout.CENTER);
		return pnl;
	}

	private void createNewQuery() {
		setQuery(true, "");
		setQuery(false, "");
		pnlResult.clearResults();
		setCurrentFile(null);
	}

	private void setQuery(boolean isGraphSelected, String query) {
		if (isGraphSelected) {
			if (query == null || query.length() == 0) {
				graph = new QueryGraph();
			} else {
				graph = toQueryGraph(query);
			}
			qgp.setQuery(graph);
			handleNewGraph();
		} else {
			acceptEdits = false;
			txpQuery.setText(query);
			acceptEdits = true;
		}
		queryChanged = false;
	}

	private void handleNewGraph() {
		graph.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				pnlResult.setValidResults(false);
				txaSparqlDL.setText(QueryGraphToOWL2QueryConverter
						.convertQueryGraph(graph, domain).toString());
				queryChanged = true;
			}
		});
		txaSparqlDL.setText(QueryGraphToOWL2QueryConverter.convertQueryGraph(
				graph, domain).toString());
	}

	/**
	 * @param file
	 * @uml.property name="currentFile"
	 */
	private void setCurrentFile(final File file) {
		currentFile = file;

		if (file != null) {
			setHeaderText(file.getAbsolutePath());
		} else {
			setHeaderText(ResourceLoader.getResource("file_status", this));
		}
	}

	/**
	 * 
	 * @return true if the user has selected yes or no, false if the user has
	 *         selected cancel.
	 */
	private boolean promptToSaveAQuery() {
		int ret = JOptionPane.showConfirmDialog(this,
				ResourceLoader.getResource("overide_query_message", this));
		switch (ret) {
		case JOptionPane.YES_OPTION:
			saveAction.actionPerformed(null);
		case JOptionPane.NO_OPTION:
			return true;
		}
		return false;
	}

	private void initComponents() {
		final String C_GRAPH = "graph";
		final String C_SPARQL = "sparql";
		final String C_RULES = "rules";

		final JPanel pnlMain = new JPanel(new BorderLayout());
		final JToolBar pnlChoice = new JToolBar();

		final JButton btnNewQuery = new JButton();
		final JButton btnDemoQuery = new JButton();
		final JButton btnOpenQuery = new JButton(ResourceLoader.getResource(
				"open", this));
		final JButton btnSaveQuery = new JButton(ResourceLoader.getResource(
				"save", this));
		// final JButton btnSaveAsQuery = new JButton("save as ...");

		pnlChoice.add(btnDemoQuery);
		pnlChoice.add(btnNewQuery);
		pnlChoice.add(btnOpenQuery);
		pnlChoice.add(btnSaveQuery);
		// pnlChoice.add(btnSaveAsQuery);

		pnlChoice.setRollover(true);

		final JPanel pnlToolbars = new JPanel(
				new FlowLayout(FlowLayout.LEADING));
		pnlToolbars.add(pnlChoice);

		final JRadioButton rbtGraph = new JRadioButton(
				ResourceLoader.getResource("graph", this));
		pnlChoice.add(rbtGraph);
		final JRadioButton rbtQuery = new JRadioButton("SPARQL");
		pnlChoice.add(rbtQuery);
		final JRadioButton rbtRules = new JRadioButton("Rules");
		pnlChoice.add(rbtRules);
		final ButtonGroup grpChoice = new ButtonGroup();
		grpChoice.add(rbtGraph);
		grpChoice.add(rbtQuery);
		grpChoice.add(rbtRules);
		rbtGraph.setSelected(true);

		final CardLayout cardLayout = new CardLayout();
		final JPanel pnlQuery = new JPanel(cardLayout);
		pnlQueryRules = new OWL2QueryRulesPanel(getOWLWorkspace(),owl2Ontology);
		final JComponent pnlQuerySPARQL = createSparqlPanel();
		final JComponent pnlQueryGraph = createGraphPanel(pnlToolbars);

		pnlQuery.add(pnlQueryRules, C_RULES);
		pnlQuery.add(pnlQuerySPARQL, C_SPARQL);
		pnlQuery.add(pnlQueryGraph, C_GRAPH);
		pnlMain.add(pnlQuery, BorderLayout.CENTER);
		txaSparqlDL = new JTextArea();
		txaSparqlDL.setEditable(false);
		txaSparqlDL.setLineWrap(true);
		// final Dimension d = new Dimension(0, 40);
		// txaSparqlDL.setPreferredSize(d);

		final JScrollPane scpSPARQLDL = new JScrollPane(txaSparqlDL);
		scpSPARQLDL
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scpSPARQLDL
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		txaSparqlDL.setToolTipText(ResourceLoader.getResource(
				"sparqldltxt_tooltip", this));
		// scpSPARQLDL.getViewport().setPreferredSize(d);

		pnlMain.add(scpSPARQLDL, BorderLayout.SOUTH);

		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (rbtGraph.isSelected()) {
						setQuery(true, txpQuery.getText());
						qgp.getLayoutStoreEditor().loadStoreFromString(
								serializedLayouts);
						// layout the genereal tab
						JSplitPane gen = qgp.getGeneralPanel();
						if (sppPrefix != null) {
							Component pref = sppPrefix.getTopComponent();
							if (pref != null && pref != gen) {
								int p = sppPrefix.getDividerLocation();
								sppPrefix.remove(pref);
								gen.setTopComponent(pref);
								sppPrefix.setTopComponent(gen);
								sppPrefix.setDividerLocation(p);
							}
						}
						cardLayout.show(pnlQuery, C_GRAPH);
					} else if (rbtQuery.isSelected()) {
						OWL2Query<OWLObject> q = QueryGraphToOWL2QueryConverter
								.convertQueryGraph(graph, domain);
						final StringWriter w = new StringWriter();
						new SparqlARQWriter<OWLObject>().write(q, w,
								owl2Ontology);
						setQuery(false, w.toString());
						// txpQuery.setText(w.toString());
						serializedLayouts = qgp.getLayoutStoreEditor()
								.storeToString();
						// layout the genereal tab
						JSplitPane gen = qgp.getGeneralPanel();
						Component pref = gen.getLeftComponent();
						if (pref != null) {
							gen.remove(pref);
							int p = sppPrefix.getDividerLocation();
							sppPrefix.remove(gen);
							sppPrefix.setTopComponent(pref);
							sppPrefix.setDividerLocation(p);
						}
						cardLayout.show(pnlQuery, C_SPARQL);
					} else if (rbtRules.isSelected()) {
						cardLayout.show(pnlQuery, C_RULES);						
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					String s = rbtGraph.isSelected() ? "parse_query_warning"
							: rbtQuery.isSelected() ? "serialize_query_warning"
									: null;
					JOptionPane.showMessageDialog(OWL2QueryView.this,
							ResourceLoader.getResource(s, OWL2QueryView.this)
									+ ex.getMessage(), ResourceLoader
									.getResource("error", OWL2QueryView.this),
							JOptionPane.ERROR_MESSAGE);
				}

			}
		};

		rbtGraph.addActionListener(al);

		rbtQuery.addActionListener(al);

		rbtRules.addActionListener(al);

		// grpChoice.getSelection().addChangeListener(new ChangeListener() {
		// public void stateChanged(ChangeEvent e) {
		// if (rbtGraph.isSelected()) {
		// try {
		// setQuery(true, txpQuery.getText());
		// } catch (Exception ex) {
		// JOptionPane.showMessageDialog(OWL2QueryView.this,
		// "A problem occured when parsing the query into SPARQL-DL: "
		// + ex.getMessage(), "Error",
		// JOptionPane.ERROR_MESSAGE);
		// }
		// cardLayout.show(pnlQuery, C_GRAPH);
		// } else {
		// try {
		// OWL2Query<OWLObject> q = QueryGraphToOWL2QueryConverter
		// .convertQueryGraph(graph, owl2Ontology);
		// final StringWriter w = new StringWriter();
		// new SparqlARQParser<OWLObject>().write(q, w, owl2Ontology);
		// txpQuery.setText(w.toString());
		// } catch (Exception ex) {
		// JOptionPane.showMessageDialog(OWL2QueryView.this,
		// "A problem occured when transforming the SPARQL-DL graph to SPARQL: "
		// + ex.getMessage(), "Error",
		// JOptionPane.ERROR_MESSAGE);
		// }
		// cardLayout.show(pnlQuery, C_SPARQL);
		// }
		// }
		// });

		cardLayout.show(pnlQuery, C_SPARQL);
		setQuery(false, demoQuery);
		rbtGraph.doClick();

		final GridBagLayout gbl = new GridBagLayout();
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		final JPanel pnlEvaluation = new JPanel(gbl);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		final Icon play_icon = new ImageIcon(getClass().getResource(
				"icons/run.png"));
		final Icon stop_icon = new ImageIcon(getClass().getResource(
				"icons/stop.png"));
		final Icon stoping_icon = new ImageIcon(getClass().getResource(
				"icons/stoping.png"));
		btnRunQuery = new JButton(ResourceLoader.getResource("run", this),
				play_icon);
		btnRunQuery.setToolTipText(ResourceLoader.getResource("run_tooltip",
				this));
		pnlEvaluation.add(btnRunQuery, gbc);

		// gbc.gridx = 0;
		// gbc.gridy++;
		// gbc.anchor = GridBagConstraints.BASELINE;
		// final JLabel lblResults = new JLabel("Results:");
		// pnlEvaluation.add(lblResults, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		pnlResult = new ResultPanel(sys);
		btnRunQuery.setEnabled(!isNullReasoner());
		// final JTable tblResults = new JTable();
		btnRunQuery.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				final OWL2Query<OWLObject> query;
				// owl2Ontology = createOWLOntology();
				boolean isRunning = true;
				synchronized (btnRunQuery) {
					isRunning = currentQueryExecutionThread != null;
				}
				if (isRunning) {
					interruptQueryExecution();
					// btnRunQuery.setEnabled(false);
					btnRunQuery.setIcon(stoping_icon);
					return;
				}

				if (rbtGraph.isSelected()) {
					query = QueryGraphToOWL2QueryConverter.convertQueryGraph(
							graph, domain);
				} else if (rbtQuery.isSelected()) {
					query = new SparqlARQParser<OWLObject>().parse(
							txpQuery.getText(), owl2Ontology);
				} else {
					log.log(Level.SEVERE, "Unknown status");
					return;
				}

				// currentQueryExecutionThread =

				s.submit(new Runnable() {
					public void run() {

						// synchronized (mutex) {
						try {
							currentQueryExecutionThread = this;
							btnRunQuery.setText(ResourceLoader.getResource(
									"stop", OWL2QueryView.this));
							btnRunQuery.setIcon(stop_icon);
							log.log(Level.INFO, "Executing query: " + query);
							final QueryResult<OWLObject> r = OWL2QueryEngine
									.exec(query);
							log.log(Level.INFO, "Result: " + r.size());
							final List<ResultBinding<OWLObject>> b = new ArrayList<ResultBinding<OWLObject>>();
							for (final Iterator<ResultBinding<OWLObject>> i = r
									.iterator(); i.hasNext();) {
								b.add(i.next());
							}
							// System.out.println(threadCount - 1);
							// btnRunQuery.setText(ResourceLoader.getResource("run",
							// OWL2QueryView.this) + " ("+ (--threadCount)
							// +")");
							if (this == currentQueryExecutionThread) {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										pnlResult
												.setResult(new OWL2QueryResultTableModel(
														r));
									}
								});
							}

						} catch (Exception e) {
							log.log(Level.WARNING, "", e);
						} finally {
							synchronized (btnRunQuery) {
								btnRunQuery.setText(ResourceLoader.getResource(
										"run", OWL2QueryView.this));
								btnRunQuery.setIcon(play_icon);
								// btnRunQuery.setEnabled(true);
								currentQueryExecutionThread = null;
							}
						}
					}
					// SwingUtilities.invokeLater(new Runnable() {
					// public void run() {
					// tblResults.setModel(new AbstractTableModel() {
					//
					// public String getColumnName(int column) {
					// return r.getResultVars().get(column)
					// .toString();
					// }
					//
					// public int getColumnCount() {
					// return r.getResultVars().size();
					// }
					//
					// public int getRowCount() {
					// return b.size();
					// }
					//
					// public Object getValueAt(int rowIndex,
					// int columnIndex) {
					// return b.get(rowIndex).get(
					// r.getResultVars().get(
					// columnIndex));
					// }
					// });
					// }
					// });
					// }
				});
			}
		});

		Action demoAction = new AbstractAction(null, new ImageIcon(getClass()
				.getResource("icons/sample.png"))) {
			public void actionPerformed(ActionEvent e) {
				if (queryChanged) {
					if (!promptToSaveAQuery()) {
						return;// the user has canceled the action
					}
				}
				setQuery(rbtGraph.isSelected(), demoQuery);
				pnlResult.clearResults();
				setCurrentFile(null);
			}
		};
		demoAction.putValue(Action.SHORT_DESCRIPTION,
				ResourceLoader.getResource("sample_query", this));
		btnDemoQuery.setAction(demoAction);

		Action newAction = new AbstractAction(null, new ImageIcon(getClass()
				.getResource("icons/new.png"))) {
			public void actionPerformed(ActionEvent e) {
				if (queryChanged) {
					if (!promptToSaveAQuery()) {
						return;// the user has canceled the action
					}
				}
				createNewQuery();
				// if (rbtGraph.isSelected()) {
				// graph = new QueryGraph();
				// qgp.setQuery(graph);
				// handleNewGraph();
				// pnlResult.clearResults();
				// } else {
				// txpQuery.setText("");
				// }
				// setCurrentFile(null);
			}
		};

		newAction.putValue(Action.SHORT_DESCRIPTION,
				ResourceLoader.getResource("new_query", this));
		btnNewQuery.setAction(newAction);

		Action openAction = new AbstractAction(null, new ImageIcon(getClass()
				.getResource("icons/open.png"))) {
			public void actionPerformed(ActionEvent e) {
				try {

					// if (currentFile != null) {
					// // JOptionPane.show
					// int retSave = fc.showSaveDialog(OWL2QueryView.this);
					// if(retSave == JFileChooser.APPROVE_OPTION){
					// saveAction.actionPerformed(null);
					// fc.setSelectedFile(null);
					// }
					// }
					if (queryChanged) {
						if (!promptToSaveAQuery()) {
							return;// the user has canceled the action
						}
					}
					final JFileChooser fc = getFileChooser();
					fc.setSelectedFile(new File(""));
					// fc.setVisible(true);

					int ret = fc.showOpenDialog(OWL2QueryView.this);

					if (ret == JFileChooser.APPROVE_OPTION) {

						setCurrentFile(fc.getSelectedFile());
						final BufferedReader r = new BufferedReader(
								new InputStreamReader(new FileInputStream(
										currentFile)));
						String retstr = "";
						String line;
						while ((line = r.readLine()) != null) {
							retstr += line + '\n';
						}

						setQuery(rbtGraph.isSelected(), retstr);
						qgp.getLayoutStoreEditor().loadStoreFromFile(
								currentFile);
						serializedLayouts = qgp.getLayoutStoreEditor()
								.storeToString();
						pnlResult.clearResults();
					}
				} catch (IOException e1) {
					log.log(Level.SEVERE, e1.getMessage(), e1);
				}
			}
		};
		openAction.putValue(Action.SHORT_DESCRIPTION,
				ResourceLoader.getResource("open_query", this));
		btnOpenQuery.setAction(openAction);

		class SaveAction extends AbstractAction {

			boolean force;

			SaveAction(boolean force) {
				super(null, new ImageIcon(OWL2QueryView.this.getClass()
						.getResource("icons/save.png")));
				this.force = force;
			}

			public void actionPerformed(final ActionEvent e) {
				if (force || currentFile == null) {
					final JFileChooser fc = getFileChooser();
					fc.setVisible(true);
					int ret = fc.showSaveDialog(OWL2QueryView.this);
					if (ret != JFileChooser.APPROVE_OPTION) {
						return;
					}

					setCurrentFile(fc.getSelectedFile());
				}

				BufferedWriter w = null;
				try {
					w = new BufferedWriter(new FileWriter(currentFile));
					OWL2Query<OWLObject> q = QueryGraphToOWL2QueryConverter
							.convertQueryGraph(graph, domain);
					new SparqlARQWriter<OWLObject>().write(q, w, owl2Ontology);
					// w.write(txpQuery.getText());
					String layouts = qgp.getLayoutStoreEditor().storeToString();
					w.write("\n" + layouts);
					w.close();
					queryChanged = false;
				} catch (IOException e1) {
					log.log(Level.SEVERE, e1.getMessage(), e1);
				}
			}
		}
		;

		saveAction = new SaveAction(false);
		saveAction.putValue(Action.SHORT_DESCRIPTION,
				ResourceLoader.getResource("save_query", this));
		btnSaveQuery.setAction(saveAction);
		// btnSaveAsQuery.setAction(new SaveAction(true));

		// final JScrollPane scpResults = new JScrollPane(tblResults);
		// scpResults.getViewport()
		// .setPreferredSize(tblResults.getPreferredSize());
		// pnlEvaluation.add(scpResults, gbc);
		pnlEvaluation.add(pnlResult, gbc);
		// scpResults.setViewportView(tblResults);

		final JSplitPane sppResults = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				pnlMain, pnlEvaluation);
		sppResults.setResizeWeight(1);
		sppResults.setDividerLocation(0.8);
		sppResults.setOneTouchExpandable(true);
		sppPrefix = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				qgp.getGeneralPanel(),// new PrefixEditor(sys),
				sppResults);
		sppPrefix.setDividerLocation(0.2);
		sppPrefix.setOneTouchExpandable(true);
		this.setLayout(new BorderLayout());
		this.add(pnlToolbars, BorderLayout.PAGE_START);
		this.add(sppPrefix, BorderLayout.CENTER);
		StatusHandler.DefaultStatusHandler sh = new StatusHandler.DefaultStatusHandler();
		JPanel statusContainer = new JPanel();
		statusContainer.setLayout(new FlowLayout(FlowLayout.RIGHT));
		sys.setStatusHandler(sh);
		statusContainer.add(sh);
		this.add(statusContainer, BorderLayout.SOUTH);
		// this.getWorkspace().getStatusArea().set
	}// </editor-fold>//GEN-END:initComponents

	@Override
	protected void updateView(OWLOntology activeOntology) throws Exception {
		// TODO Auto-generated method stub

	}
}
