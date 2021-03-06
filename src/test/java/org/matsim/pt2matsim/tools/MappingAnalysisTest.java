/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.pt2matsim.tools;

import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt2matsim.config.OsmConverterConfigGroup;
import org.matsim.pt2matsim.config.PublicTransitMappingConfigGroup;
import org.matsim.pt2matsim.gtfs.GtfsConverter;
import org.matsim.pt2matsim.mapping.PTMapper;
import org.matsim.pt2matsim.mapping.PTMapperImpl;
import org.matsim.pt2matsim.osm.OsmMultimodalNetworkConverter;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.Vehicles;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @author polettif
 */
public class MappingAnalysisTest {

	private TransitSchedule schedule;
	private Vehicles vehicles;
	private Network network;
	private GtfsConverter gtfsConverter;

	private String input = "test/analysis/";


	@Before
	public void prepare() {
		// convert schedule
		schedule = ScheduleTools.createSchedule();
		vehicles = VehicleUtils.createVehiclesContainer();
		gtfsConverter = new GtfsConverter(schedule, vehicles, TransformationFactory.getCoordinateTransformation("WGS84", "EPSG:2032"));
		gtfsConverter.run(input + "addisoncounty-vt-us-gtfs/", "all");
		ScheduleTools.writeTransitSchedule(gtfsConverter.getSchedule(), input + "mts/schedule_unmapped.xml.gz");

		// read network
		/*convert from osm
		OsmConverterConfigGroup osmConfig = OsmConverterConfigGroup.createDefaultConfig();
		osmConfig.setOutputCoordinateSystem("EPSG:2032");
		osmConfig.setOsmFile(input+"osm/addison.osm");
		osmConfig.setOutputNetworkFile(input+"network/addison.xml.gz");
		osmConfig.setMaxLinkLength(20);

		new OsmMultimodalNetworkConverter(osmConfig).run();
		*/

		network = NetworkTools.readNetwork(input + "network/addison.xml.gz");
	}

	@Before
	public void runMapping() {
		PublicTransitMappingConfigGroup ptmConfig = PublicTransitMappingConfigGroup.createDefaultConfig();

		new PTMapperImpl(ptmConfig, schedule, network).run();

		NetworkTools.writeNetwork(network, input+"output/addison_network.xml.gz");
		ScheduleTools.writeTransitSchedule(schedule, input+"output/addison_schedule.xml.gz");
	}

	@Test
	public void analysis() {
		new File(input + "output/").mkdirs();

		MappingAnalysis analysis = new MappingAnalysis(schedule, network, gtfsConverter.getReferencedShapes());

		analysis.run();
		analysis.writeAllDistancesCsv(input+"output/DistancesAll.csv");
		analysis.writeQuantileDistancesCsv(input+"output/DistancesQuantile.csv");
	}
}