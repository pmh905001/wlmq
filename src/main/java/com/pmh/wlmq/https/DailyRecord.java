package com.pmh.wlmq.https;

import lombok.Data;

@Data
public class DailyRecord {

	private String loadDateStr;
	private String keyMatched;
	private String trainNumber;
	private String totalWeight;
	private String location;
	private String canReceiveMsg;
	private String mobile;
	
	private int row;

}