var hc_gloab_window = window ? window : wx ? wx : global;
if (!hc_gloab_window) {
    throw "not found gloab window"
}
hc_gloab_window.binObj = function (newNavigator, origNavigator) {
    // todo 取之 cordova.js
    let defineGetterSetter_inner = function (obj, key, getFunc, opt_setFunc) {
        if (Object.defineProperty) {
            var desc = {
                get: getFunc,
                configurable: true
            };
            if (opt_setFunc) {
                desc.set = opt_setFunc;
            }
            Object.defineProperty(obj, key, desc);
        } else {
            obj.__defineGetter__(key, getFunc);
            if (opt_setFunc) {
                obj.__defineSetter__(key, opt_setFunc);
            }
        }
    };
    for (var key in origNavigator) {
        if (typeof origNavigator[key] == 'function') {
            newNavigator[key] = origNavigator[key].bind(origNavigator);
        } else {
            (function (k) {
                defineGetterSetter_inner(newNavigator, key, function () {
                    return origNavigator[k];
                });
            })(key);
        }
    }
}

if (!hc_gloab_window.cordova) {
    hc_gloab_window.cordova = {};
    hc_gloab_window.cordova.isInit = false
}


if (typeof (wx) != "undefined") {
    let arg = {};
    if (!hc_gloab_window.cordova.wxObj) {
        hc_gloab_window.cordova.wxObj = {};
    }
    module.exports = hc_gloab_window.cordova.wxObj;
}



// if (wx) {
//
// }

