package com.example.studypartner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.studypartner.domain.dto.GoodsDTO;
import com.example.studypartner.domain.entity.Goods;
import com.example.studypartner.mapper.GoodsMapper;
import com.example.studypartner.service.GoodsService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wuxie
 * @description 针对表【goods(商品表)】的数据库操作Service实现
 * @createDate 2024-01-15 16:46:34
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods>
		implements GoodsService {

	@Resource
	private RedissonClient redissonClient;

	/**
	 * 获得所有商品
	 *
	 * @return 所有商品
	 */
	@Override
	public Page<Goods> getAll(GoodsDTO goodsDTO) {
		//分页查询商品，goodsDTO为查询条件
		int pageSize = goodsDTO.getPageSize();
		int pageNum = goodsDTO.getPageNum();

		String searchText = goodsDTO.getSearchText();
		//searchText不为空时，根据商品名称或者商品描述模糊查询
		LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
		if (searchText != null && !searchText.isEmpty()) {
			wrapper.like(Goods::getName, searchText)
					.or().like(Goods::getDescription, searchText);
		}
		return this.page(new Page<>(pageNum, pageSize), wrapper);
	}

	/**
	 * 根据商品id获得商品
	 *
	 * @param goodsId 商品id
	 * @return 商品
	 */

	@Override
	public Goods getById(Long goodsId) {
		//根据商品id获得商品
		return this.getById(goodsId);
	}

	@Override
	public void reduceStock(Long goodsId, Integer num) {
		//减少商品库存
		//使用redisson锁更新库存
		//获得锁
		RLock lock = redissonClient.getLock("goodsId:" + goodsId);
		lock.lock();
		try {
			//获得商品
			Goods goods = this.getById(goodsId);
			//判断商品是否存在
			if (goods == null) {
				throw new RuntimeException("商品不存在");
			}
			//更新库存
			//库存不足
			if (goods.getNum() < num) {
				throw new RuntimeException("库存不足");
			}
			goods.setNum(goods.getNum() - num);
			//更新商品
			this.updateById(goods);
		} finally {
			//根据每个线程获得的锁释放锁，防止误删，线程安全
			if (lock.isHeldByCurrentThread()) {
				System.out.println("unLock: " + Thread.currentThread().getId());
				lock.unlock();
			}
		}
	}
}




