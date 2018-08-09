package com.ebsco.platform.infrastructure.core.awsclients;

import com.amazonaws.regions.Regions;

public interface IFaceAWSClient {
	void shutdown();

/**
 * Method uses constant names and not there representation like Regions.fromName
 * @param reg
 * @return
 */
	static Regions formRegion(String reg){
		return Regions.valueOf(reg); //use constant names
	}
}
