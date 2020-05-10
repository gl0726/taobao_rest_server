package com.shenque.control.app;


import com.shenque.model.ApiResponse;
import com.shenque.model.ReResult;
import com.shenque.utils.ESUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * xiao.py
 * APP 批量商品删除接口
 */
@RestController
@RequestMapping("/batchdel")
@Api(tags = "APP 请求删除es各平台的商品接口", description = "APP 请求删除es各平台的商品接口")
public class BatchDeleteGoods {

    private static final Logger logger = Logger.getLogger(BatchDeleteGoods.class);

    private static final String taobao_index_alias_Name = "recommend";
    private static final String taobao_type = "recommend_goods";

    private static final String dou_index_alias_Name = "dou";
    private static final String dou_type = "dou_goods";

    private static final String pindd_index_alias_Name = "pindd";
    private static final String pindd_type = "pindd_goods";


    private static final String jd_index_alias_Name = "jd_index";
    private static final String jd_type = "jd_goods";

    @Autowired
    private ESUtil eSUtil;


    /**
     * APP 删除淘宝天猫商品
     * @param id
     * @return
     */
    @ApiOperation(value="批量删除淘宝天猫商品", notes="批量删除淘宝天猫商品")
    @DeleteMapping("/tb/id/{id}")
    public String deleteTb(@PathVariable String id) {
        boolean flag = false;
        try {
            String[] split = id.split(",");
            for (int i = 0 ;i < split.length ; i++){
                flag = eSUtil.deleteDucId(taobao_index_alias_Name, taobao_type, split[i].trim());
            }

            return new ReResult(flag).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return new ReResult(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),"批量删除淘宝天猫商品异常" + e ,flag).toString();
        }
    }

    /**
     * APP 批量删除拼多多商品
     * @param id
     * @return
     */
    @ApiOperation(value="批量删除拼多多商品", notes="批量删除拼多多商品")
    @DeleteMapping("/pindd/id/{id}")
    public String deletePindd(@PathVariable  String id) {
        boolean flag = false;
        try {
            String[] split = id.split(",");
            for (int i = 0 ;i < split.length ; i++) {
                flag = eSUtil.deleteDucId(pindd_index_alias_Name, pindd_type, split[i].trim());
            }
            return new ReResult(flag).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return new ReResult(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),"批量删除拼多多商品异常" + e ,flag).toString();
        }
    }


    /**
     * APP 批量删除抖货商品
     * @param id
     * @return
     */
    @ApiOperation(value="批量删除抖货商品", notes="批量删除抖货商品")
    @DeleteMapping("/dou/id/{id}")
    public String deleteDou(@PathVariable  String id) {
        boolean flag = false;
        try {
            String[] split = id.split(",");
            for (int i = 0 ;i < split.length ; i++) {
                flag = eSUtil.deleteDucId(dou_index_alias_Name, dou_type, split[i].trim());
            }
            return new ReResult(flag).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return new ReResult(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),"批量删除抖货商品异常" + e ,flag).toString();
        }
    }

    /**
     * APP 批量删除京东商品
     * @param id
     * @return
     */
    @ApiOperation(value="批量删除京东商品", notes="批量删除京东商品")
    @DeleteMapping("/jd/id/{id}")
    public String deleteJd(@PathVariable  String id) {
        boolean flag = false;
        try {
            String[] split = id.split(",");
            for (int i = 0 ;i < split.length ; i++) {
                flag = eSUtil.deleteDucId(jd_index_alias_Name, jd_type, split[i].trim());
            }
            return new ReResult(flag).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return new ReResult(ApiResponse.Status.INTERNAL_SERVER_ERROR.getCode(),"批量删除京东商品异常" + e ,flag).toString();
        }
    }
}
