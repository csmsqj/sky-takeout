package com.sky.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class OrderStatisticsVO implements Serializable {
    //待接单数量2
    private Integer toBeConfirmed;

    //待派送数量3
    private Integer confirmed;

    //派送中数量4
    private Integer deliveryInProgress;
}
