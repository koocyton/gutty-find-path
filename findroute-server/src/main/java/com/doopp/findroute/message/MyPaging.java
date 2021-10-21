//package com.doopp.findroute.message;
//
//import com.github.pagehelper.PageInfo;
//import lombok.Data;
//
//import java.util.List;
//
//@Data
//public class MyPaging<T> {
//
//    private int code = 0;
//
//    private String msg = "";
//
//    private Long count = 0L;
//
//    private List<T> data;
//
//    private MyPaging() {
//    }
//
//    public static <D> MyPaging<D> ok(List<D> list) {
//        PageInfo<D> pageInfo = new PageInfo<>(list);
//        MyPaging<D> pr = new MyPaging<D>();
//        pr.data = list;
//        pr.count = pageInfo.getTotal();
//        return pr;
//    }
//}
