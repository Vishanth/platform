var apiProvider = require("/core/greg/greg.js").getAPIProviderObj();

var getSubscribersOfAPI = function (apiName, version) {

    var user = require("/core/user/user.js").getUser();
    var providerName = user.username;
    var subscribersOut = [];
    var subscribersArray = [];
    try {
        subscribersArray = apiProvider.getSubscribersOfAPI(providerName, apiName, version);
        if (log.isDebugEnabled()) {
            log.debug("getSubscribersOfAPI : " + stringify(subscribersArray));
        }
        if (subscribersArray == null) {
            return {
                error:true
            };

        } else {
            for (var k = 0; k < subscribersArray.length; k++) {
                var elem = {
                    userName:subscribersArray[k].userName,
                    subscribedDate:subscribersArray[k].subscribedDate
                };
                subscribersOut.push(elem);
            }
            return {
                error:false,
                subscribers:subscribersOut
            };
        }
    } catch (e) {
        log.error(e.message);
        return {
            error:e,
            subscribers:null
        };
    }

};

var getSubscribersOfProvider = function () {
    var subscribersOut = [];
    var subscribersArray = [];
    var user = require("/core/user/user.js").getUser();
    var provider = user.username;
    try {
        subscribersArray = apiProvider.getAllAPIUsageByProvider(provider);
        if (log.isDebugEnabled()) {
            log.debug("getSubscribersOfProvider : " + stringify(subscribersArray));
        }
        if (subscribersArray == null) {
            return {
                error:true
            };

        } else {
            for (var k = 0; k < subscribersArray.length; k++) {
                var elem = {
                    userName:subscribersArray[k].userName,
                    application:subscribersArray[k].application,
                    apis:subscribersArray[k].apis
                };
                subscribersOut.push(elem);
            }
            return {
                error:false,
                subscribers:subscribersOut
            };
        }
    } catch (e) {
        log.error(e.message);
        return {
            error:e,
            subscribers:null
        };
    }

};

var getSubscribedAPIs = function (username) {
    var apisOut = [];
    var apis = [];
    try {
        apis = apiProvider.getSubscribedAPIs(username);
        if (log.isDebugEnabled()) {
            log.debug("getSubscribedAPIs : " + stringify(apis));
        }
        if (apis == null) {
            return {
                error:true
            };
        }
        else {
            for (var k = 0; k < apis.length; k++) {
                var elem = {
                    name:apis[k].apiName,
                    version:apis[k].version,
                    lastUpdatedDate:apis[k].updatedDate
                };
                apisOut.push(elem);
            }
            return {
                error:false,
                apis:apisOut
            };
        }
    } catch (e) {
        log.error(e.message);
        return {
            error:e,
            apis:null
        };
    }
};