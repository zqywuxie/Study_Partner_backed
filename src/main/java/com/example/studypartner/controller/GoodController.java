package com.example.studypartner.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.studypartner.common.CommonResult;
import com.example.studypartner.domain.dto.GoodsDTO;
import com.example.studypartner.domain.entity.Goods;
import com.example.studypartner.service.GoodsService;
import com.example.studypartner.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wuxie
 * @date 2024/1/15 16:43
 * @description 该文件的描述 todo
 */

//商品相关接口

@RequestMapping("/good")
@RestController
@Slf4j
public class GoodController {

	@Resource
	private GoodsService goodsService;


	/**
	 * @description: 商品列表接口
	 * @param: []
	 * @return: com.example.studypartner.common.CommonResult<java.util.List < com.example.studypartner.domain.entity.Goods>>
	 */
	@GetMapping("/list")
	public CommonResult<Page<Goods>> list(@RequestParam GoodsDTO goodsDTO) {
		Page<Goods> goodsList = goodsService.getAll(goodsDTO);
		return ResultUtils.success(goodsList);
	}

	//根据商品id获得商品信息接口
	@RequestMapping("/getById")
	public CommonResult<Goods> getById(Integer id) {
		Goods goods = goodsService.getById(id);
		return ResultUtils.success(goods);
	}

	//减少商品库存接口
	@PostMapping("/reduceStock")
	public CommonResult<Boolean> reduceStock(Long id, Integer amount) {
		goodsService.reduceStock(id, amount);
		return ResultUtils.success(true);
	}
}
