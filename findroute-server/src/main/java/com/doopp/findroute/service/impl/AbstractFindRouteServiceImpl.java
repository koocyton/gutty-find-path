package com.doopp.findroute.service.impl;

import com.doopp.findroute.pojo.Tile;
import com.doopp.findroute.service.FindRouteService;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.function.BiFunction;

public abstract class AbstractFindRouteServiceImpl implements FindRouteService  {

    // 初始化 open tile
    private Table<Integer, Integer, Tile> newOpenTileTable(Tile startTile, Tile endTile, Table<Integer, Integer, Tile> mapTileTable) {
        Table<Integer, Integer, Tile> openTileTable = HashBasedTable.create();
        for (int x=startTile.getX()-1; x<=startTile.getX()+1; x++) {
            for (int y=startTile.getY()-1; y<=startTile.getY()+1; y++) {
                if ((x==startTile.getX() && y==startTile.getY()) || (x==endTile.getX() && y==endTile.getY())) {
                    continue;
                }
                Tile tile = mapTileTable.get(x, y);
                if (tile!=null) {
                    tile.refreshGHF(startTile, startTile, endTile);
                    openTileTable.put(x, y, tile);
                }
            }
        }
        return openTileTable;
    }

    // 初始化地块
    protected Table<Integer, Integer, Tile> newMapTileTable(Tile startTile, Tile endTile, Table<Integer, Integer, Tile> closeTileTable) {
        Table<Integer, Integer, Tile> mapTileTable = HashBasedTable.create();
        // x 轴
        for (int x=0; x<60; x++) {
            // y 轴
            for (int y=0; y<60; y++) {
                // 终点，起点地块不算
                if ((startTile.getX()==x && startTile.getY()==y) || (endTile.getX()==x && endTile.getY()==y)) {
                    continue;
                }
                mapTileTable.put(x, y, new Tile(x, y, closeTileTable.get(x, y)!=null));
            }
        }
        return mapTileTable;
    }

    // 获取相邻的地块 neighborTile
    protected Tile nextNeighborTile(Tile currentTile, Tile startTile, Tile endTile, Table<Integer, Integer, Tile> mapTileTable, Table<Integer, Integer, Tile> openTileTable) {
        Tile nextTile = null;
        // 相邻地块，即 x,y的增量在 -1 ~ 1 之间 共 八个
        for (int incX=-1; incX<=1; incX++) {
            // y 增量
            for (int incY=-1; incY<=1; incY++) {
                // 不算自己
                if (incX==0 && incY==0) {
                    continue;
                }
                // 下一个的坐标
                int nextX = currentTile.getX() + incX;
                int nextY = currentTile.getY() + incY;
                // 如果是终点
                if (endTile.getX()==nextX && endTile.getY()==nextY) {
                    // System.out.println(endTile);
                    return endTile;
                }
                Tile neighborTile = mapTileTable.get(nextX, nextY);
                // 没有在 openTileTable 里不行
                if (neighborTile==null) {
                    continue;
                }
                // 纳入到 openTileTable，在 A* 回找时，用这个做 map
                openTileTable.put(neighborTile.getX(), neighborTile.getY(), neighborTile);
                // 更新 f
                // neighborTile.refreshGHF(currentTile, endTile);
                neighborTile.refreshGHF(currentTile, startTile, endTile);
                // 如果 f 值最小，就是下一步的路径
                if (nextTile==null) {
                    nextTile = neighborTile;
                }
                else if (neighborTile.getF()<nextTile.getF()) {
                    nextTile = neighborTile;
                }
                // else if (neighborTile.getF()==nextTile.getF()) {
                // System.out.println(neighborTile);
                // System.out.println(nextTile);
                // }
            }
        }
        // 已经走过的块，从可以走的地图块上移除
        if (nextTile!=null) {
            mapTileTable.remove(nextTile.getX(), nextTile.getY());
        }
        return nextTile;
    }

    private final static BiFunction<Tile, Tile, Double> manhattan = (a, b) -> {
        return (double) Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    };

    private final static BiFunction<Tile, Tile, Double> euclidean = (a, b) -> {
        return Math.sqrt(
                Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2)
        );
    };

    private final static BiFunction<Tile, Tile, Double> octile = (a, b) -> {
        double F = Math.sqrt(2) - 1;
        double dx = Math.abs(a.getX() - b.getX());
        double dy = Math.abs(a.getY() - b.getY());
        return (dx < dy) ? F * dx + dy : F * dy + dx;
    };

    private final static BiFunction<Tile, Tile, Double> chebyshev = (a, b) -> {
        return (double) Math.max(
                Math.abs(a.getX() - b.getX()),
                Math.abs(a.getY() - b.getY())
        );
    };



    // 取 A 点移动方向的下一个点两侧是否有阻挡, 有就标记强制邻居
    private void getNeighbor(Tile tile, String direction, Table<Integer, Integer, Tile> mapTileTable, Table<Integer, Integer, Tile> openTileTable) {
        int mx = 0;
        int my = 0;
        Integer[][] checkCoordinate;
        Tile nextTile;
        Tile neighborTile1;
        Tile neighborTile2;

        // 8 个方向检测的块的邻居
        switch (direction) {
            case "left" :
                nextTile      = mapTileTable.get(tile.getX()-1, tile.getY());
                neighborTile1 = mapTileTable.get(tile.getX()-1, tile.getY() + 1);
                neighborTile2 = mapTileTable.get(tile.getX()-1, tile.getY() - 1);
                break;
            case "right" :
                nextTile      = mapTileTable.get(tile.getX()+1, tile.getY());
                neighborTile1 = mapTileTable.get(tile.getX()+1, tile.getY() + 1);
                neighborTile2 = mapTileTable.get(tile.getX()+1, tile.getY() - 1);
                break;
            case "up" :
                nextTile      = mapTileTable.get(tile.getX(), tile.getY() + 1);
                neighborTile1 = mapTileTable.get(tile.getX()+1, tile.getY() + 1);
                neighborTile2 = mapTileTable.get(tile.getX()-1, tile.getY() + 1);
                break;
            case "down" :
                nextTile      = mapTileTable.get(tile.getX(), tile.getY() - 1);
                neighborTile1 = mapTileTable.get(tile.getX()+1, tile.getY() - 1);
                neighborTile2 = mapTileTable.get(tile.getX()-1, tile.getY() - 1);
                break;
            case "left-up" :
                nextTile      = mapTileTable.get(tile.getX()-1, tile.getY() + 1);
                neighborTile1 = mapTileTable.get(tile.getX()-1, tile.getY());
                neighborTile2 = mapTileTable.get(tile.getX(), tile.getY()+1);
                break;
            case "right-down":
                nextTile      = mapTileTable.get(tile.getX()+1, tile.getY() - 1);
                neighborTile1 = mapTileTable.get(tile.getX()-1, tile.getY());
                neighborTile2 = mapTileTable.get(tile.getX(), tile.getY()+1);
                break;
            case "left-down" :
                nextTile      = mapTileTable.get(tile.getX()-1, tile.getY() - 1);
                neighborTile1 = mapTileTable.get(tile.getX()-1, tile.getY());
                neighborTile2 = mapTileTable.get(tile.getX(), tile.getY()-1);
                break;
            case "right-up"  :
                nextTile      = mapTileTable.get(tile.getX()+1, tile.getY() + 1);
                neighborTile1 = mapTileTable.get(tile.getX()-1, tile.getY());
                neighborTile2 = mapTileTable.get(tile.getX(), tile.getY()+1);
                break;
            default: return;
        }
    }
}
