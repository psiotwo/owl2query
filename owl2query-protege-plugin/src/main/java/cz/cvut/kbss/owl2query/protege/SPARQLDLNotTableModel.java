package cz.cvut.kbss.owl2query.protege;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

public class SPARQLDLNotTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -8114688446930065744L;
	
	private List<SPARQLDLNOTRule> rows = new ArrayList<SPARQLDLNOTRule>();
	private Set<SPARQLDLNOTRule> active = new HashSet<SPARQLDLNOTRule>();

	public SPARQLDLNotTableModel() {
	}	

	public void addRule(SPARQLDLNOTRule rule) {
		rows.add(rule);
	}
	
	public void removeRule(SPARQLDLNOTRule rule) {
		rows.remove(rule);
	}

	public Boolean isActive(SPARQLDLNOTRule rule) {
		return active.contains(rule);
	}
	
	public SPARQLDLNOTRule getRule(int row) {
		return rows.get(row);
	}
	
	public void setActive(SPARQLDLNOTRule rule, Boolean b) {
		if (( b == null || !b) &&  active.contains(rule)) {
			active.remove(rule);
		} else {
			active.add(rule);
		}
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
	    return (col == 0); 
	}
	
	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}
	
	@Override
	public Class<?> getColumnClass(int column) {
		switch (column) {
			case 0: return Boolean.class; 
			case 1: return Integer.class; 
		}
		throw new IllegalArgumentException();
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
			case 0: return ""; 
			case 1: return "rule"; 
		}
		throw new IllegalArgumentException();
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
			case 0: return isActive(rows.get(rowIndex)); 
			case 1: return rows.get(rowIndex).getRule().toString(); 
		}
		throw new IllegalArgumentException();
	}
	@Override
	public void setValueAt(Object o, int rowIndex, int columnIndex) {
		if (columnIndex != 0) {
			throw new IllegalArgumentException();
		}		
		setActive(rows.get(rowIndex), (Boolean) o);
	}
}
