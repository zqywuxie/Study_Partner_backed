package com.example.studypartner.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wuxie
 * @date 2024/1/14 13:46
 * @description 该文件的描述 todo
 */


@Data
public class LocationRequest implements Serializable {

	private Double x;

	private Double y;
}
