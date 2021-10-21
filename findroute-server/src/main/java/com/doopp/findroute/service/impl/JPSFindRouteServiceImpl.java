package com.doopp.findroute.service.impl;

import com.doopp.findroute.pojo.FTile;
import com.doopp.findroute.pojo.Tile;
import com.doopp.findroute.service.FindRouteService;
import com.doopp.findroute.service.ShortRouteService;
import com.doopp.gutty.annotation.Service;
import com.google.common.collect.Table;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.log4j.Log4j2;
import org.ksdev.jps.Graph;
import org.ksdev.jps.JPS;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Log4j2
@Service("jpsFindRouteService")
public class JPSFindRouteServiceImpl implements FindRouteService {

    @Inject
    private NioEventLoopGroup workerEventLoopGroup;

    @Inject
    private ShortRouteService shortRouteService;

    @Override
    public List<FTile> walkingRoute(FTile startTile, FTile endTile, Table<Integer, Integer, FTile> closeTileTable) {
        final CompletableFuture<List<FTile>> future = new CompletableFuture<>();
        workerEventLoopGroup.execute(()->{
            future.complete(_walkingRoute(startTile,endTile,closeTileTable));
        });
        try {
            List<FTile> walkingTiles = shortRouteService.shortRoute(future.get(), closeTileTable);
            return walkingTiles.subList(1, walkingTiles.size()-1);
        }
        catch (InterruptedException | ExecutionException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    private List<FTile> _walkingRoute(FTile startTile, FTile endTile, Table<Integer, Integer, FTile> closeTileTable) {
        // List<List<FTile>> tileList = mapToTileList(map2);
        final List<List<FTile>> tileList = this.newTileList(closeTileTable);
        final JPS<FTile> jps = JPS.JPSFactory.getJPS(new Graph<>(tileList), Graph.Diagonal.ONE_OBSTACLE);
        FTile start = tileList.get(startTile.getY()).get(startTile.getX());
        FTile end = tileList.get(endTile.getY()).get(endTile.getX());

        try {
            Future<Queue<FTile>> futurePath = jps.findPath(start, end);
            Queue<FTile> path = futurePath.get();
            // return Arrays.asList(path.toArray(new FTile[0])).subList(1, path.size()-1);
            return Arrays.asList(path.toArray(new FTile[0]));
        }
        catch(Exception e) {
            return new ArrayList<>();
        }
    }

    // 初始化地块
    protected List<List<FTile>> newTileList(Table<Integer, Integer, FTile> closeTileTable) {
        final List<List<FTile>> tileList = new ArrayList<>();
        // x 轴
        for (int y=0; y<60; y++) {
            List<FTile> tileColList = new ArrayList<>();
            for (int x=0; x<60; x++) {
                FTile fTile = new FTile(x, y);
                fTile.setWalkable(closeTileTable.get(x, y)==null);
                tileColList.add(fTile);
            }
            // log.info("x {}, y {}", tileColList.get(0).x, tileColList.get(0).y);
            tileList.add(tileColList);
        }
        return tileList;
    }

    @Override
    public List<Tile> walkingRoute(Tile startTile, Tile endTile, Table<Integer, Integer, Tile> closeTileTable) {
        return null;
    }
}
