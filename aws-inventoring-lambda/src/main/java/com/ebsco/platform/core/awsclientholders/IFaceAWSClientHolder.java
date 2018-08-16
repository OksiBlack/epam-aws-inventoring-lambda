package com.ebsco.platform.core.awsclientholders;

import com.amazonaws.regions.Regions;

public interface IFaceAWSClientHolder {
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
