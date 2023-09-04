/**
 * se.vti.matsim.dynameq
 * 
 * Copyright (C) 2023 by Filip Kristofferson (VTI) and Gunnar Flötteröd (VTI, LiU).
 * 
 * VTI = Swedish National Road and Transport Institute
 * LiU = Linköping University, Sweden
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>. See also COPYING and WARRANTY file.
 */
package se.vti.matsim.dynameq.network;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.NetworkWriter;

/**
 * 
 * @author FilipK
 *
 */
public class CreateNetwork {

	private static final Logger log = Logger.getLogger(CreateNetwork.class);

	public static void main(String[] args) throws IOException {

		final String BASE_NETWORK_FILE_PATH = "data/dynameq_files/nuläge dynafile/STHLM_v46_RT_1_20_base.dqt";
		final String FIXED_BASE_NETWORK_FILE_PATH = "data/modified_dynameq_files/STHLM_v46_RT_1_20_base_FIXED.txt";
		final String OUTPUT_NETWORK_FILE = "data/produced_matsim_files/dynameq_network.xml";

		BaseNetworkFileCleaner baseNetworkFileCleaner = new BaseNetworkFileCleaner();
		baseNetworkFileCleaner.removeLeadingWhitespacesAndStars(BASE_NETWORK_FILE_PATH, FIXED_BASE_NETWORK_FILE_PATH);

		BaseNetworkFileReader baseNetworkFileReader = new BaseNetworkFileReader(FIXED_BASE_NETWORK_FILE_PATH);

		Network network = NetworkUtils.createNetwork();

		baseNetworkFileReader.readAndAddNodes(network, "NODES", "CENTROIDS");
		baseNetworkFileReader.readAndAddLinks(network, "LINKS", "LANE_PERMS");
		baseNetworkFileReader.readAndAddCentroids(network, "CENTROIDS", "LINKS");
		baseNetworkFileReader.readAndAddVirtualLinks(network, "VIRTUAL_LINKS", "MOVEMENTS");
		baseNetworkFileReader.addSuperCentroids(network);

		new NetworkCleaner().run(network);

		new NetworkWriter(network).write(OUTPUT_NETWORK_FILE);

		log.info("Number of " + Utils.LinkTypeConstants.LINK + " in network: "
				+ getNumberOfLinksByType(network.getLinks(), Utils.LinkTypeConstants.LINK));
		log.info("Number of " + Utils.LinkTypeConstants.VIRTUAL_LINK + " in network: "
				+ getNumberOfLinksByType(network.getLinks(), Utils.LinkTypeConstants.VIRTUAL_LINK));
		log.info("Number of " + Utils.LinkTypeConstants.SUPER_VIRTUAL_LINK + " in network: "
				+ getNumberOfLinksByType(network.getLinks(), Utils.LinkTypeConstants.SUPER_VIRTUAL_LINK));

		log.info("Number of " + Utils.NodeTypeConstants.NODE + " in network: "
				+ getNumberOfNodesByType(network.getNodes(), Utils.NodeTypeConstants.NODE));
		log.info("Number of " + Utils.NodeTypeConstants.CENTROID + " in network: "
				+ getNumberOfNodesByType(network.getNodes(), Utils.NodeTypeConstants.CENTROID));
		log.info("Number of " + Utils.NodeTypeConstants.SUPER_CENTROID + " in network: "
				+ getNumberOfNodesByType(network.getNodes(), Utils.NodeTypeConstants.SUPER_CENTROID));
	}

	private static int getNumberOfLinksByType(Map<Id<Link>, ? extends Link> links, String linkType) {
		int counter = 0;
		for (Link link : links.values()) {
			if (linkType.equals(link.getAttributes().getAttribute(Utils.NODE_LINK_TYPE_ATTRIBUTE_KEY))) {
				counter++;
			}
		}
		return counter;
	}

	private static int getNumberOfNodesByType(Map<Id<Node>, ? extends Node> nodes, String nodeType) {
		int counter = 0;
		for (Node node : nodes.values()) {
			if (nodeType.equals(node.getAttributes().getAttribute(Utils.NODE_LINK_TYPE_ATTRIBUTE_KEY))) {
				counter++;
			}
		}
		return counter;
	}
}
