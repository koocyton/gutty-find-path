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

    window.socket = function (uri, protocolHeaders) {
        let protocol = /^https/.test(window.location.protocol) ? "wss\:\/\/" : "ws\:\/\/";
        this.url = /^ws/.test(uri) ? uri : protocol + window.location.host + uri;
        this.protocolHeaders = protocolHeaders;
        this.ws = (typeof (this.protocolHeaders) === "object")
            ? new WebSocket(this.url, this.protocolHeaders)
            : new WebSocket(this.url);
    };
    window.socket.prototype.reconnect = function () {
        this.ws = (typeof (this.protocolHeaders) === "object")
            ? new WebSocket(this.url, this.protocolHeaders)
            : new WebSocket(this.url);
    };
    window.socket.prototype.onOpen = function (callOpen) {
        if (typeof callOpen === "function") {
            this.ws.onopen = callOpen;
        }
        return this;
    };
    window.socket.prototype.onClose = function (callClose) {
        if (typeof callClose === "function") {
            this.ws.onclose = callClose;
        }
        return this;
    };
    window.socket.prototype.onError = function (callError) {
        if (typeof callError === "function") {
            this.ws.onerror = callError;
        }
        return this;
    };
    window.socket.prototype.onMessage = function (callMessage) {
        if (typeof callMessage === "function") {
            this.ws.onmessage = callMessage;
        }
        return this;
    };
    window.socket.prototype.sendString = function (message) {
        this.ws.send(message);
    };
    window.socket.prototype.sendJson = function (obj) {
        this.ws.send(JSON.stringify(obj));
    };
    window.socket.prototype.sendBinary = function (obj) {
        this.ws.send(JSON.stringify(obj));
    };
    window.socket.prototype.close = function () {
        try {
            this.ws.close();
        } catch (e) {
        }
    };

    let game = null;

    window.Game = function() {
        this.container = $("div#agar-container")
        this.canvas = $("canvas#agar-canvas");
        this.context = this.canvas[0].getContext("2d");
        this.socket = null;
        this.pageX = 0;
        this.pageY = 0;
        this.me = null;
        this.players = {};
        this.foods = {};
        this.lastToX = null;
        this.lastToY = null;
        this.resize();
    }

    window.Game.start = function () {
        Game.login(function(token, user){
            if (game!=null) {
                game.play(token, user);
            }
        });
        return false;
    }

    window.Game.login = function (onSuccess) {
        let name = $("input[name='name']").val();
        let myInfoRequest = function(token) {
            let headers = {"User-Token": token};
            ajax.get("/api/me", headers, function (obj) {
                if (obj && obj.code === 0) {
                    onSuccess(token, obj.data);
                }
            });
        }
        ajax.post("/api/login", JSON.stringify({"name": name}), null, function (obj) {
            if (obj.data != null && obj.data.user_token.length > 1) {
                myInfoRequest(obj.data.user_token, onSuccess)
            }
        });
    }

    window.Game.prototype.resize = function () {
        this.devicePixelRatio = window.devicePixelRatio || 1;
        this.width = $(window).width();
        this.height = $(window).height();
        this.canvasWidth = this.width * this.devicePixelRatio;
        this.canvasHeight = this.height * this.devicePixelRatio;
        this.container.css({"width": this.width, "height": this.height});
        this.canvas.css({"width": this.width, "height": this.height});
        this.canvas.attr({"width": this.canvasWidth, "height": this.canvasHeight});
    }

    window.Game.prototype.play = function (token, user) {
        let that = this;
        this.connect(token, user, function(){
            that.me = user;
            that.moveToX = user.x;
            that.moveToY = user.y;
            that.refreshCanvas();
            that.listenMove();
        }, function (util) {
            if (util.type==="cell") {
                if (util.id !== that.me.id) {
                    if (typeof that.players[util.id]=="undefined" || that.players[util.id].time<util.time) {
                        that.players[util.id] = util;
                    }
                } else {
                    if (that.me==null || that.me.time<util.time) {
                        if (that.lastToX == null || that.lastToY == null) {
                            that.lastToX = that.me.x;
                            that.lastToY = that.me.y;
                        }
                        that.me = util;
                    }
                    else {
                        log.info({
                            "message" : "接受到的超时的数据",
                            "me_time": that.me.time,
                            "util_time": util.time,
                            "me": that.me,
                            "util":util
                        });
                    }
                }
            }
            else if (util.type==="food") {
                that.foods[util.id] = util;
            }
            else if (util.type==="remove-food") {
                delete that.foods[util.id];
            }
        });
    };

    window.Game.prototype.connect = function(token, user, onOpen, onMessage) {
        this.socket = new socket("/ws/agar-io", ["User-Token", token]);
        this.socket.onOpen(function () {
            onOpen();
        });
        let that = this;
        this.socket.onMessage(function (msg) {
            if (msg.data==="youDie") {
                this.me = null;
                window.alert("You die ...");
                location.reload();
                return;
            }
            let receiveUsers = msg.data.split(/\n/);
            that.players = {0:false};
            receiveUsers.forEach(function (receiveUser) {
                receiveUser = receiveUser.split(" ");
                if (receiveUser.length >= 4) {
                    let util = null
                    if (receiveUser[0]==="cell") {
                        util = {
                            type: receiveUser[0],
                            time: 1 * receiveUser[1],
                            id: receiveUser[2],
                            name: receiveUser[3],
                            color: receiveUser[4],
                            gradle: 1 * receiveUser[5],
                            x: 1 * receiveUser[6],
                            y: 1 * receiveUser[7],
                        };
                    }
                    else if (receiveUser[0]==="food" || receiveUser[0]==="remove-food") {
                        util = {
                            type: receiveUser[0],
                            id: receiveUser[1],
                            color: receiveUser[2],
                            gradle: 1 * receiveUser[3],
                            x: 1 * receiveUser[4],
                            y: 1 * receiveUser[5],
                        };
                    }
                    if (util!=null) {
                        onMessage(util);
                    }
                }
            });
            that.players[0] = true;
            that.runTimer();
        });
    }

    window.Game.prototype.refreshCanvas = function() {
        this.canvas.css("background", "#ffffff");
        $("div.form-content").hide();
    };

    window.Game.prototype.drawMap = function(user) {
        let interval = 100;
        let mapWidth = Math.ceil(this.canvasWidth/interval) * interval;
        let mapHeight = Math.ceil(this.canvasHeight/interval) * interval;

        for (let ii=0; ii<=mapWidth/interval; ii++) {
            let startX = ii * interval - user.x % interval;
            this.context.moveTo(startX, 0);
            this.context.lineTo(startX, this.canvasHeight);
        }
        for (let ii=0; ii<=mapHeight/interval; ii++) {
            let startY = ii * interval - user.y % interval;
            this.context.moveTo (0, startY);
            this.context.lineTo(this.canvasWidth, startY);
        }
        this.context.lineWidth = 1;
        this.context.strokeStyle = "#aaa" ;
        this.context.stroke();
    };

    window.Game.prototype.drawPlayer = function(user) {
        let x = user.x - this.me.x + this.canvasWidth / 2;
        let y = user.y - this.me.y + this.canvasHeight / 2;
        if (user.id===this.me.id) {
            x = this.canvasWidth / 2;
            y = this.canvasHeight / 2;
        }
        this.context.beginPath();
        this.context.fillStyle = "#" + user.color;
        this.context.moveTo(x, y);
        this.context.arc(x, y, Math.sqrt(user.gradle/Math.PI),0,Math.PI*2,20);//x,y坐标,半径,圆周率
        // this.context.transform(1, 2, 3, 4, 5, 6);
        this.context.closePath();
        this.context.fill();

        let textStyle = "#FFFFFF";
        let strokeStyle = "#444444";
        if (user.name!=null) {
            this.context.font = (1 * user.gradle / 400) + "px bold 黑体";
            this.context.fillStyle = textStyle;
            this.context.textAlign = "center";
            this.context.textBaseline = "middle";
            this.context.strokeStyle = strokeStyle;
            this.context.strokeText(user.name, x, y);
            this.context.fillStyle = textStyle;
            this.context.fillText(user.name, x, y);
        }
    };

    window.Game.prototype.drawFood = function(food) {
        let x = food.x - this.me.x + this.canvasWidth / 2;
        let y = food.y - this.me.y + this.canvasHeight / 2;
        this.context.beginPath();
        this.context.fillStyle = "#" + food.color;
        this.context.moveTo(x, y);
        this.context.arc(x, y, Math.sqrt(food.gradle/Math.PI),0,Math.PI*2,20);//x,y坐标,半径,圆周率
        this.context.closePath();
        this.context.fill();
    };

    window.Game.prototype.runTimer = function() {
        this.context.clearRect(0,0, this.canvasWidth, this.canvasHeight);
        if (this.me!=null) {
            this.drawMap(this.me)
        }
        let that = this;
        if (this.players[0]===true) {
            $.each(this.foods, function (userId, food) {
                that.drawFood(food);
            });
            $.each(this.players, function (userId, player) {
                that.drawPlayer(player);
            });
        }
        if (this.me!=null) {
            this.drawPlayer(this.me);
        }
        //requestAnimationFrame(function(time){
        //    that.runTimer();
        //});
    };

    window.Game.prototype.listenMove = function() {
        let that = this;
        this.canvas.on("mousemove", function(ev) {
            that.pageX = ev.pageX;
            that.pageY = ev.pageY;
        });
        this.sendMoveData();
    };

    // let lastTime = 0;
    window.Game.prototype.sendMoveData = function() {
        if (this.me!=null) {
            let moveXSpeed = (this.pageX - this.width/2) / 40;
            let moveYSpeed = (this.pageY - this.height/2) / 40;
            let maxXSpeed = moveXSpeed>=0 ? 6 : -6;
            let maxYSpeed = moveYSpeed>=0 ? 6 : -6;
            let moveToX = Math.abs(moveXSpeed)>6 ? this.me.x + maxXSpeed : this.me.x + moveXSpeed;
            let moveToY = Math.abs(moveYSpeed)>6 ? this.me.y + maxYSpeed : this.me.y + moveYSpeed;
            // moveToX = moveToX < 0 ? 0 : (moveToX > 5000 ? 5000 : moveToX);
            // moveToY = moveToY < 0 ? 0 : (moveToY > 5000 ? 5000 : moveToY);
            // if (moveToX!==this.me.x || moveToY!==this.me.y) {
            // if (this.lastToX===this.me.x && this.lastToY===this.me.y) {
            this.socket.sendString(moveToX + " " + moveToY);
            // this.lastToX=moveToX;
            // this.lastToY=moveToY;
            // }
            // }
        }
        let that=this;
        requestAnimationFrame(function(time){
            that.sendMoveData();
        });
        // let currTime = new Date().getTime();
        // let timeToCall = Math.max(0, 16 - (currTime - lastTime));
        // setTimeout(function(){
        //    that.sendMoveData();
        // },8.3);
        // lastTime = currTime + timeToCall;
    };

    $(document).ready(function(){
        game = new Game();
        $(window).bind('resize', function (){
            game.resize();
        });
        game.runTimer();
    });

})(jQuery);

