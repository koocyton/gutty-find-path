package com.doopp.findroute.service.impl;

import com.doopp.findroute.pojo.FTile;
import com.doopp.findroute.pojo.Tile;
import com.doopp.findroute.service.ShortRouteService;
import com.doopp.gutty.annotation.Service;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.log4j.Log4j2;
import com.doopp.findroute.service.FindRouteService;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Log4j2
@Service("aStarFindRouteService")
public class AStarFindRouteServiceImpl implements FindRouteService {

    @Inject
    private NioEventLoopGroup workerEventLoopGroup;

    @Inject
    private ShortRouteService shortRouteService;

    @Override
    public List<Tile> walkingRoute(Tile startTile, Tile endTile, Table<Integer, Integer, Tile> closeTileTable) {
        final CompletableFuture<List<Tile>> future = new CompletableFuture<>();
        workerEventLoopGroup.execute(()->{
            future.complete(_walkingRoute(startTile,endTile,closeTileTable));
        });
        // result
        try {
            return future.get();
        }
        catch (InterruptedException | ExecutionException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }


    private List<Tile> _walkingRoute(Tile startTile, Tile endTile, Table<Integer, Integer, Tile> closeTileTable) {
        Table<Integer, Integer, Tile> mapTileTable = newMapTileTable(startTile, endTile, closeTileTable);
        Table<Integer, Integer, Tile> openTileTable = HashBasedTable.create();
        // 从 start 开始起步
        List<Tile> firstRoute = _walkingRoute(startTile, endTile, mapTileTable, HashBasedTable.create());
        // return firstRoute;
        if (firstRoute.size()<2) {
            return firstRoute;
        }
        //mapTileTable.clear();
        openTileTable.clear();
        firstRoute.forEach((tile)->{
            openTileTable.put(tile.getX(), tile.getY(), tile);
        });
        //log.info(firstRoute.size());
        //log.info(openTileTable.size());
        // 反走
        // Table<Integer, Integer, Tile> openTileTable2 = HashBasedTable.create();
        List<Tile> secondRoute = _walkingRoute(endTile, startTile, openTileTable, HashBasedTable.create());
        //log.info(secondRoute.size());
        Collections.reverse(secondRoute);
        return secondRoute;
        // return firstRoute;
        // openTileTable.clear();
        // return _walkingRoute(startTile, endTile, openTileTable2, HashBasedTable.create());
    }

    public List<Tile> _walkingRoute(Tile startTile, Tile endTile, Table<Integer, Integer, Tile> mapTileTable, Table<Integer, Integer, Tile> openTileTable) {
        // 从 start 开始起步
        Tile currentTile = startTile;
        Tile nextTile;
        List<Tile> routeTileList = new ArrayList<>();
        int ii = 0;
        int nn = mapTileTable.size();
        // 循环执行
        while(ii<nn) {
            nextTile = nextStep(currentTile, startTile, endTile, mapTileTable, openTileTable);
            // 比如走入死胡同
            if (nextTile==null) {
                // 当前 tile 是起始 tile
                if (currentTile==startTile) {
                    break;
                }
                // 沿路回退一个 tile 重新走
                int routeTileListSize = routeTileList.size();
                routeTileList.remove(routeTileListSize-1);
                currentTile = routeTileList.get(routeTileListSize - 2);
                continue;
            }
            // 如果到达 endTile
            if (nextTile.getX().equals(endTile.getX()) && nextTile.getY().equals(endTile.getY())) {
                break;
            }
            // System.out.println(mapTileTable.size());
            currentTile = nextTile;
            mapTileTable.remove(nextTile.getX(), nextTile.getY());
            routeTileList.add(nextTile);
            ii++;
        }
        return routeTileList;
    }

    // 初始化地块
    private Table<Integer, Integer, Tile> newMapTileTable(Tile startTile, Tile endTile, Table<Integer, Integer, Tile> closeTileTable) {
        Table<Integer, Integer, Tile> mapTileTable = HashBasedTable.create();
        // x 轴
        for (int x=0; x<60; x++) {
            // y 轴
            for (int y=0; y<60; y++) {
                // 终点，起点地块不算
                if (closeTileTable.get(x, y)!=null || (startTile.getX()==x && startTile.getY()==y) || (endTile.getX()==x && endTile.getY()==y)) {
                    continue;
                }
                // 可以走的地块
                mapTileTable.put(x, y, new Tile(x, y));
            }
        }
        return mapTileTable;
    }

    // 获取相邻的地块 neighborTile
    private Tile nextStep(Tile currentTile, Tile startTile, Tile endTile, Table<Integer, Integer, Tile> mapTileTable, Table<Integer, Integer, Tile> openTileTable) {
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

    @Override
    public List<FTile> walkingRoute(FTile startTile, FTile endTile, Table<Integer, Integer, FTile> closeTileTable) {
        return null;
    }
}
