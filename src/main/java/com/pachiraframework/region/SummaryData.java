package com.pachiraframework.region;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import lombok.Data;

@Data
public class SummaryData {
	private String code;
	private Integer sum;
	private Map<String, List<SummaryData>> childrens = Maps.newConcurrentMap();
}
