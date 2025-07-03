package ltd.goods.cloud.newbee.controller.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @program: newbee-mall-cloud-dev-step07
 * @description:
 * @author: Miao Zheng
 * @date: 2025-06-30 17:08
 **/
@Data
public class NewBeeMallSearchGoodsVO implements Serializable {

    @ApiModelProperty("商品id")
    private Long goodsId;

    @ApiModelProperty("商品名称")
    private String goodsName;

    @ApiModelProperty("商品简介")
    private String goodsIntro;

    @ApiModelProperty("商品图片地址")
    private String goodsCoverImg;

    @ApiModelProperty("商品价格")
    private Integer sellingPrice;

}
