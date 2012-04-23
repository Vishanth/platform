var invokeService = function (cookie, service, action, payload, opts) {
    var conf = require("/conf/greg.json");
    var server = require("/core/greg/greg.js");
    var ws = require('ws');
    var client = new ws.WSRequest();

    var url = server.getServerURL() + "/" + conf.servicesContext + "/" + service;

    var options = [];
    options.useSOAP = 1.2;
    options.userWSA = 1.0;
    options.action = action;

    if (cookie) {
        options.HTTPHeaders = [
            { name:"Cookie", value:cookie }
        ];
    }

    if (opts) {
        for (var key in opts) {
            if (opts.hasOwnProperty(key)) {
                options[key] = opts[key];
            }
        }
    }

    try {
        client.open(options, url, false);
        client.send(payload);
        return {
            client:client,
            error:false
        };
    } catch (e) {
        log("Error invoking service " + url + ", action : " + options.action + ", " + e.toString());
        return {
            client:client,
            error:e
        };
    }
};