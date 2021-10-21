package com.doopp.findroute.service;

import com.doopp.findroute.pojo.FTile;
import com.doopp.findroute.pojo.Tile;
import com.google.common.collect.Table;

import java.util.List;

public interface FindRouteService {

    List<Tile> walkingRoute(Tile startTile, Tile endTile, Table<Integer, Integer, Tile> closeTileTable);

    List<FTile> walkingRoute(FTile startTile, FTile endTile, Table<Integer, Integer, FTile> closeTileTable);
}
