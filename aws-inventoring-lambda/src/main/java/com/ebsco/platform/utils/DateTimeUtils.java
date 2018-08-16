package com.ebsco.platform.utils;

import java.time.Instant;

public class DateTimeUtils {



public static Long getNowMillis(){
	return Instant.now().toEpochMilli();
}

public static Long getNowSeconds(){
	return Instant.now().getEpochSecond();
}

public static Long getMinSeconds(){
	return Instant.EPOCH.getEpochSecond();
}

public static  Long getEpochMillis(){
	return  Instant.EPOCH.toEpochMilli();
}

public static Long getMaxSeconds(){
	return Instant.MAX.getEpochSecond();
}


}
