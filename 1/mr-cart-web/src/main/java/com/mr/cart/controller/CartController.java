package com.mr.cart.controller;

import com.mr.model.Item;
import com.mr.service.ItemService;
import com.mr.util.DataResult;
import com.mr.util.JackSonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import utils.CookieUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/5/22.
 */
@Controller
public class CartController {
    @Autowired
    private ItemService itemService;
    @Value("${CARTCOOKIENAME}")
    private String CARTCOOKIENAME;
    @Value("${COOKIEMAXAGE}")
    private Integer COOKIEMAXAGE;
    //cart/add/${item.id}
    @RequestMapping("cart/add/{itemId}/{num}")
    public String add(@PathVariable long itemId,@PathVariable Integer num,
                      HttpServletRequest request, HttpServletResponse response){
        // 1.从cookie中获取购物车集合数据
        boolean falg = false;
        List<Item> list = new ArrayList();
        String json = CookieUtils.getCookieValue(request, CARTCOOKIENAME, true);
        if(StringUtils.isNotBlank(json)){
             list = JackSonUtil.jsonToList(json, Item.class);
            // 2.判断商品在购物车中是否存在
            for (Item item : list) {
                if(item.getId() == itemId){ //说明存在
                    // 3.如果存在.则添加数量 +n
                    item.setNum(item.getNum()+num);
                    falg = true;
                }
            }
        }

        if(!falg){ // 不存在
            // 4.如果不存在.需要通过id查询数据
            Item item = itemService.getItemById(itemId);
            // 修改数量
            item.setNum(num);
            // 5.查出以后将数据存放在list中
            list.add(item);
        }
        // 6.将list集合存放到cookie中
    CookieUtils.setCookie(request,response,CARTCOOKIENAME,JackSonUtil.objectToJson(list),COOKIEMAXAGE,true);

        // 7.返回成功页面
        return "cartSuccess";
    }

    @RequestMapping("/cart/cart")
     public String cartListPage(ModelMap map,HttpServletRequest request){
        String json = CookieUtils.getCookieValue(request, CARTCOOKIENAME, true);
        map.put("cartList",StringUtils.isBlank(json)?null:JackSonUtil.jsonToList(json,Item.class));
        return "cart";
    }
    ///cart/update/num/"+_thisInput.attr("itemId")+"/"+_thisInput.val() + ".html
    @ResponseBody
    @RequestMapping("/cart/update/num/{itemId}/{num}")
    public DataResult updateNum(@PathVariable long itemId , @PathVariable Integer num,
                                HttpServletRequest request, HttpServletResponse response){
        String json = CookieUtils.getCookieValue(request, CARTCOOKIENAME, true);
          List<Item>  list = JackSonUtil.jsonToList(json, Item.class);
            // 2.判断商品在购物车中是否存在
            for (Item item : list) {
                if(item.getId() == itemId){ //说明存在
                    // 3.如果存在.则添加数量 +n
                    item.setNum(num);
                    break;
                }
            }

        // 6.将list集合存放到cookie中
        CookieUtils.setCookie(request,response,CARTCOOKIENAME,JackSonUtil.objectToJson(list),COOKIEMAXAGE,true);

        return DataResult.ok();
    }

    @RequestMapping("/cart/delete/{itemId}")
    public String delete(@PathVariable long itemId,HttpServletRequest request, HttpServletResponse response){
        String json = CookieUtils.getCookieValue(request, CARTCOOKIENAME, true);
        List<Item>  list = JackSonUtil.jsonToList(json, Item.class);
        // 2.判断商品在购物车中是否存在
        for (Item item : list) {
            if(item.getId() == itemId){ //说明存在
                // 3.如果存在.则添加数量 +n
                list.remove(item);
                break;
            }
        }
        // 6.将list集合存放到cookie中
        CookieUtils.setCookie(request,response,CARTCOOKIENAME,JackSonUtil.objectToJson(list),COOKIEMAXAGE,true);

        return "redirect:/cart/cart.html";
    }
}
