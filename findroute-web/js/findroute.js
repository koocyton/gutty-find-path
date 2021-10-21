(function ($) {

    let log = {
        info: function () {
            if (window.console && window.console.log && arguments.length >= 1) {
                window.console.log("%c >> Count : " + arguments.length + " ", "color:white;background:blue;");
                for (let ii = 0; ii < arguments.length; ii++) {
                    window.console.log(arguments[ii]);
                }
            }
        },
        alert: function () {
            if (window.alert && arguments.length >= 1) {
                let s = ">> Count : " + arguments.length;
                for (let ii = 0; ii < arguments.length; ii++) {
                    s = s + "\n" + arguments[ii];
                }
                window.alert(s);
            }
        }
    };

    let ajax = {
        ajaxRequest: function (url, method, data, headers, onSuccess, onError, onComplete) {
            // set headers
            if ($.type(headers) !== "object" || $.isEmptyObject(headers)) {
                headers = {};
            }
            headers['Ajax-Request'] = "jQuery.KTAnchor";
            headers['Content-Type'] = "application/json; charset=UTF-8"
            let contentType = false;
            if (method === "POST") {
                contentType = (data instanceof FormData) ? false : "application/x-www-form-urlencoded; charset=UTF-8";
            }
            $.ajax({
                "url": url,
                "type": method,
                "data": data,
                "contentType": contentType,
                "processData": false,
                "headers": headers,
                "success": function (responseText) {
                    if ($.isFunction(onSuccess)) onSuccess(responseText);
                },
                "error": function (XMLHttpRequest) {
                    if ($.isFunction(onError)) onError(XMLHttpRequest);
                },
                "complete": function (XMLHttpRequest) {
                    if ($.isFunction(onComplete)) onComplete(XMLHttpRequest);
                }
            });
        },
        // get request
        get: function (url, headers, onSuccess, onError) {
            ajax.ajaxRequest(url, "GET", null, headers, function (responseText) {
                ajax.onResponse(responseText, onSuccess, onError);
            });
        },
        // post request
        post: function (url, data, headers, onSuccess, onError) {
            ajax.ajaxRequest(url, "POST", data, headers, function (responseText) {
                ajax.onResponse(responseText, onSuccess, onError);
            });
        },
        // on response
        onResponse: function (obj, onSuccess, onError) {
            if (typeof obj === "string") {
                obj = JSON.parse(obj)
            }
            if (obj.code === 0) {
                if ($.isFunction(onSuccess)) onSuccess(obj);
            } else {
                if ($.isFunction(onError)) onError(obj);
            }
        }
    };

    let Game = function(){
        this.blockList = [];
        this.whiteBlockList = [];
    }

    Game.prototype.drawBlock = function(id){
        let elt = $("#" + id);
        if (elt.attr("class")==="block-tile") {
            for(let ii=0; ii<this.blockList.length; ii++) {
                if (this.blockList[ii]===id) {
                    this.blockList.splice(ii, 1);
                }
            }
            elt.removeClass("block-tile")
        }
        else {
            let idx = this.blockList.length;
            this.blockList[idx] = id;
            elt.addClass("block-tile")
        }
    }

    Game.prototype.drawMap = function(){
        let tiles = ""
        for(let y=59; y>=0; y--) {
            for(let x=0; x<60; x++) {
                let id = x + "-" + y;
                if (this.startX===x && this.startY===y) {
                    tiles += "<li class='start-tile' id='"+id+"' title='"+id+"'></li>";
                }
                else if (this.targetX===x && this.targetY===y) {
                    tiles += "<li class='target-tile' id='"+id+"' title='"+id+"'></li>";
                }
                else {
                    tiles += "<li class='white-block-tile' onclick=\"g.drawBlock('"+id+"')\" id='" + id + "' title='" + id + "'></li>";
                }
            }
        }
        $("div#tile-map").append(tiles);
    }

    Game.prototype.refresh = function() {
        this.startX  = Math.floor(Math.random()*60);
        this.startY  = Math.floor(Math.random()*60);
        this.targetX = Math.floor(Math.random()*60);
        this.targetY = Math.floor(Math.random()*60);
        $("div#tile-map").empty();
        this.blockList = [];
        this.whiteBlockList = [];
        this.drawMap();
    }

    Game.prototype.findRoute = function(ac) {
        let that = this;
        $("li").each(function(idx, e){
            $(e).removeClass("route-tile");
            $(e).removeClass("point-tile");
        });
        ajax.post(
            "/api/find-route?sx="+that.startX+"&sy="+that.startY+"&tx="+that.targetX+"&ty="+that.targetY+"&ac="+ac,
            JSON.stringify(that.blockList.concat(that.whiteBlockList)),
            null,
            function(resp){
                that.drawRoute(resp.data, 0);
            }
        );
    }

    Game.prototype.drawRoute = function(tileList, idx) {
        let tile = tileList[idx];
        let tileElt = $("li#" + tile.x + "-" + tile.y);
        let that = this;
        if (tile.straight_point===true) {
            tileElt.addClass("point-tile");
        }
        else {
            tileElt.addClass("route-tile");
        }
        setTimeout(function(){
            if (idx<=tileList.length-1) {
                that.drawRoute(tileList, idx+1);
            }
        }, 20);
    }

    Game.prototype.whiteBlock = function() {
        let that = this;
        $("li.white-block-tile")
            .removeClass("white-block-tile")
            .addClass("grey-block-tile")
            .each(function(idx, e){
                let elt = $(e);
                if(elt.attr("class")!=="route-tile") {
                    that.whiteBlockList[idx] = elt.attr("id");
                    elt.removeClass("white-block-tile").addClass("grey-block-tile");
                }
                that.whiteBlockList[idx] = $(e).attr("id");
        });
    }

    Game.prototype.whiteUnBlock = function() {
        this.whiteBlockList = [];
        $("li.grey-block-tile")
            .removeClass("grey-block-tile")
            .addClass("white-block-tile");
    }

    $(document).ready(function(){
        window.g = new Game();
        g.refresh();
    });

})(jQuery);

