package com.doopp.findroute.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class Tile {

    private Integer x;

    private Integer y;

    private Boolean straightPoint;

    @JsonIgnore
    private Boolean closed;

    // 从起点 A 移动到指定方格的移动代价，沿着到达该方格而生成的路径
    // 斜线相邻为 14, 横竖相邻为 10 ( 14.14²≈10²+10²=200)
    @JsonIgnore
    private Integer g;

    // 从指定的方格移动到终点 B 的估算成本
    // Manhattan 方法，计算从当前方格横向或纵向移动到达目标所经过的方格数
    @JsonIgnore
    private Integer h;

    // g + h
    // 移动到最小值
    @JsonIgnore
    private double f;

    public Tile(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public Tile(Integer x, Integer y, Boolean closed) {
        this.x = x;
        this.y = y;
        this.closed = closed;
    }

    public void refreshGHF(Tile fromTile, Tile startTile, Tile endTile) {

        g = (fromTile.x.equals(x) || fromTile.y.equals(y)) ? 10 : 14;

        int sx = Math.abs(x - startTile.x);
        int sy = Math.abs(y - startTile.y);

        int ex = Math.abs(endTile.x - x);
        int ey = Math.abs(endTile.y - y);

        // Manhattan
        // f = Math.sqrt(sx * sx + sy * sy) + Math.sqrt(ex * ex + ey * ey);
        // f = Math.sqrt(ex * ex + ey * ey);
        f = ex + ey;

        // Euclidean
        // f = Math.sqrt(ex * ex + ey * ey);

        // Octile
        // double _f = Math.sqrt(2) - 1;
        // f = (ex < ey) ? _f * ex + ey : _f * ey + ex;

        // Chebyshev
        // f = Math.min(dx, dy);
    }
}
