/*
 * Copyright (C) 2014 Ye Tian
 * Department of Electrical and Computer Engineering, Virginia Tech
 * 
 * This file is part of KDDN app for Cytoscape.
 *
 * KDDN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * KDDN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with KDDN. If not, see <http://www.gnu.org/licenses/>.
 */

package org.cytoscape.kddn.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Read input files for gene list, data, and prior network
 * @author Ye Tian
 *
 */
public class FileInput {

	public FileInput() {
		
	}

	public static String[] readGeneList(String fileName) throws IOException {
		ArrayList<String> genes = new ArrayList<String>();

		BufferedReader inputStream = null;
        try {
            inputStream = new BufferedReader(new FileReader(fileName));

            String l;

            while ((l = inputStream.readLine()) != null) {
            	genes.add(l);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        
		return genes.toArray(new String[genes.size()]);
	}

	public static double[][] readData(String fileName) throws IOException {
		
        BufferedReader inputStream = null;
        try {

            inputStream = new BufferedReader(new FileReader(fileName));

            String l;
            String[] row;
            int numRow = 0;
            while ((l = inputStream.readLine()) != null) {
                numRow++;
            }

            inputStream = new BufferedReader(new FileReader(fileName));
            l = inputStream.readLine();
            int numCol = l.split("[,\t]+").length;

            double[][] data = new double[numCol][numRow];
            int i = 0;
            inputStream = new BufferedReader(new FileReader(fileName));
            while ((l = inputStream.readLine()) != null) {
                row = l.split("[,\t]+");
                for (int j=0; j<numCol; j++) {
                	
                	// check if input are numerical
                	try {
						data[j][i] = Double.parseDouble(row[j]);
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null, 
								"Non-numerical contents in data file.");
						e.printStackTrace();
						
						return null;
					}
                }
                i++;
            }
            
            return data;

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
	}

	public static String[][] readKnowledge(String fileName) throws IOException {
		ArrayList<String[]> edges = new ArrayList<String[]>();

		// not xml kgml file
		if(!fileName.endsWith("xml")) {
			BufferedReader inputStream = null;
	        try {
	            inputStream = new BufferedReader(new FileReader(fileName));
	
	            String l;
	            String[] row;
	            while ((l = inputStream.readLine()) != null) {
	            	row = l.split("[,\t]+");
	            	edges.add(row);
	            }
	        } finally {
	            if (inputStream != null) {
	                inputStream.close();
	            }
	        }
		} else { // kgml xml file
			edges = parseKGML(fileName);
		}
        
		if(edges != null)
			return edges.toArray(new String[edges.size()][]);
		else
			return null;
	}

	/**
	 * Parse xml kgml from KEGG
	 * @param fileName
	 * @return
	 */
	private static ArrayList<String[]> parseKGML(String fileName) {
		ArrayList<String[]> edges = new ArrayList<String[]>();
		
		HashMap<Integer, ArrayList<String>> entries = new HashMap<Integer, ArrayList<String>>();
		ArrayList<int[]> relations = new ArrayList<int[]>();
		
		try {
			 
			File fXmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
		 
			doc.getDocumentElement().normalize();
		 		 
			// parse gene entry
			NodeList nList = doc.getElementsByTagName("entry");
		 
			if(nList.getLength() == 0) {
				JOptionPane.showMessageDialog(null, "Does this xml follow the KGML format?\n" +
						"Calculate without prior knowledge.");
				return null;
			}
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
		 
				Node nNode = nList.item(temp);
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
					Element eElement = (Element) nNode;
		 
					if(eElement.getAttribute("type").equals("gene")) {
						int id = Integer.parseInt(eElement.getAttribute("id"));
						
						Element t = (Element) eElement.getElementsByTagName("graphics").item(0);
						String names = t.getAttribute("name");
												
						ArrayList<String> genes = breakGenes(names);

						entries.put(id, genes);
					}
				}
			}
			
			// parse group entry
			for (int temp = 0; temp < nList.getLength(); temp++) {
		 
				Node nNode = nList.item(temp);
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
					Element eElement = (Element) nNode;
		 
					if(eElement.getAttribute("type").equals("group")) {
						int id = Integer.parseInt(eElement.getAttribute("id"));
						
						ArrayList<String> genes = new ArrayList<String>(); 
						
						NodeList cps = eElement.getElementsByTagName("component");
						for(int i=0; i<cps.getLength(); i++) {
							Element t = (Element) cps.item(i);
							int cpid = Integer.parseInt(t.getAttribute("id"));
							// existing entry
							if(entries.containsKey(cpid)) {
								genes.addAll(entries.get(cpid));
							}
						}

						if(genes.size()>0)
							entries.put(id, genes);
					}
				}
			}
			
			// parse relation
			nList = doc.getElementsByTagName("relation");
		 
			if(nList.getLength() == 0) {
				JOptionPane.showMessageDialog(null, "Does this xml follow the KGML format?\n" +
						"Calculate without prior knowledge.");
				return null;
			}
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
		 
				Node nNode = nList.item(temp);
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
					Element eElement = (Element) nNode;
		 
					int id1 = Integer.parseInt(eElement.getAttribute("entry1"));
					int id2 = Integer.parseInt(eElement.getAttribute("entry2"));
					
					int[] pair = {id1, id2};
					relations.add(pair);
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Does this xml follow the KGML format?\n" +
					"Calculate without prior knowledge.");
			e.printStackTrace();
			return null;
		}
		
		// expand the edges
		for(int i=0; i<relations.size(); i++) {
			int nd1 = relations.get(i)[0];
			int nd2 = relations.get(i)[1];
			
			if(entries.containsKey(nd1) && entries.containsKey(nd2)) {
				ArrayList<String> names1 = entries.get(nd1);
				ArrayList<String> names2 = entries.get(nd2);
				
				for(int m=0; m<names1.size(); m++)
					for(int n=0; n<names2.size(); n++) {
						String[] anEdge = {names1.get(m), names2.get(n)};
						edges.add(anEdge);
					}
			}
		}
		
		return edges;
	}

	/**
	 * Split multiple genes in one KEGG entry into multiple
	 * @param names
	 * @return
	 */
	private static ArrayList<String> breakGenes(String names) {
		ArrayList<String> genes = new ArrayList<String>();
		
		names = names.replace("...", "");
		String[] subNames = names.split(", ");
		
		genes.addAll(Arrays.asList(subNames));
		
		return genes;
	}

	public static String[] readGeneList(InputStream demoGeneStream) throws IOException {
		ArrayList<String> genes = new ArrayList<String>();

		BufferedReader inputStream = null;
		
		try {
			inputStream = new BufferedReader(new InputStreamReader(demoGeneStream, "UTF-8"));
			
			String l;

            while ((l = inputStream.readLine()) != null) {
            	genes.add(l);
            }
		} finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
		
		return genes.toArray(new String[genes.size()]);
	}

	public static double[][] readData(InputStream demoFileStream) throws IOException {
		ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
		
		BufferedReader inputStream = null;
		
		try {
			inputStream = new BufferedReader(new InputStreamReader(demoFileStream, "UTF-8"));

			String l;
            
            while ((l = inputStream.readLine()) != null) {
            	String[] row;
            	row = l.split("[,\t]+");
            	
            	ArrayList<Double> aRow = new ArrayList<Double>();
            	for(int i=0; i<row.length; i++)
            		aRow.add(Double.parseDouble(row[i]));
            	
            	data.add(aRow);
            }
		} finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
            
		double[][] dataArray = new double[data.get(0).size()][data.size()];
		for(int i=0; i<dataArray.length; i++)
			for(int j=0; j<dataArray[0].length; j++) {
				dataArray[i][j] = data.get(j).get(i);
			}
		
		return dataArray;
	}

	public static String[][] readKnowledge(InputStream demoPriorStream) throws IOException {
		ArrayList<String[]> edges = new ArrayList<String[]>();

		BufferedReader inputStream = null;
		
		try {
			inputStream = new BufferedReader(new InputStreamReader(demoPriorStream, "UTF-8"));
			
			String l;
			String[] row;
            while ((l = inputStream.readLine()) != null) {
            	row = l.split("[,\t]+");
            	edges.add(row);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        
		return edges.toArray(new String[edges.size()][]);
	}

}
