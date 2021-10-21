package com.doopp.findroute.service.impl;

import com.doopp.findroute.pojo.FTile;
import com.doopp.findroute.pojo.Tile;
import com.doopp.findroute.service.ShortRouteService;
import com.doopp.gutty.annotation.Service;
import com.google.common.collect.Table;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class ShortRouteServiceImpl implements ShortRouteService {

    @Override
    public List<FTile> shortRoute(List<FTile> walkingTiles, Table<Integer, Integer, FTile> closeTileTable) {
        List<FTile> turningTiles = turningTiles(walkingTiles);
        // log.info("turningTiles : {}", turningTiles);
        // log.info("closeTiles : {}", closeTileTable);
        int cnt = turningTiles.size();
        if (cnt<=2) {
            return walkingTiles;
        }
        // List<FTile> sortTiles = new ArrayList<>();
        // sortTiles.add(turningTiles.get(0));
        turningTiles.get(0).setStraightPoint(true);
        for (int ii=0; ii<cnt-2; ii++) {
            for (int mm=cnt-1; mm>=ii; mm--) {
                FTile sTile = turningTiles.get(ii);
                FTile eTile = turningTiles.get(mm);
                // log.info("ii {}, mm {}, aa {}, bb {}", ii, mm, sTile.x, eTile.x);
                if (!existCloseTile(sTile, eTile, closeTileTable)) {
                    // log.info("ii {}  mm {} eTile {}", ii,mm,eTile);
                    eTile.setStraightPoint(true);
                    // sortTiles.add(eTile);
                    ii = mm;
                    break;
                }
            }
        }
        // walkingTiles.get(cnt-1).setStraightPoint(true);
        // sortTiles.add(walkingTiles.get(cnt-1));
        // log.info("{}", turningTiles);
        return walkingTiles;
    }

    // 两个地块直线距离上是否存在不能走的地块
    private boolean existCloseTile(FTile startTile, FTile endTile, Table<Integer, Integer, FTile> closeTileTable) {
        // log.info("{}-{} {}-{}", startTile.x, startTile.y, endTile.x, endTile.y);
        int x = endTile.x - startTile.x;
        int y = endTile.y - startTile.y;
        double z = Math.sqrt(x*x + y*y);
        for (double zz=0; zz<=z; zz=zz+0.1) {
            double xx = Math.ceil(zz/z * x + startTile.x);
            double yy = Math.ceil(zz/z * y + startTile.y);
            // log.info("\n  >>> checkTile : x {} y {} z {} xx {} yy {} zz {}", x, y, z, new Double(xx).intValue(), new Double(yy).intValue(), zz);
            FTile closeTile = closeTileTable.get(new Double(xx).intValue(), new Double(yy).intValue());
            if (closeTile!=null) {
                // log.info("    \n   x:{} xx:{}  y:{} yy:{}", x, xx, y, yy);
                return true;
            }
        }
        return false;
    }

    // 获取有拐弯的节点
    // 即来的方向和走的方向不一致
    private List<FTile> turningTiles(List<FTile> walkingTiles) {
        List<FTile> tTiles = new ArrayList<>();
        for(int ii=0; ii<walkingTiles.size(); ii++) {
            // 第一个 || 最后一个
            if (ii==0 || ii==walkingTiles.size()-1) {
                tTiles.add(walkingTiles.get(ii));
            }
            // 拐弯处
            else {
                FTile aTile = walkingTiles.get(ii - 1);
                FTile bTile = walkingTiles.get(ii);
                FTile cTile = walkingTiles.get(ii + 1);
                int ax = aTile.getX();
                int ay = aTile.getY();
                int bx = bTile.getX();
                int by = bTile.getY();
                if (cTile.getX() != bx + bx - ax || cTile.getY() != by + by - ay) {
                    tTiles.add(bTile);
                }
            }
        }
        return tTiles;
    }
}
