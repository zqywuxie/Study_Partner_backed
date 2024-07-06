package com.example.studypartner.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wuxie
 * @date 2024/1/15 9:31
 * @description 该文件的描述 todo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationRequest {
	private String ip;
	private Location location;
	private AdInfo ad_info;




	@Data
	public static class Location {
		private double lat;
		private double lng;

	}

	@Data
	public static class AdInfo {
		private String nation;
		private String province;
		private String city;
		private String district;
	}

}
