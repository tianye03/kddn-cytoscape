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

import java.net.URL;
import java.util.Properties;

import javax.help.HelpSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyHelpBroker;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

/**
 * App entrance
 * @author Ye Tian
 *
 */
public class CyActivator extends AbstractCyActivator {

	protected static KddnResultsPanel kddnResultsPanel;
	protected static int totalNumberRun = 0;
	protected static KddnConfigurePanel kddnConfigurePanel;
	protected static CyHelpBroker cyHelpBroker;
	
	public CyActivator() {
		super();
	}
	
	@Override
	public void start(BundleContext bc) throws Exception {
		
		CySwingApplication cytoscapeDesktopService = getService(bc,CySwingApplication.class);

		// register visualization related managers to Cytoscape
		// network creation
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyNetworkNaming cyNetworkNamingServiceRef = getService(bc,CyNetworkNaming.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		
		// progress dialog
		DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);
		
		// view creation
		CyNetworkViewFactory networkViewFactory = getService(bc, CyNetworkViewFactory.class);
		CyNetworkViewManager networkViewManager = getService(bc, CyNetworkViewManager.class);
  
		// visual style manager
		VisualMappingManager vmmServiceRef = getService(bc,VisualMappingManager.class);
		VisualStyleFactory vsfServiceRef = getService(bc,VisualStyleFactory.class);

		// visual mapping functions
		VisualMappingFunctionFactory vmfFactoryC = getService(bc,VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		VisualMappingFunctionFactory vmfFactoryD = getService(bc,VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		VisualMappingFunctionFactory vmfFactoryP = getService(bc,VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");

		// layout manager
		CyLayoutAlgorithmManager clamRef = getService(bc, CyLayoutAlgorithmManager.class);
		
		// main control panel added to the left of Cytoscape panel
		kddnConfigurePanel = new KddnConfigurePanel(cyNetworkManagerServiceRef,
				cyNetworkNamingServiceRef,cyNetworkFactoryServiceRef,
				dialogTaskManager, networkViewFactory, networkViewManager,
				vmmServiceRef, vsfServiceRef, vmfFactoryC,
				vmfFactoryD, vmfFactoryP, clamRef);
		registerService(bc,kddnConfigurePanel,CytoPanelComponent.class, new Properties());
		
		// results panel added to the right of Cytoscape panel
		kddnResultsPanel = new KddnResultsPanel(cytoscapeDesktopService);
		registerService(bc, kddnResultsPanel, CytoPanelComponent.class, new Properties());
		
		// menu items
		CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
		
		KddnMenuAction runMenu = new KddnMenuAction(cyApplicationManager, "Run analysis", cytoscapeDesktopService, kddnConfigurePanel);
		registerAllServices(bc, runMenu, new Properties());
	
		KddnMenuAction helpMenu = new KddnMenuAction(cyApplicationManager, "Help", cytoscapeDesktopService, kddnConfigurePanel);
		registerAllServices(bc, helpMenu, new Properties());
		
		KddnMenuAction aboutMenu = new KddnMenuAction(cyApplicationManager, "About", cytoscapeDesktopService, kddnConfigurePanel);
		registerAllServices(bc, aboutMenu, new Properties());
		
		// help doc
		cyHelpBroker = getService(bc, CyHelpBroker.class);

		final String HELP_SET_NAME = "/help/jhelpset";
		final ClassLoader classLoader = getClass().getClassLoader();
		URL helpSetURL;
		try {
			helpSetURL = HelpSet.findHelpSet(classLoader, HELP_SET_NAME);
			final HelpSet newHelpSet = new HelpSet(classLoader, helpSetURL);
			cyHelpBroker.getHelpSet().add(newHelpSet);
		} catch (final Exception e) {
			System.err.println("Could not find help set: \"" + HELP_SET_NAME + ".");
		}
	}

}
