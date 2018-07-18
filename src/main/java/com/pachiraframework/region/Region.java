package com.pachiraframework.region;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Region {
	private String code;
	private Integer level;
	private String name;
	private String parent;
}
