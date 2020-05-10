package com.shenque.model;

import java.util.List;
/**
 * xiao.py
 */
public class ESCommdityRestInfo {
    /**
     * 店铺类型	0为淘宝店铺，1为天猫店铺
     */
    private int user_type;

    /**
     * 商品推荐语或描述
     */
    private String  item_description;

    private String  category;

    /**
     * 优惠券信息
     */
    private String  coupon_info;
    /**
     * 商品链接
     */
    private String  item_url;
    /**
     * 佣金比例   百分比，建议使用高佣接口重新转链
     */
    private Double  commission_rate;

    private Long  online_end_time;
    /**
     * 总优惠券数量
     */
    private Integer coupon_total_count;
    /**
     * 已领取优惠券数量
     */
    private Integer  yilingqu;
    /**
     * 在售价
     */
    private Double zk_final_price;
    /**
     * 券后价 使用优惠券后的价格
     */
    private Double quanhoujia;
    /**
     * 店铺名	如没有数据，则显示为nodata
     */
    private String shop_title;
    /**
     * 	销量
     */
    private Long volume;
    /**
     * 优惠券剩余数量
     */
    private Integer coupon_remain_count;
    /**
     * 优惠券开始时间
     */
    private String  coupon_start_time;
    /**
     * 优惠券结束时间
     */
    private String  coupon_end_time;

    /**
     * 店铺ID
     */
    private String  seller_id;

    /**
     * 商品名
     */
    private String  title;
    /**
     * 商品id
     */
    private String num_iid;
    /**
     * 优惠券ID
     */
    private String activeid;
    /**
     * 优惠券金额
     */
    private Double youhuiquan;

    private Long online_start_time;
    /**
     * 商品主图
     */
    private String pict_url;

    /**
     * 商家旺旺昵称	如没有数据，则显示为nodata
     */
    private String  nick;

    /**
     * 自己添加了 新增优惠券比率
     */
    private Double youhuiquanbilv;

    /**
     * 搜索填充词
     */
   // private List<GoodsSuggest> suggest;

    public int getUser_type() {
        return user_type;
    }

    public void setUser_type(int user_type) {
        this.user_type = user_type;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCoupon_info() {
        return coupon_info;
    }

    public void setCoupon_info(String coupon_info) {
        this.coupon_info = coupon_info;
    }

    public String getItem_url() {
        return item_url;
    }

    public void setItem_url(String item_url) {
        this.item_url = item_url;
    }

    public Double getCommission_rate() {
        return commission_rate;
    }

    public void setCommission_rate(Double commission_rate) {
        this.commission_rate = commission_rate;
    }

    public Long getOnline_end_time() {
        return online_end_time;
    }

    public void setOnline_end_time(Long online_end_time) {
        this.online_end_time = online_end_time;
    }

    public Integer getCoupon_total_count() {
        return coupon_total_count;
    }

    public void setCoupon_total_count(Integer coupon_total_count) {
        this.coupon_total_count = coupon_total_count;
    }

    public Integer getYilingqu() {
        return yilingqu;
    }

    public void setYilingqu(Integer yilingqu) {
        this.yilingqu = yilingqu;
    }

    public Double getZk_final_price() {
        return zk_final_price;
    }

    public void setZk_final_price(Double zk_final_price) {
        this.zk_final_price = zk_final_price;
    }

    public Double getQuanhoujia() {
        return quanhoujia;
    }

    public void setQuanhoujia(Double quanhoujia) {
        this.quanhoujia = quanhoujia;
    }

    public String getShop_title() {
        return shop_title;
    }

    public void setShop_title(String shop_title) {
        this.shop_title = shop_title;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public Integer getCoupon_remain_count() {
        return coupon_remain_count;
    }

    public void setCoupon_remain_count(Integer coupon_remain_count) {
        this.coupon_remain_count = coupon_remain_count;
    }

    public String getCoupon_start_time() {
        return coupon_start_time;
    }

    public void setCoupon_start_time(String coupon_start_time) {
        this.coupon_start_time = coupon_start_time;
    }

    public String getCoupon_end_time() {
        return coupon_end_time;
    }

    public void setCoupon_end_time(String coupon_end_time) {
        this.coupon_end_time = coupon_end_time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNum_iid() {
        return num_iid;
    }

    public void setNum_iid(String num_iid) {
        this.num_iid = num_iid;
    }

    public String getActiveid() {
        return activeid;
    }

    public void setActiveid(String activeid) {
        this.activeid = activeid;
    }

    public Double getYouhuiquan() {
        return youhuiquan;
    }

    public void setYouhuiquan(Double youhuiquan) {
        this.youhuiquan = youhuiquan;
    }

    public Long getOnline_start_time() {
        return online_start_time;
    }

    public void setOnline_start_time(Long online_start_time) {
        this.online_start_time = online_start_time;
    }

    public String getPict_url() {
        return pict_url;
    }

    public void setPict_url(String pict_url) {
        this.pict_url = pict_url;
    }

    public String getItem_description() {
        return item_description;
    }

    public void setItem_description(String item_description) {
        this.item_description = item_description;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public Double getYouhuiquanbilv() {
        return youhuiquanbilv;
    }

    public void setYouhuiquanbilv(Double youhuiquanbilv) {
        this.youhuiquanbilv = youhuiquanbilv;
    }

    /*public List<GoodsSuggest> getSuggest() {
        return suggest;
    }

    public void setSuggest(List<GoodsSuggest> suggest) {
        this.suggest = suggest;
    }*/



}