if (hc_gloab_window.cordova.isInit) {
   // console.error("reach here ok")
    if (!hc_gloab_window.navigator) {
        hc_gloab_window.navigator = hc_gloab_window.cordova.navigator;
    } else {
        hc_gloab_window.navigator = hc_gloab_window.cordova.navigator;
    }
    if (!hc_gloab_window.document) {
        hc_gloab_window.document = hc_gloab_window.cordova.document;
    } else {
        hc_gloab_window.document = hc_gloab_window.cordova.document;
    }
} else {
    hc_gloab_window.cordova.isInit = true;
    // todo 替换navigator
    (function () {


        function replaceNavigator_inner(origNavigator) {

            // todo 取之 cordova.js
            let defineGetterSetter_inner = function (obj, key, getFunc, opt_setFunc) {
                if (Object.defineProperty) {
                    var desc = {
                        get: getFunc,
                        configurable: true
                    };
                    if (opt_setFunc) {
                        desc.set = opt_setFunc;
                    }
                    Object.defineProperty(obj, key, desc);
                } else {
                    obj.__defineGetter__(key, getFunc);
                    if (opt_setFunc) {
                        obj.__defineSetter__(key, opt_setFunc);
                    }
                }
            };

            var CordovaNavigator = function () {
            };
            CordovaNavigator.prototype = origNavigator;
            var newNavigator = new CordovaNavigator();
            // This work-around really only applies to new APIs that are newer than Function.bind.
            // Without it, APIs such as getGamepads() break.
            if (CordovaNavigator.bind) {
                for (var key in origNavigator) {
                    if (typeof origNavigator[key] == 'function') {
                        newNavigator[key] = origNavigator[key].bind(origNavigator);
                    } else {
                        (function (k) {
                            defineGetterSetter_inner(newNavigator, key, function () {
                                return origNavigator[k];
                            });
                        })(key);
                    }
                }
            }
            return newNavigator;
        }

        if (!hc_gloab_window.navigator) {
            hc_gloab_window.cordova.navigator = {};
        }
        hc_gloab_window.cordova.navigator = replaceNavigator_inner(hc_gloab_window.navigator);
        hc_gloab_window.navigator = hc_gloab_window.cordova.navigator;
    })();


    hc_gloab_window.cordova.pluginListNum = 0;
    hc_gloab_window.cordova.pluginList = {};
    hc_gloab_window.cordova.thirdPartyOk = false;
    hc_gloab_window.cordova.gloabFieldNumble = 0;
    hc_gloab_window.cordova.OnDeviceReadyMethodList = [];

    if (hc_gloab_window.document) {
        hc_gloab_window.cordova.document = hc_gloab_window.document;
    } else {
        hc_gloab_window.cordova.document = {};
        hc_gloab_window.document = hc_gloab_window.cordova.document;
    }
    if (hc_gloab_window.navigator) {
        hc_gloab_window.cordova.navigator = hc_gloab_window.navigator;
    } else {
        hc_gloab_window.cordova.navigator = {};
    }
    try {
        // todo 获取本js文本运行位置
        function hc_getPath_inner() {
            var jsPath = document.currentScript ? document.currentScript.src : function () {
                var js = document.scripts,
                    last = js.length - 1,
                    src;
                for (var i = last; i > 0; i--) {
                    if (js[i].readyState === 'interactive') {
                        src = js[i].src;
                        break;
                    }
                }
                return src || js[last].src;
            }();
            return jsPath.substring(0, jsPath.lastIndexOf('/') + 1);
        }

        hc_gloab_window.cordova.local_of_hc_mobile_js = hc_getPath_inner();
    } catch (e) {
    }

    // todo 定义 define
    hc_gloab_window.cordova.define = function (id, func) {
        hc_gloab_window.cordova.pluginList[id] = func;
        hc_gloab_window.cordova.pluginListNum++;
    }

    // todo 定义 require
    hc_gloab_window.cordova.require = function (arg) {
        switch (arg) {
            case 'cordova/utils':
                var utils = {};
                utils.defineGetterSetter = function (obj, key, getFunc, opt_setFunc) {
                    if (Object.defineProperty) {
                        var desc = {
                            get: getFunc,
                            configurable: true
                        };
                        if (opt_setFunc) {
                            desc.set = opt_setFunc;
                        }
                        Object.defineProperty(obj, key, desc);
                    } else {
                        obj.__defineGetter__(key, getFunc);
                        if (opt_setFunc) {
                            obj.__defineSetter__(key, opt_setFunc);
                        }
                    }
                };

                utils.defineGetter = utils.defineGetterSetter;

                utils.typeName = function (val) {
                    return Object.prototype.toString.call(val).slice(8, -1);
                };

                utils.isArray = Array.isArray ||
                    function (a) {
                        return utils.typeName(a) == 'Array';
                    };

                utils.isDate = function (d) {
                    return (d instanceof Date);
                };
                return utils;
        }
    }


    // todo 尝试触发onDeviceReady事件
    hc_gloab_window.cordova.tryInvokeOnDeviceReady = function () {
        if (hc_gloab_window.cordova.thirdPartyOk && (hc_gloab_window.cordova.gloabFieldNumble === 0) && (hc_gloab_window.cordova.pluginListNum > 1)) {
            hc_gloab_window.cordova.alert("invoke success onDeviceReady")
            hc_gloab_window.cordova.OnDeviceReadyMethodList.forEach(function (funcAElement) {
                funcAElement();
            })
            hc_gloab_window.cordova.OnDeviceReadyMethodList = [];
            return true;
        }
        return false;
    }


    // todo 快捷注册此方法不可跨平台使用
    hc_gloab_window.cordova.hc_forbidFunc_office = function (obj, met, objPath) {
        let oriNav = !objPath ? "navigotor." : "";
        objPath = !objPath ? hc_gloab_window.navigator : objPath;
        if (objPath[obj] == null) {
            objPath[obj] = {};
        }
        let oriObj = obj;
        obj = objPath[obj];
        obj[met] = function (arg1, arg2, arg3, arg4, arg5) {
            if (!arguments || arguments.length === 0) {
                let info = 'not support this funcation in this platform: ' + hc_gloab_window.cordova.platform + ' : ' + oriNav + oriObj + "." + met;
                hc_gloab_window.cordova.error(info);
                return;
            }
            for (let i = arguments.length - 1; i >= 0; i--) {
                let argItem = arguments[i];
                if (Object.prototype.toString.call(argItem).slice(8, -1) === "Function") {
                    argItem('not support this funcation in this platform: ' + hc_gloab_window.cordova.platform + oriNav + oriObj + "." + met)
                    let info = 'not support this funcation in this platform: ' + hc_gloab_window.cordova.platform + ' : ' + oriNav + oriObj + "." + met;
                    hc_gloab_window.cordova.error(info);
                }
            }
        }
    };

    // todo 当第三方初始化成功后调用
    hc_gloab_window.cordova.thirdPartySdkInitSuccess = function () {
        hc_gloab_window.cordova.thirdPartyOk = true;
        hc_gloab_window.cordova.tryInvokeOnDeviceReady();
    };


    hc_gloab_window.cordova.alert = function (info) {
        // hc_gloab_window.cordova.alert("cordova info:  " + cordova.thirdPartyOk + " : " + cordova.gloabFieldNumble + " : " + cordova.pluginListNum)
        console.log(info);
        // window.alert(info);
    }
    hc_gloab_window.cordova.error = function (info) {
        // hc_gloab_window.cordova.alert("cordova info:  " + cordova.thirdPartyOk + " : " + cordova.gloabFieldNumble + " : " + cordova.pluginListNum)
        console.error(info);
    }

    hc_gloab_window.cordova.loadScript = function (jsLocal, onLoad, onError) {
        // ccccccc " + jsLocal)
        function defLoadJs() {
            let head = hc_gloab_window.document.getElementsByTagName('head')[0];
            let script = hc_gloab_window.document.createElement('script');
            script.type = 'text/javascript';
            script.src = hc_gloab_window.cordova.local_of_hc_mobile_js + jsLocal;
            script.onload = function () {
                if (onLoad) {
                    onLoad();
                }
            }
            script.onerror = function () {
                if (onError) {
                    onError();
                }
            }
            head.appendChild(script);
        }

        function loadWxJs() {
            require(jsLocal)
            if (onLoad) {
                onLoad();
            }
        }

        if (typeof (wx) != "undefined") {
            loadWxJs();
        }else {
            defLoadJs();
        }

    }


    // todo 处理注册 deviceready 事件
    let m_document_addEventListener = hc_gloab_window.cordova.document.addEventListener;
    let m_window_addEventListener = hc_gloab_window.addEventListener;
    hc_gloab_window.cordova.document.addEventListener = function (event, handler, useCapture) {
        if (event === "deviceready") {
            hc_gloab_window.cordova.OnDeviceReadyMethodList.push(handler);
            hc_gloab_window.cordova.tryInvokeOnDeviceReady();
        } else {
            if (m_document_addEventListener) {
                m_document_addEventListener.call(hc_gloab_window.cordova.document, event, handler, useCapture)
            }
        }
    }
    hc_gloab_window.addEventListener = function (event, handler, capture) {
        if (event === "deviceready") {
            hc_gloab_window.cordova.OnDeviceReadyMethodList.push(handler);
            hc_gloab_window.cordova.tryInvokeOnDeviceReady();
        } else {
            if (m_window_addEventListener) {
                m_window_addEventListener.call(hc_gloab_window, event, handler, capture);
            }
        }
    };


}


