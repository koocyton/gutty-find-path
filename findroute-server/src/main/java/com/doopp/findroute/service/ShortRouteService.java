package com.doopp.findroute.service;

import com.doopp.findroute.pojo.FTile;
import com.doopp.findroute.pojo.Tile;
import com.google.common.collect.Table;

import java.util.List;

public interface ShortRouteService {

    public List<FTile> shortRoute(List<FTile> walkingRouteTiles, Table<Integer, Integer, FTile> closeTileTable);
}
