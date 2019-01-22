package com.kkl.graffiti.edit.filter;

import com.kkl.graffiti.R;

import java.util.LinkedHashMap;

/**
 * @author cst1718 on 2019/1/18 18:51
 * @explain
 */
public class FilterHelper {
    public static LinkedHashMap<String, Integer> getRawList() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        map.put("留声", R.raw.aimei);
        map.put("淡黄", R.raw.danhuang);
        map.put("淡蓝", R.raw.danlan);
        map.put("复古", R.raw.fugu);
        map.put("高冷", R.raw.gaoleng);
        map.put("怀旧", R.raw.huaijiu);
        map.put("胶片", R.raw.jiaopian);
        map.put("可爱", R.raw.keai);
        map.put("落寞", R.raw.lomo);
        map.put("午后", R.raw.morenjiaqiang);
        map.put("暖心", R.raw.nuanxin);
        map.put("清新", R.raw.qingxin);
        map.put("日系", R.raw.rixi);
        map.put("温暖", R.raw.wennuan);
        map.put("浅滩", R.raw.tone_cuver_sample);
        return map;
    }
}
