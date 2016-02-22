
// Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

public class Utils {

	 public static Region getRegion() {
	        Region region = Regions.getCurrentRegion();

	        // This code is for local testing only.
	        if (region == null) {
	            region = Region.getRegion(Regions.US_WEST_1);
	        }

	        System.out.printf("Utils.getRegion() returned %s. %n ", region.getName());
	        return region;
	 }
}
