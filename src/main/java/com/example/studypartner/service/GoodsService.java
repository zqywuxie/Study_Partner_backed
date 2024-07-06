package com.example.studypartner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.studypartner.domain.dto.GoodsDTO;
import com.example.studypartner.domain.entity.Goods;

import java.util.List;

/**
* @author wuxie
* @description 针对表【goods(商品表)】的数据库操作Service
* @createDate 2024-01-15 16:46:34
*/
public interface GoodsService extends IService<Goods> {

	/**
	 * 获得所有商品
	 * @return 所有商品
	 */

	Page<Goods> getAll(GoodsDTO goodsDTO);

	/**
	 * 根据商品id获取商品
	 * @param goodsId 商品id
	 * @return 商品
	 */
	Goods getById(Long goodsId);

	/**
	 * 根据商品id减少库存
	 * @param goodsId 商品id
	 * @param num 减少数量
	 */
	void reduceStock(Long goodsId, Integer num);
}
