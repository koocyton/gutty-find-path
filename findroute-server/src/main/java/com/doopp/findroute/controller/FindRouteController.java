package com.doopp.findroute.controller;

import com.doopp.findroute.message.MyResponse;
import com.doopp.findroute.pojo.FTile;
import com.doopp.findroute.pojo.Tile;
import com.doopp.findroute.service.FindRouteService;
import com.doopp.gutty.annotation.Controller;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.inject.name.Named;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.*;

@Log4j2
@Controller
@Path("/api")
public class FindRouteController {

    @Inject
    @Named("aStarFindRouteService")
    private FindRouteService aStarFindRouteService;

    @Inject
    @Named("jpsFindRouteService")
    private FindRouteService jpsFindRouteService;

    @POST
    @Path("/find-route")
    @Produces("application/json")
    public MyResponse<List<?>> findPath(@QueryParam("sx") Integer sx,
                                           @QueryParam("sy") Integer sy,
                                           @QueryParam("tx") Integer tx,
                                           @QueryParam("ty") Integer ty,
                                           @QueryParam("ac") String ac,
                                           String[] blockIds) {
        if (ac!=null && ac.equals("jps")) {
            log.info("zz");
            return MyResponse.ok(
                    jpsFindRouteService.walkingRoute(new FTile(sx, sy), new FTile(tx,ty), closeFTileTable(blockIds))
            );
        }
        return MyResponse.ok(
                aStarFindRouteService.walkingRoute(new Tile(sx, sy), new Tile(tx,ty), closeTileTable(blockIds))
        );
    }

    Table<Integer, Integer, Tile> closeTileTable(String[] blockIds) {
        Table<Integer, Integer, Tile> closeTileTable = HashBasedTable.create();
        if (blockIds!=null && blockIds.length>=1) {
            Arrays.asList(blockIds).forEach((id) -> {
                String[] idXY = id.split("-");
                Integer[] idp = new Integer[2];
                idp[0] = Integer.parseInt(idXY[0]);
                idp[1] = Integer.parseInt(idXY[1]);
                closeTileTable.put(idp[0], idp[1], new Tile(idp[0], idp[1]));
            });
        }
        return closeTileTable;
    }

    Table<Integer, Integer, FTile> closeFTileTable(String[] blockIds) {
        Table<Integer, Integer, FTile> closeTileTable = HashBasedTable.create();
        if (blockIds!=null && blockIds.length>=1) {
            Arrays.asList(blockIds).forEach((id) -> {
                String[] idXY = id.split("-");
                Integer[] idp = new Integer[2];
                idp[0] = Integer.parseInt(idXY[0]);
                idp[1] = Integer.parseInt(idXY[1]);
                closeTileTable.put(idp[0], idp[1], new FTile(idp[0], idp[1]));
            });
        }
        return closeTileTable;
    }
}
