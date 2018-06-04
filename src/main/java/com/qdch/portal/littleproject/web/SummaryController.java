package com.qdch.portal.littleproject.web;

import com.qdch.portal.common.jdbc.datasource.DynamicDataSource;
import com.qdch.portal.common.web.BaseController;
import com.qdch.portal.littleproject.dao.CustomerAgeModelDao;
import com.qdch.portal.littleproject.dao.CustomerClassifyModelDao;
import com.qdch.portal.littleproject.dao.CustomerCountModelDao;
import com.qdch.portal.littleproject.dao.CustomerNumberModelDao;
import com.qdch.portal.littleproject.dao.EntryAndExitCapitalModelDao;
import com.qdch.portal.littleproject.dao.InterestRateModelDao;
import com.qdch.portal.littleproject.dao.ProductCountModelDao;
import com.qdch.portal.littleproject.dao.ProductDistributeModelDao;
import com.qdch.portal.littleproject.dao.ProductTrendModelDao;
import com.qdch.portal.littleproject.dao.QuotationModelDao;
import com.qdch.portal.littleproject.dao.SedimentaryCapitalModelDao;
import com.qdch.portal.littleproject.dao.TradeAmountModelDao;
import com.qdch.portal.littleproject.dao.TradeCountModelDao;
import com.qdch.portal.littleproject.dao.TradeMarketModelDao;
import com.qdch.portal.littleproject.dao.TradeRtioModelDao;
import com.qdch.portal.littleproject.entity.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.text.DecimalFormat;
import java.util.*;

/**
 * 总况
 * 
 * @author gaozhao
 * @time 2018年4月13日
 */
@Controller
public class SummaryController extends BaseController {
	@Autowired
	public TradeAmountModelDao tradeAmountModelDao;// 交易额
	@Autowired
	public TradeMarketModelDao tradeMarketModelDao;// 交易市场
	@Autowired
	public TradeCountModelDao tradeCountModelDao;//交易额统计
	@Autowired
	public CustomerNumberModelDao customerNumberModelDao;//客户数
	@Autowired
	public CustomerClassifyModelDao customerClassifyModelDao;//客户分类
	@Autowired
	public CustomerCountModelDao customerCountModelDao;//客户统计
	@Autowired
	public CustomerAgeModelDao customerAgeModelDao;//客户年龄
	@Autowired
	public ProductDistributeModelDao productDistributeModelDao;//金融类——产品分布
	@Autowired
	public ProductTrendModelDao productTrendModelDao;//产品趋势
	@Autowired
	public InterestRateModelDao interestRateModelDao;//平均年化利率
	@Autowired
	public ProductCountModelDao productCountModelDao;//产品统计
	@Autowired
	public SedimentaryCapitalModelDao sedimentaryCapitalModelDao;//商品类——沉淀资金
	@Autowired
	public EntryAndExitCapitalModelDao entryAndExitCapitalModelDao;//出入金
	@Autowired
	public QuotationModelDao quotationModelDao;//各指数行情
	@Autowired
	public TradeRtioModelDao tradeRtioModelDao;//产品交易额占比





	/**
	 * 交易额
	 * 
	 * @time 2018年4月13日
	 * @author gaozhao
	 * @param request
	 * @param response
	 * @return
	 */
	