// todo 使 onDeviceReady 可用
function prepareCordovaLikeEnv() {
    hc_gloab_window.cordova.loadScript('cordova_plugins.js', function () {
        function declareFiled(name, filePath, id) {
            hc_gloab_window.cordova.gloabFieldNumble++;
            try {
                hc_gloab_window.cordova.loadScript(filePath,
                    function () {
                        hc_gloab_window.cordova.gloabFieldNumble--;
                        try {
                            let moduleItem = {};
                            moduleItem.exports = {}
                            hc_gloab_window.cordova.pluginList[id](hc_gloab_window.cordova.require, moduleItem.exports, moduleItem);
                            hc_gloab_window[name] = moduleItem.exports;
                        } catch (e) {
                            console.log(e + "  : " + name + "   " + id);
                        }
                        hc_gloab_window.cordova.tryInvokeOnDeviceReady();
                    },
                    function () {
                        hc_gloab_window.cordova.gloabFieldNumble--;
                        hc_gloab_window.cordova.tryInvokeOnDeviceReady();
                    });
            } catch (e) {
                console.log("------>"+e);
                hc_gloab_window.cordova.gloabFieldNumble--;
            }
        }

        let navigatorStart = new RegExp("^navigator");
        let windowStart = new RegExp("^window");
        let modules = {};
        hc_gloab_window.cordova.pluginList["cordova/plugin_list"](null, null, modules);
        let moduleList = modules.exports;
        for (let i = 0; i < moduleList.length; i++) {
            let item = moduleList[i];
            let clobbers = item.clobbers;
            let merges = item.merges;
            if (clobbers) {
                for (let j = 0; j < clobbers.length; j++) {
                    let clobItem = clobbers[j];
                    if ((!clobItem) || navigatorStart.test(clobItem) || windowStart.test(clobItem)) {
                        continue
                    }
                    declareFiled(clobItem, item.file, item.id);
                }
            }
            if (merges) {
                for (var k = 0; k < merges.length; k++) {
                    var mergeItem = merges[k];
                    if (navigatorStart.test(mergeItem) || windowStart.test(mergeItem)) {
                        continue
                    }
                    declareFiled(mergeItem, item.file, item.id);
                }
            }
        }
    });

}


// 不同运行环境分开处理
if (typeof (wx) != "undefined") {
    prepareCordovaLikeEnv();
    hc_gloab_window.cordova.platform = "wx"
    require("./wechat.js")
} else if (/dingTalk/i.test(navigator.userAgent))  {
    prepareCordovaLikeEnv();
    hc_gloab_window.cordova.platform = "钉钉"
    // todo  钉钉小程序环境模拟
    document.write("<script type='text/javascript' src='https://g.alicdn.com/dingding/dingtalk-jsapi/2.10.3/dingtalk.open.js'></script>");
    document.write("<script type='text/javascript' src='" + hc_gloab_window.cordova.local_of_hc_mobile_js + "dingtk.js'></script>");
    window.hc_debug = true;
}


//

// else if (/滴滴/i.test(navigator.userAgent)) {
//     prepareCordovaLikeEnv();
//     cordova.platform = "滴滴"
//     // document.write("<script type='text/javascript' src='./wechat.js'></script>");
//     // todo  滴滴小程序环境模拟
//     // document.write("<script type='text/javascript' src='supconit://cordova.js'></script>");
// } else if (/baidu/i.test(navigator.userAgent)) {
//     prepareCordovaLikeEnv();
//     cordova.platform = "百度"
//     // todo  百度小程序环境模拟
//     // document.write("<script type='text/javascript' src='supconit://cordova.js'></script>");
// } else if (/android/i.test(navigator.userAgent)) {
//     // todo android cordova 运行环境
//     // document.write("<script type='text/javascript' src='supconit://cordova.js'></script>");
// } else if (/ios/i.test(navigator.userAgent)) {
//     // todo ios cordova 运行环境
//     // document.write("<script type='text/javascript' src='supconit://cordova.js'></script>");
// }