	@RequestMapping(value = { "${portalPath}/littleproject/tradeAmount" })
	@ResponseBody
	public String tradeAmount(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			//切换数据源
			DynamicDataSource.setInsightDataSource();
			String type = request.getParameter("type");
			List<TradeAmountModel> lists1 = new ArrayList<TradeAmountModel>(500);

			LittleProjectDto dto = new LittleProjectDto();
			// 交易市场
			List<TradeMarketModel> tradelist = tradeMarketModelDao
					.tradeMarket();
			//切回原来的数据源
			DynamicDataSource.removeDataSourceKey();
			TradeMarketModel oneMarket = new TradeMarketModel();
			tradelist.add(oneMarket);

			// 时间集合
			List<String> times = new ArrayList<String>(500);
			// 交易市场集合
			List<LittleProjectEntity> res = new ArrayList<LittleProjectEntity>();
			int n = 1;
			if ("day".equals(type)) {

				lists1 = tradeAmountModelDao.tradeDay();// 按天查询

			} else if ("week".equals(type)) {

				lists1 = tradeAmountModelDao.tradeWeek();// 按周查询
			} else if ("month".equals(type)) {

				lists1 = tradeAmountModelDao.tradeMonth();// 按月查询
			}

			// 把查询出来的市场加到市场集合中
			if (tradelist != null && tradelist.size() > 0) {
				for (TradeMarketModel o : tradelist) {
					LittleProjectEntity entity = new LittleProjectEntity();
					if (o.equals(oneMarket)) {
						entity.setName("总量");
					} else {
						entity.setName(o.getJysinfo());
					}
					res.add(entity);
				}
			}

			// 交易市场的交易额一些信息
			if (res != null && res.size() > 0) {
				for (LittleProjectEntity s : res) {

					List<String> shiChan = new ArrayList<String>();
					if (lists1 != null && lists1.size() > 0) {

						for (TradeAmountModel o : lists1) {
							if (o.getJysinfo() == null) {
								continue;
							}
							if (o.getJysinfo().equals(s.getName())) {
								if ("day".equals(type)) {
									shiChan.add(o.getFvalue() + "");
								} else if ("week".equals(type)
										|| "month".equals(type)) {
									shiChan.add(o.getSum() + "");
								}
							}
						}

					}
					s.setLists(shiChan);
				}
			}
			// 删除空list
			Iterator<LittleProjectEntity> it = res.iterator();
			while (it.hasNext()) {
				LittleProjectEntity litteEntity = it.next();

				if (litteEntity.getLists() == null || litteEntity.getLists().isEmpty()
						|| litteEntity.getLists().size() == 0) {
					it.remove();
				}

			}

			// 获取时间
			if (res != null && res.size() > 0) {
				for (LittleProjectEntity s : res) {

					if (lists1 != null && lists1.size() > 0) {

						for (TradeAmountModel o : lists1) {
							if (o.getJysinfo() == null) {
								continue;
							}
							if ("day".equals(type) || "week".equals(type)) {
								if (o.getJysinfo().equals(s.getName())
										&& n== 1) {
									times.add(o.getVday());
								}
							} else if ("month".equals(type)) {
								if (o.getJysinfo().equals(s.getName())
										&& n == 1) {
									times.add(o.getVmonth());
								}
							}

						}
					}
					n = 2;
				}
			}

			dto.setTimes(times.toArray());// 把时间加到对象dto中
			// 文化产权分离
			String jys = "";
			List<LittleProjectEntity> culturalRights = new ArrayList<LittleProjectEntity>(500);
			for (LittleProjectEntity littleProjectEntity : res) {
				jys = littleProjectEntity.getName();
				if (jys.contains("文化产权")) {
					culturalRights.add(littleProjectEntity);
					res.remove(littleProjectEntity);
				}
			}
			dto.setEntities(res);// 把其他市场的信息加到对象dto中
			dto.setCulturalRights(culturalRights);// 文化产权
			if (lists1 == null || lists1.isEmpty()) {
				return this.resultSuccessData(request, response, "", null);
			} else {
				return this.resultSuccessData(request, response, "", dto);
			}

		} catch (Exception e) {
			logger.warn("总况——交易额", e);
			return this.resultFaliureData(request, response, "", null);
		}

	}

	/**
	 * 交易额统计
	 *
	 * @time 2018年4月13日
	 * @author 高照
	 * @param request
	 * @param response
	 * @return
	 */


	@RequestMapping(value = { "${portalPath}/littleproject/jiaoYiAmount" })
	@ResponseBody
	public String jiaoYiAmount(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			DynamicDataSource.setInsightDataSource();
			LittleProjectDto dto = new LittleProjectDto();
			// 交易额统计
			List<TradeCountModel> tradeCountList = tradeCountModelDao
					.getTradeCountModel();
			// 交易市场
			List<TradeMarketModel> tradelist = tradeMarketModelDao
					.tradeMarket();
			//目的为了把‘总量’加进去
			TradeMarketModel oneMarket = new TradeMarketModel();
			tradelist.add(oneMarket);
			// 交易市场集合
			List<LittleProjectEntity> res = new ArrayList<LittleProjectEntity>();
			if (tradelist != null && tradelist.size() > 0) {
				for (TradeMarketModel o : tradelist) {
					LittleProjectEntity entity = new LittleProjectEntity();
					if (o.equals(oneMarket)) {
						entity.setName("总量");
					} else {
						entity.setName(o.getJysinfo());
					}
					res.add(entity);
				}
			}
			if (res != null && res.size() > 0) {
				for (LittleProjectEntity s : res) {

					List<String> shiChan = new ArrayList<String>(100);
					if (tradeCountList != null && tradeCountList.size() > 0) {

						for (TradeCountModel o : tradeCountList) {
							if (o.getJysinfo().equals(s.getName())) {
								shiChan.add(o.getBz() + "");
								shiChan.add(o.getBy() + "");
								shiChan.add(o.getBn() + "");
								shiChan.add(o.getLj() + "");

							}
						}

					}
					s.setLists(shiChan);
				}
			}
			// 删除空list
			Iterator<LittleProjectEntity> it = res.iterator();
			while (it.hasNext()) {
				LittleProjectEntity s = it.next();

				if (s.getLists() == null || s.getLists().isEmpty()
						|| s.getLists().size() == 0) {
					it.remove();
				}

			}

			dto.setEntities(res);
			DynamicDataSource.removeDataSourceKey();
			if (tradeCountList == null || tradeCountList.size() < 0) {

				return this.resultSuccessData(request, response, "", null);
			} else {
				return this.resultSuccessData(request, response, "", dto);
			}

		} catch (Exception e) {
			logger.warn("总况——交易额统计", e);
			return this.resultFaliureData(request, response, "", null);
		}
	}

	/**
	 * 总况——总量——用户 客户数
	 * 
	 * @author gaozhao
	 * @time 2018年4月16日
	 */
	

	@RequestMapping(value = { "${portalPath}/littleproject/yongHuShu" })
	@ResponseBody
	public String yongHuShu(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			DynamicDataSource.setInsightDataSource();
			String type = request.getParameter("type");
			List<CustomerNumberModel> lists = new ArrayList<CustomerNumberModel>(500);
			if ("day".equals(type)) {
				lists = customerNumberModelDao.getCustomerNumberModelDao();
			} else if ("week".equals(type)) {
				lists = customerNumberModelDao.getCustomerNumberModelDao2();
			} else if ("month".equals(type)) {
				lists = customerNumberModelDao.getCustomerNumberModelDao3();
			}
			// 要返回的对象
			LittleProjectDto dto = new LittleProjectDto();
			// 交易市场
			List<TradeMarketModel> tradelist = tradeMarketModelDao
					.tradeMarket();
			TradeMarketModel oneMarket = new TradeMarketModel();
			oneMarket.setJysinfo("总量");
			tradelist.add(oneMarket);
			// 时间集合
			List<String> times = new ArrayList<String>();
			// 交易市场集合
			List<LittleProjectEntity> res = new ArrayList<LittleProjectEntity>();
			int n = 1;
			// 给交易市场集合加入市场对象
			if (tradelist != null && tradelist.size() > 0) {
				for (TradeMarketModel o : tradelist) {
					LittleProjectEntity aa = new LittleProjectEntity();
					aa.setName(o.getJysinfo());
					res.add(aa);
				}
			}

			// 获取市场

			if (res != null && res.size() > 0) {
				for (LittleProjectEntity s : res) {

					List<String> shiChan = new ArrayList<String>(100);
					if (lists != null && lists.size() > 0) {

						for (CustomerNumberModel o : lists) {
							if(o.getJysinfo() == null){
								continue;
							}
							if (o.getJysinfo().equals(s.getName())) {

								shiChan.add(o.getFvalue() + "");

							}
						}

					}
					s.setLists(shiChan);
				}
			}
			// 删除空list
			Iterator<LittleProjectEntity> it = res.iterator();
			while (it.hasNext()) {
				LittleProjectEntity s = it.next();

				if (s.getLists() == null || s.getLists().isEmpty()
						|| s.getLists().size() == 0) {
					it.remove();
				}

			}

			// 获取时间
			if (res != null && res.size() > 0) {
				for (LittleProjectEntity s : res) {

					if (lists != null && lists.size() > 0) {

						for (CustomerNumberModel o : lists) {
							if(o.getJysinfo() == null){
								continue;
							}
							if (o.getJysinfo().equals(s.getName()) && n == 1) {
								times.add(o.getVday());

							}

						}
					}
					n = 2;
				}
			}
			dto.setTimes(times.toArray());
			dto.setEntities(res);
			DynamicDataSource.removeDataSourceKey();
			if (lists == null) {
				return this.resultSuccessData(request, response, "", null);
			} else {
				return this.resultSuccessData(request, response, "", dto);
			}

		} catch (Exception e) {
			logger.warn("总况——总量——用户 客户数", e);
			return this.resultFaliureData(request, response, "", null);
		}

	}

	/**
	 * 总况——总量——用户——金融资产类-客户分类
	 *
	 * @author gaozhao
	 * @time 2018年4月16日
	 */
	

	@RequestMapping(value = { "${portalPath}/littleproject/keHuFenLei" })
	@ResponseBody
	public String keHuFenLei(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			DynamicDataSource.setInsightDataSource();

			KeHuFenLei customer = new KeHuFenLei();
			List<CustomerClassifyModel> customerList = customerClassifyModelDao
					.getCustomerClassifyModelDao();
			if (customerList != null && customerList.size() > 0) {
				for (CustomerClassifyModel o : customerList) {
					customer.setGrs(o.getGrkhs() + "");
					customer.setJgs(o.getJgkhs() + "");
				}
			}
			DynamicDataSource.removeDataSourceKey();
			if (customerList == null) {
				return this.resultSuccessData(request, response, "", null);
			} else {
				return this.resultSuccessData(request, response, "", customer);
			}

		} catch (Exception e) {
			logger.warn("总况——总量——用户——金融资产类-客户分类", e);
			return this.resultFaliureData(request, response, "", null);
		}

	}

	/**
	 * 总况——总量——用户——客户统计
	 *
	 * @author gaozhao
	 * @time 2018年4月16日
	 */
	
	@RequestMapping(value = { "${portalPath}/littleproject/keHuTongJi" })
	@ResponseBody
	public String keHuTongJi(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			DynamicDataSource.setInsightDataSource();
			LittleProjectDto dto = new LittleProjectDto();
			// 客户统计
			List<CustomerCountModel> tongji = customerCountModelDao
					.getCustomerCountModelDao();

			List<LittleProjectEntity> res = new ArrayList<LittleProjectEntity>();

			if (tongji != null && tongji.size() > 0) {
				for (CustomerCountModel o : tongji) {
					List<String> shichan = new ArrayList<String>();
					LittleProjectEntity entity = new LittleProjectEntity();
					shichan.add(o.getGrrzrkhs() + "/" + o.getJgrzrkhs());
					shichan.add(o.getGrtzrkhs() + "/" + o.getJgrtzrkhs());
					shichan.add(o.getGrkhs() + "/" + o.getJgkhs());
					entity.setName(o.getJysinfo());
					entity.setLists(shichan);
					res.add(entity);
				}

			}

			dto.setEntities(res);
			DynamicDataSource.removeDataSourceKey();
			if (tongji == null) {
				return this.resultSuccessData(request, response, "", null);
			} else {
				return this.resultSuccessData(request, response, "", dto);
			}

		} catch (Exception e) {
			logger.warn("总况——总量——用户——客户统计", e);
			return this.resultFaliureData(request, response, "", null);
		}
	}

	/**
	 * 总况——总量——用户——客户年龄
	 *
	 * @author gaozhao
	 * @time 2018年4月16日
	 */


	@RequestMapping(value = { "${portalPath}/littleproject/kehuAge" })
	@ResponseBody
	public String kehuAge(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			DynamicDataSource.setInsightDataSource();
			KeHuAge res = new KeHuAge();
			List<CustomerAgeModel> ages = customerAgeModelDao
					.getCustomerAgeModelDao();
			List<String> age = new ArrayList<String>(300);
			List<String> sum = new ArrayList<String>(300);
			if (ages != null && ages.size() > 0) {
				for (CustomerAgeModel o : ages) {

					res.setName(o.getJysinfo());

				}

			}
			if (ages != null && ages.size() > 0) {
				for (CustomerAgeModel o : ages) {

					age.add(o.getAgerange());
					sum.add(o.getSum() + "");

				}

			}

			res.setAge(age);
			res.setSum(sum);
			DynamicDataSource.removeDataSourceKey();
			if (ages == null) {
				return this.resultSuccessData(request, response, "", null);
			} else {
				return this.resultSuccessData(request, response, "", res);
			}
		} catch (Exception e) {
			logger.warn("总况——总量——用户——客户年龄", e);
			return this.resultFaliureData(request, response, "", null);
		}
	}

	/**
	 * 总况——金融资产类-产品分布
	 * 
	 * @author gaozhao
	 * @time 2018年4月17日
	 */
	
	@RequestMapping(value = { "${portalPath}/littleproject/chanpinfenbu" })
	@ResponseBody
	public String chanpinfenbu(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			DynamicDataSource.setInsightDataSource();
			List<ProductDistributeModel> lists = productDistributeModelDao
					.getProductDistributeModelDao();
			List<LittleProjectEntity> res = new ArrayList<LittleProjectEntity>();

			if (lists != null && lists.size() > 0) {
				for (ProductDistributeModel o : lists) {
					List<String> jihe = new ArrayList<String>();
					LittleProjectEntity entity = new LittleProjectEntity();
					jihe.add(o.getJys() + "");
					jihe.add(o.getCpsl() + "");
					entity.setName(o.getCplb());
					entity.setLists(jihe);
					res.add(entity);

				}
			}
			DynamicDataSource.removeDataSourceKey();
			if (lists == null) {
				return this.resultSuccessData(request, response, "", null);
			} else {
				return this.resultSuccessData(request, response, "", res);
			}

		} catch (Exception e) {
			logger.warn("总况——金融资产类-产品分布", e);
			return this.resultFaliureData(request, response, "", null);
		}

	}

	/**
	 * 总况——金融资产类-产品趋势
	 * 
	 * @author gaozhao
	 * @time 2018年4月17日
	 */
	
	@RequestMapping(value = { "${portalPath}/littleproject/chanpinqushi" })
	@ResponseBody
	public String chanpinqushi(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			DynamicDataSource.setInsightDataSource();
			List<ProductTrendModel> lists = new ArrayList<ProductTrendModel>(500);
			lists = productTrendModelDao.getProductTrendModelDao();
			List<ProductTrendModel> productList = productTrendModelDao.getProduct();
			List<String> times = new ArrayList<String>(200);
			LittleProjectDto dto = new LittleProjectDto();
			List<LittleProjectEntity> res = new ArrayList<LittleProjectEntity>();
			int n = 1;
			if (productList != null && productList.size() > 0) {
				for (ProductTrendModel o : productList) {
					LittleProjectEntity re = new LittleProjectEntity();
					re.setName(o.getCplb());
					res.add(re);
				}
			}
			if (res != null && res.size() > 0) {
				for (LittleProjectEntity s : res) {
					List<String> jihe = new ArrayList<String>(100);
					if (lists != null && lists.size() > 0) {
						for (ProductTrendModel o : lists) {
							if (o.getCplb().equals(s.getName())) {
								jihe.add(o.getCpsl() + "");
							}
						}
					}
					s.setLists(jihe);
				}
			}
			// 删除空list
			Iterator<LittleProjectEntity> it = res.iterator();
			while (it.hasNext()) {
				LittleProjectEntity s = it.next();

				if (s.getLists() == null || s.getLists().isEmpty()
						|| s.getLists().size() == 0) {
					it.remove();
				}
			}
			if (res != null && res.size() > 0) {
				for (LittleProjectEntity s : res) {
					if (lists != null && lists.size() > 0) {
						for (ProductTrendModel o : lists) {
							if (o.getCplb().equals(s.getName()) && n == 1) {
								times.add(o.getVmonth());
							}
						}
					}
					n = 2;
				}
			}
			dto.setTimes(times.toArray());
			dto.setEntities(res);
			DynamicDataSource.removeDataSourceKey();
			if (lists == null) {
				return this.resultSuccessData(request, response, "", null);
			} else {
				return this.resultSuccessData(request, response, "", dto);
			}
		} catch (Exception e) {
			logger.warn("总况——金融资产类-产品趋势", e);
			return this.resultFaliureData(request, response, "", null);
		}

	}

	/**
	 * 总况——金融资产类-平均年化利率
	 * 
	 * @author gaozhao
	 * @time 2018年4月17日
	 */
	
	@RequestMapping(value = { "${portalPath}/littleproject/nianhualilv" })
	@ResponseBody
	public String nianhualilv(HttpServletRequest request,
			HttpServletResponse response) {

		try {
			DynamicDataSource.setInsightDataSource();
			DecimalFormat dt = new DecimalFormat("0.00%");
			List<InterestRateModel> lists = new ArrayList<InterestRateModel>(300);
			lists = interestRateModelDao.getInterestRateModelDao();
			List<KeHuFenLei> res = new ArrayList<KeHuFenLei>(500);
			if (lists != null && lists.size() > 0) {
				for (InterestRateModel o : lists) {
					KeHuFenLei re = new KeHuFenLei();

					re.setGrs(o.getCplb());
					re.setJgs(dt.format(o.getCpsl()));

					res.add(re);
				}
			}
			DynamicDataSource.removeDataSourceKey();
			if (lists == null) {
				return this.resultSuccessData(request, response, "", null);
			} else {
				return this.resultSuccessData(request, response, "", res);
			}
		} catch (Exception e) {
			logger.warn("总况——金融资产类-平均年化利率", e);
			return this.resultFaliureData(request, response, "", null);
		}
	}

	/**
	 * 总况——金融资产类-产品统计
	 * 
	 * @author gaozhao
	 * @time 2018年4月17日
	 */
	

	@RequestMapping(value = { "${portalPath}/littleproject/chanpintongji" })
	@ResponseBody
	public String chanpintongji(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			DynamicDataSource.setInsightDataSource();
			DecimalFormat dt = new DecimalFormat("0.00%");
			List<ProductCountModel> lists = new ArrayList<ProductCountModel>(500);
			lists = productCountModelDao.getProductCountModelDao();
			List<LittleProjectEntity> res = new ArrayList<LittleProjectEntity>(300);
			List<String> jihe = new ArrayList<String>(300);
			if (lists != null && lists.size() > 0) {
				for (ProductCountModel o : lists) {

					jihe = new ArrayList<String>();
					LittleProjectEntity re = new LittleProjectEntity();
					jihe.add(dt.format(o.getPjll()));
					jihe.add(dt.format(o.getHbz()));
					re.setName(o.getCplb());
					re.setLists(jihe);
					res.add(re);

				}
			}
			DynamicDataSource.removeDataSourceKey();
			if (lists == null || lists.size() == 0) {
				return this.resultSuccessData(request, response, "", null);
			} else {
				return this.resultSuccessData(request, response, "", res);
			}
		} catch (Exception e) {
			logger.warn("总况——金融资产类-产品统计", e);
			return this.resultFaliureData(request, response, "", null);
		}
	}

	/**
	 * 总况——商品类-资金-沉淀资金
	 * 
	 * @author gaozhao
	 * @time 2018年4月18日
	 */
	

	@RequestMapping(value = { "${portalPath}/littleproject/chendianzijin" })
	@ResponseBody
	public String chendianzijin(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			DynamicDataSource.setInsightDataSource();
			String type = request.getParameter("type");

			List<SedimentaryCapitalModel> lists = new ArrayList<SedimentaryCapitalModel>(500);
			if ("day".equals(type)) {
				lists = sedimentaryCapitalModelDao
						.getSedimentaryCapitalModelDaoDay();
			} else if ("week".equals(type)) {
				lists = sedimentaryCapitalModelDao
						.getSedimentaryCapitalModelDaoWeek();
			} else if ("month".equals(type)) {
				lists = sedimentaryCapitalModelDao
						.getSedimentaryCapitalModelDaoMonth();
			}
			ZiJin ziJin = new ZiJin();
			List<String> jihe1 = new ArrayList<String>();
			List<String> jihe2 = new ArrayList<String>();
			if (lists != null && lists.size() > 0) {
				for (SedimentaryCapitalModel o : lists) {
					if ("week".equals(type)) {
						jihe1.add(o.getWeek_date());
						jihe2.add(o.getFvalue() + "");
					} else {
						jihe1.add(o.getDate());
						jihe2.add(o.getFvalue() + "");
					}

				}
			}
			ziJin.setA(jihe1);
			ziJin.setB(jihe2);
			DynamicDataSource.removeDataSourceKey();
			if (lists == null || lists.isEmpty()) {
				return this.resultSuccessData(request, response, "", null);
			} else {
				return this.resultSuccessData(request, response, "", ziJin);
			}
		} catch (Exception e) {
			logger.warn("总况——商品类-资金-沉淀资金", e);
			return this.resultFaliureData(request, response, "", null);
		}
	}

	/**
	 * 总况——商品类-资金-出入金
	 * 
	 * @author gaozhao
	 * @time 2018年4月18日
	 */
	

	@RequestMapping(value = { "${portalPath}/littleproject/churujin" })
	@ResponseBody
	public String churujin(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			DynamicDataSource.setInsightDataSource();
			String type = request.getParameter("type");

			List<EntryAndExitCapitalModel> lists = new ArrayList<EntryAndExitCapitalModel>(500);
			if ("day".equals(type)) {
				lists = entryAndExitCapitalModelDao
						.getEntryAndExitCapitalModelDaoDay();
			} else if ("week".equals(type)) {
				lists = entryAndExitCapitalModelDao
						.getEntryAndExitCapitalModelDaoWeek();
			} else if ("month".equals(type)) {
				lists = entryAndExitCapitalModelDao
						.getEntryAndExitCapitalModelDaoMonth();
			}
			List<EntryAndExitCapitalModel> alljinlist = entryAndExitCapitalModelDao
					.getEntryAndExitCapitalModelDaoAll();
			LittleProjectDto dto = new LittleProjectDto();
			List<LittleProjectEntity> res = new ArrayList<LittleProjectEntity>(300);
			List<String> times = new ArrayList<String>(200);
			int n = 1;
			if (alljinlist != null) {
				for (EntryAndExitCapitalModel s : alljinlist) {
					LittleProjectEntity re = new LittleProjectEntity();
					re.setName(s.getXm());
					res.add(re);
				}
			}
			if (res != null) {
				for (LittleProjectEntity s : res) {
					List<String> jihe = new ArrayList<String>();
					if (lists != null && lists.size() > 0) {
						for (EntryAndExitCapitalModel o : lists) {

							if (o.getXm().equals(s.getName())) {

								jihe.add(o.getFvalue() + "");

							}
						}
					}
					s.setLists(jihe);
				}
			}
			// 删除空list
			Iterator<LittleProjectEntity> it = res.iterator();
			while (it.hasNext()) {
				LittleProjectEntity s = it.next();

				if (s.getLists() == null || s.getLists().isEmpty()
						|| s.getLists().size() == 0) {
					it.remove();
				}

			}
			if (res != null && res.size() > 0) {
				for (LittleProjectEntity s : res) {

					if (lists != null && lists.size() > 0) {
						for (EntryAndExitCapitalModel o : lists) {

							if (o.getXm().equals(s.getName()) && n == 1) {
								times.add(o.getDate());
							}
						}
					}
					n = 2;
				}
			}
			dto.setTimes(times.toArray());
			dto.setEntities(res);
			DynamicDataSource.removeDataSourceKey();
			if (lists == null) {
				return this.resultSuccessData(request, response, "", null);
			} else {
				return this.resultSuccessData(request, response, "", dto);
			}
		} catch (Exception e) {
			logger.warn("总况——商品类-资金-出入金", e);
			return this.resultFaliureData(request, response, "", null);
		}
	}

	/**
	 * 总况——商品类-行情-各指数行情
	 * 
	 * @author gaozhao
	 * @time 2018年4月19日
	 */
	
	@RequestMapping(value = { "${portalPath}/littleproject/zhishuhangqing" })
	@ResponseBody
	public String zhishuhangqing(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			DynamicDataSource.setInsightDataSource();

			List<QuotationModel> lists = new ArrayList<QuotationModel>(500);
			lists = quotationModelDao.getQuotationModelDao();
			List<LittleProjectEntity> res = new ArrayList<LittleProjectEntity>(300);

			if (lists != null && lists.size() > 0) {
				for (QuotationModel o : lists) {
					LittleProjectEntity re = new LittleProjectEntity();
					List<String> aggregate = new ArrayList<String>();
					aggregate.add(o.getCpmc());
					aggregate.add(o.getZxjg() + "");
					aggregate.add(o.getBh() + "%");
					re.setLists(aggregate);
					res.add(re);
				}

			}
			DynamicDataSource.removeDataSourceKey();
			if (lists == null) {
				return this.resultSuccessData(request, response, "", null);
			} else {
				return this.resultSuccessData(request, response, "", res);
			}
		} catch (Exception e) {
			logger.warn("总况——商品类-行情-各指数行情", e);
			return this.resultFaliureData(request, response, "", null);
		}
	}

	/**
	 * 总况——商品类-产品-产品交易额占比
	 * 
	 * @author gaozhao
	 * @time 2018年4月26日
	 */
	

	@RequestMapping(value = { "${portalPath}/littleproject/productRtio" })
	@ResponseBody
	public String productRtio(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			DynamicDataSource.setInsightDataSource();
			String productClass = request.getParameter("productClass");
			List<TradeRtioModel> lists = new ArrayList<TradeRtioModel>(500);
			List<Map<String, Object>> dtoList = new ArrayList<Map<String, Object>>(300);
			Map<String, Object> map = null;
			if (productClass != null && productClass.length() > 0) {
				lists = tradeRtioModelDao.getTradeRtioModelDao2(productClass);
			} else {
				lists = tradeRtioModelDao.getTradeRtioModelDao();
			}
			for (TradeRtioModel tradeRtioModel : lists) {
				map = new LinkedHashMap<String, Object>();
				if (StringUtils.isEmpty(productClass)) {
					map.put("cpdlbm",
							formatterString(tradeRtioModel.getCpdlbm()));
					map.put("cpdlinfo",
							formatterString(tradeRtioModel.getCpdlinfo()));
					map.put("cpje", formatterString(tradeRtioModel.getCpje()));
				} else {
					map.put("cpdlbm", formatterString(tradeRtioModel.getCpdm()));
					map.put("cpdlinfo",
							formatterString(tradeRtioModel.getCpmc()));
					map.put("cpje", formatterString(tradeRtioModel.getCpje()));
				}
				dtoList.add(map);
			}
			DynamicDataSource.removeDataSourceKey();
			return this.resultSuccessData(request, response, "", dtoList);
		} catch (Exception e) {
			logger.error("总况——商品类-产品-产品交易额占比", e);
			return this.resultFaliureData(request, response, "", null);
		}
	}

	private String formatterString(Object obj) {
		return obj == null ? "" : obj.toString();
	}
}
