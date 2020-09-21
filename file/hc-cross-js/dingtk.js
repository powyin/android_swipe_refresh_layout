// todo 实现 navigator 各种转化

// todo 3d模型展示
cordova.hc_forbidFunc_office("glPreviewer", "preview");

// todo AdView插件
cordova.hc_forbidFunc_office("adview", "showBannerAd");
cordova.hc_forbidFunc_office("adview", "showInterteristalAd");
cordova.hc_forbidFunc_office("adview", "showNativeAd");
cordova.hc_forbidFunc_office("adview", "showVideoAd");

// todo Alipay插件
navigator.alipay = {};
navigator.alipay.share = function (obj, successCallback, errorCallback) {
    // alert(JSON.stringify(obj));
    var type = ShareUtils.shareObjectTypeOf(obj);
    // 3.判断参数对象的必选属性
    if (!obj.hasLegalProperty()) {
        return errorCallback('参数对象缺少必选属性或者必选属性类型不正确');
    }
    var back = {
        type: 0, //分享类型，0:全部组件 默认；1:只能分享到钉钉；2:不能分享，只有刷新按钮
        url: null,
        title: "",
        content: "",
        image: null,
        onSuccess: function (ret) {
            if (successCallback) {
                successCallback(ret);
            }
        },
        onFail: function (err) {
            if (errorCallback) {
                errorCallback(err);
            }
        }
    }
    switch (type) {
        case ShareObjectType.Text:
            back.content = obj.text ? obj.text : "";
            break;
        case ShareObjectType.Image:
            back.image = obj.shareImage ? obj.shareImage : null;
            back.title = "图片"
            back.url = obj.shareImage ? obj.shareImage : null;
            break;
        case ShareObjectType.Music:
            back.url = obj.musicUrl ? obj.musicUrl : null;
            back.title = obj.title ? obj.title : "";
            back.content = obj.description ? obj.description : "";
            back.image = obj.thumbImage ? obj.thumbImage : null;
            break;
        case ShareObjectType.Video:
            back.url = obj.videoUrl ? obj.videoUrl : null;
            back.title = obj.title ? obj.title : "";
            back.content = obj.description ? obj.description : "";
            back.image = obj.thumbImage ? obj.thumbImage : null;
            break;
        case ShareObjectType.Webpage:
            back.webpageUrl = obj.webpageUrl ? obj.webpageUrl : null;
            back.title = obj.title ? obj.title : "";
            back.content = obj.description ? obj.description : "";
            back.image = obj.thumbImage ? obj.thumbImage : null;
            break;
        default:
            break;
    }
    dd.biz.util.share(back)
}

// todo App插件
navigator.app = {};
cordova.hc_forbidFunc_office("app", "reset");
cordova.hc_forbidFunc_office("app", "resetUrl");
navigator.app.exit = function () {
    dd.biz.navigation.close({
        onSuccess: function (result) {},
        onFail: function (err) {}
    })
}
navigator.app.openURL = function (_url, successCallback, errorCallback) {
    dd.biz.util.openLink({
        url: _url, //要打开链接的地址
        onSuccess: function (result) {
            if (successCallback) {
                successCallback();
            }
        },
        onFail: function (err) {
            if (errorCallback) {
                errorCallback(err);
            }
        }
    })
}

// todo AppInfo插件
navigator.appInfo = {}
navigator.appInfo.getInfo = function (successCallback) {
    let ret = {};
    ret.id = "com.ali.dingtalk";
    ret.name = "钉钉";
    ret.isRelease = true;
    ret.onLineId = "";
    if (successCallback) {
        successCallback(ret);
    }
}

// todo ArcSoftFace插件
cordova.hc_forbidFunc_office("arcSoftFace", "imageModeCheck");
cordova.hc_forbidFunc_office("arcSoftFace", "faceFeatureComparison");
cordova.hc_forbidFunc_office("arcSoftFace", "videoModeCheck");

// todo 录音
navigator.audio = {};
navigator.audio.recording = function () {
    alert("需要前端插入一个控制录音的页面")
}

// todo 录音文件选取
cordova.hc_forbidFunc_office("audioPicker", "open");

// todo 录音浏览
navigator.audioPreviewer = {};
navigator.audioPreviewer.preview = function () {}

// todo 身份证识别
cordova.hc_forbidFunc_office("bdidcard", "recognizeIDCard");
cordova.hc_forbidFunc_office("bdidcard", "recognizeIDCardQuality");

// todo 营业执照识别
cordova.hc_forbidFunc_office("bdlicensebusiness", "licensebusiness");

// todo 车牌识别
cordova.hc_forbidFunc_office("bdlicenseplate", "licenseplate");

// todo BDRecognition插件
cordova.hc_forbidFunc_office("bdrecognition", "generalBasic");
cordova.hc_forbidFunc_office("bdrecognition", "general");
cordova.hc_forbidFunc_office("bdrecognition", "accurateBasic");
cordova.hc_forbidFunc_office("bdrecognition", "accurate");
cordova.hc_forbidFunc_office("bdrecognition", "generalWebImage");


// todo Bluetooth插件（连接外设）
cordova.hc_forbidFunc_office("bluetooth", "scan");
cordova.hc_forbidFunc_office("bluetooth", "stopScan");
cordova.hc_forbidFunc_office("bluetooth", "connect");
cordova.hc_forbidFunc_office("bluetooth", "disconnect");
cordova.hc_forbidFunc_office("bluetooth", "update");
cordova.hc_forbidFunc_office("bluetooth", "write");
cordova.hc_forbidFunc_office("bluetooth", "read");
cordova.hc_forbidFunc_office("bluetooth", "writeWithoutResponse");
cordova.hc_forbidFunc_office("bluetooth", "startNotification");
cordova.hc_forbidFunc_office("bluetooth", "stopNotification");
cordova.hc_forbidFunc_office("bluetooth", "isConnected");

// todo Camera插件
navigator.camera = {}
cordova.hc_forbidFunc_office("camera", "takePhoto");
cordova.hc_forbidFunc_office("camera", "shootVideo");


// todo Connection插件
if (!navigator.connection) {
    navigator.connection = {};
}
try {
    navigator.connection = Connection.UNKNOWN;
} catch (e) {}
document.addEventListener('deviceready', function onDeviceReady() {
    dd.device.base.getPhoneInfo({
        onSuccess: function (data) {
            switch (data.netInfo) {
                case "wifi":
                    navigator.connection = Connection.WIFI;
                    break
                case "4g":
                    navigator.connection = Connection.CELL_4G;
                    break
                case "3g":
                    navigator.connection = Connection.CELL_3G;
                    break
            }
        },
        onFail: function (err) {}
    });
});

// todo 获取崩溃日志
cordova.hc_forbidFunc_office("crash", "getCrashReport");
cordova.hc_forbidFunc_office("crash", "deleteCrashReport");


// todo Database插件
cordova.hc_forbidFunc_office("db", "execSQL");
cordova.hc_forbidFunc_office("db", "rawQuery");
cordova.hc_forbidFunc_office("db", "loadFromJson");


// todo Device插件   需要补充
document.addEventListener('deviceready', function onDeviceReady() {
    dd.device.base.getPhoneInfo({
        onSuccess: function (data) {
            let device = {};
            device.modal = data.modal;
            device.platform = "android";
            device.version = data.version;
        },
        onFail: function (err) {}
    });
});

// todo Dialing插件     需要补充 健全
navigator.dialing = {};
navigator.dialing.dial = function (success, failure, phoneNumber, isDialNow) {
    alert("------------>>>>>>>>>>>")
    dd.biz.telephone.showCallMenu({
        phoneNumber: phoneNumber, // 期望拨打的电话号码
        code: '+86', // 国家代号，中国是+86
        showDingCall: false, // 是否显示钉钉电话
        onSuccess: success,
        onFail: failure
    })
}

// todo 警告对话框
navigator.notification = {};
navigator.notification.alert = function (message, callback, title, buttonLabel) {
    dd.device.notification.alert({
        message: message,
        title: title,
        buttonName: buttonLabel,
        onSuccess: callback,
        onFail: function (err) {}
    });
}
navigator.notification.confirm = function (message, callback, title, buttonLabels) {
    if (!buttonLabels) {
        buttonLabels = ['ok', 'cancel'];
    }
    dd.device.notification.confirm({
        message: message,
        title: title,
        buttonLabels: buttonLabels,
        onSuccess: function (res) {
            if (callback) {
                callback(res.buttonIndex);
            }
        },
        onFail: function (err) {}
    });
}

navigator.notification.prompt = function (message, callback, title, buttonLabels, defaultText) {
    dd.device.notification.prompt({
        message: message,
        title: title,
        defaultText: defaultText,
        buttonLabels: buttonLabels,
        onSuccess: function (result) {
            var res = {};
            res.buttonIndex = result.buttonIndex;
            res.input1 = result.value;
            if (callback) {
                callback(res);
            }
        },
        onFail: function (err) {}
    });
}
navigator.notification.beep = function (time) {
    if (!time) {
        time = 300;
    } else {
        try {
            time = time * 300;
        } catch (e) {}
    }
    dd.device.notification.vibrate({
        duration: time, //震动时间，android可配置 iOS忽略
        onSuccess: function (result) {},
        onFail: function (err) {}
    })
}

// todo 文件选取
cordova.hc_forbidFunc_office("documentPicker", "open");

// todo 文件预览
cordova.hc_forbidFunc_office("documentPicker", "open");

// todo 图片，视频，录音，文件选取
cordova.hc_forbidFunc_office("filePicker", "open");

// todo 文件传输
function FileTransfer() {
    this.download = function (downloadUrl, successCallback, errorCallback) {
        alert("download is not allow action in android system");
    }
    this.upload = function () {
        alert("upload is not allow action in android system");
    }
    this.abort = function () {
        alert("upload is not allow action in android system");
    }
}

window.FileTransfer = FileTransfer;
window.resolveLocalFileSystemURL = function () {
    alert("resolveLocalFileSystemURL is not allow action in android system");
}

// todo 指纹验证
cordova.hc_forbidFunc_office("fingerprint", "isAvailable");
cordova.hc_forbidFunc_office("fingerprint", "verifyFingerprint");

// todo Geolocation插件
navigator.geolocation = {};
navigator.geolocation.getCurrentPosition = function (successCallback, errorCallback, options) {
    dd.device.geolocation.get({
        targetAccuracy: 200,
        coordinate: 1,
        withReGeocode: false,
        useCache: true,
        onSuccess: function (result) {
            if (successCallback) {
                let ret = {};
                ret.coords = {};
                ret.coords.latitude = result.latitude;
                ret.coords.longitude = result.longitude;
                successCallback(ret);
            }
        },
        onFail: function (err) {
            if (errorCallback) {
                errorCallback(err);
            }
        }
    });
    return "random_time";
}
navigator.geolocation.watchPosition = navigator.geolocation.getCurrentPosition;
navigator.geolocation.clearWatch = function () {}

// todo Hik插件
cordova.hc_forbidFunc_office("hik", "login");
cordova.hc_forbidFunc_office("hik", "previewVideo");
cordova.hc_forbidFunc_office("hik", "queryRegionResources");
cordova.hc_forbidFunc_office("hik", "queryControlUnitResources");

// todo IM插件
cordova.hc_forbidFunc_office("nim", "registerNim");
cordova.hc_forbidFunc_office("nim", "loginNim");
cordova.hc_forbidFunc_office("nim", "modifyPassword");
cordova.hc_forbidFunc_office("nim", "modifyUserInfo");
cordova.hc_forbidFunc_office("nim", "refreshPassword");
cordova.hc_forbidFunc_office("nim", "enterTheRecentSessionPage");
cordova.hc_forbidFunc_office("nim", "onTopRecentSession");
cordova.hc_forbidFunc_office("nim", "deleteRecentSession");
cordova.hc_forbidFunc_office("nim", "enterTheChatPage");
cordova.hc_forbidFunc_office("nim", "allRecentSessions");
cordova.hc_forbidFunc_office("nim", "totalUnreadCount");

// todo ImagePicker插件
cordova.hc_forbidFunc_office("imagePicker", "selectImage");


// todo Log插件
cordova.hc_forbidFunc_office("log", "onEvent");
cordova.hc_forbidFunc_office("log", "onLog");
cordova.hc_forbidFunc_office("log", "getLogReport");
cordova.hc_forbidFunc_office("log", "deleteLogReport");
cordova.hc_forbidFunc_office("log", "addUsers");
cordova.hc_forbidFunc_office("log", "activeUsers");

// todo Mail插件
cordova.hc_forbidFunc_office("mail", "sendMail");

// todo Map插件
cordova.hc_forbidFunc_office("map", "inInitializeMap");
cordova.hc_forbidFunc_office("map", "open");
cordova.hc_forbidFunc_office("map", "calculateLineDistance");
cordova.hc_forbidFunc_office("map", "convertCoordinate");
cordova.hc_forbidFunc_office("map", "startNavi");
cordova.hc_forbidFunc_office("map", "startPlanningPath");
cordova.hc_forbidFunc_office("map", "downloadByCityName");
cordova.hc_forbidFunc_office("map", "downloadByCityCode");
cordova.hc_forbidFunc_office("map", "downloadByProvinceName");
cordova.hc_forbidFunc_office("map", "openDownloadView");
cordova.hc_forbidFunc_office("map", "getOfflineMapCityList");
cordova.hc_forbidFunc_office("map", "getOfflineMapProvinceList");

// todo MiniProgram插件
cordova.hc_forbidFunc_office("miniProgram", "getMiniProgramList");
cordova.hc_forbidFunc_office("miniProgram", "openMiniProgram");

// todo Modal插件
navigator.modal = {};
navigator.modal.toast = function (message, time) {
    if (!time) {
        time = 2;
    }
    if (time <= 0) {
        time = 2;
    }
    dd.device.notification.toast({
        icon: '', //icon样式，不同客户端参数不同，请参考参数说明
        text: message, //提示信息
        duration: time, //显示持续时间，单位秒，默认按系统规范[android只有两种(<=2s >2s)]
        delay: 0, //延迟显示，单位秒，默认0
        onSuccess: function (result) {},
        onFail: function (err) {}
    })
}

// todo MQTT插件
cordova.hc_forbidFunc_office("mqtt", "connect");
cordova.hc_forbidFunc_office("mqtt", "subscribe");
cordova.hc_forbidFunc_office("mqtt", "publish");
cordova.hc_forbidFunc_office("mqtt", "unsubscribe");
cordova.hc_forbidFunc_office("mqtt", "disconnect");

// todo NFC插件
cordova.hc_forbidFunc_office("nfc", "addNdefListener", window);
cordova.hc_forbidFunc_office("nfc", "removeNdefListener", window);
cordova.hc_forbidFunc_office("nfc", "addTagDiscoveredListener", window);
cordova.hc_forbidFunc_office("nfc", "removeTagDiscoveredListener", window);
cordova.hc_forbidFunc_office("nfc", "showSettings", window);
cordova.hc_forbidFunc_office("nfc", "write", window);

// todo Panorama插件
cordova.hc_forbidFunc_office("panorama", "showByConfig");

// todo PhotoPicker插件
cordova.hc_forbidFunc_office("photoPicker", "open");

// todo PhotoPreviewer插件
navigator.photoPreviewer = {};
navigator.photoPreviewer.preview = function (ul) {
    dd.biz.util.previewImage({
        urls: [ul], //图片地址列表
        current: ul, //当前显示的图片链接
        onSuccess: function (result) {},
        onFail: function (err) {}
    })
}

// todo QQ插件
navigator.qq = {}
cordova.hc_forbidFunc_office("qq", "getAuth");
navigator.qq.share = navigator.alipay.share;

// todo 扫描插件
navigator.scanner = {};
navigator.scanner.scan = function (successCallback, errorCallback) {
    // alert("scan  now")
    dd.biz.util.scan({
        type: "all", // type 为 all、qrCode、barCode，默认是all。
        onSuccess: function (data) {
            if (successCallback) {
                if (data.text) {
                    successCallback(data.text);
                } else {
                    successCallback(data);
                }
            }
        },
        onFail: function (err) {
            if (errorCallback) {
                errorCallback(err);
            }
        }
    })
};
cordova.hc_forbidFunc_office("scanner", "createBarCode");
cordova.hc_forbidFunc_office("scanner", "create2DCode");

// todo SFVPN(深信服)插件
cordova.hc_forbidFunc_office("sfvpn", "connectTo");
cordova.hc_forbidFunc_office("sfvpn", "disConnect");
cordova.hc_forbidFunc_office("sfvpn", "isOnline");

// todo Sina插件
navigator.sina = {}
cordova.hc_forbidFunc_office("sina", "getAuth");
navigator.sina.share = navigator.alipay.share;

// todo Sign插件
cordova.hc_forbidFunc_office("signature", "sign");

// todo Sms插件
cordova.hc_forbidFunc_office("sms", "send");

// todo SpeechRecognition插件
cordova.hc_forbidFunc_office("speechrecognition", "speechRecognition");
cordova.hc_forbidFunc_office("speechrecognition", "speechStreamRecognition");
cordova.hc_forbidFunc_office("speechrecognition", "stopRecognition");
cordova.hc_forbidFunc_office("speechrecognition", "cancelRecognition");

// todo SpeechSynthesis插件
cordova.hc_forbidFunc_office("speechsynthesis", "speechSynthesis");
cordova.hc_forbidFunc_office("speechsynthesis", "cancelSynthesis");
cordova.hc_forbidFunc_office("speechsynthesis", "pauseSynthesis");
cordova.hc_forbidFunc_office("speechsynthesis", "resumeSynthesis");

// todo Storage插件
cordova.hc_forbidFunc_office("nim", "registerNim");
navigator.storage = {}
navigator.storage.setItem = function (successCallback, errorCallback, options) {
    if (options) {
        var count_my_rec = 0;
        var count_my_rec_suc = 0;
        var count_my_rec_err = 0;
        for (let item in options) {
            count_my_rec = count_my_rec + 1;
        }
        if (count_my_rec === 0) {
            if (errorCallback) {
                errorCallback("no date");
            }
            return;
        }

        function doLastReport() {
            if (count_my_rec === (count_my_rec_suc + count_my_rec_err)) {
                if (count_my_rec_err === 0) {
                    if (successCallback) {
                        successCallback();
                    }
                } else {
                    if (errorCallback) {
                        errorCallback("no save data： " + count_my_rec_err)
                    }
                }
            }
        }

        for (var item_my_rec in options) {
            dd.util.domainStorage.setItem({
                name: item_my_rec, // 存储信息的key值
                value: JSON.stringify(options[item_my_rec]), // 存储信息的Value值
                onSuccess: function (info) {
                    count_my_rec_suc = count_my_rec_suc + 1;
                    doLastReport();
                },
                onFail: function (err) {
                    count_my_rec_err = count_my_rec_err + 1;
                    doLastReport();
                }
            });
        }
    }
}

navigator.storage.getItem = function (resultCallback, key) {
    dd.util.domainStorage.getItem({
        name: key, // 存储信息的key值
        onSuccess: function (info) {
            if (resultCallback) {
                resultCallback(info.value);
            }
        },
        onFail: function (err) {
            alert(JSON.stringify(err));
        }
    });
}
cordova.hc_forbidFunc_office("storage", "getItems");
navigator.storage.removeItem = function (resultCallback, key) {
    dd.util.domainStorage.removeItem({
        name: key, // 存储信息的key值
        onSuccess: function (info) {
            if(resultCallback){
                resultCallback();
            }
        },
        onFail: function (err) {
            if(resultCallback){
                resultCallback(err);
            }
        }
    });
}
cordova.hc_forbidFunc_office("storage", "clear");

// todo UHF插件
cordova.hc_forbidFunc_office("uhf", "readTID");
cordova.hc_forbidFunc_office("uhf", "setTransmissionPower");
cordova.hc_forbidFunc_office("uhf", "getTransmissionPower");

// todo 友盟插件配置
cordova.hc_forbidFunc_office("umeng", "addPushListener");
cordova.hc_forbidFunc_office("umeng", "getDeviceToken");
cordova.hc_forbidFunc_office("umeng", "addTags");
cordova.hc_forbidFunc_office("umeng", "deleteTags");
cordova.hc_forbidFunc_office("umeng", "getTags");
cordova.hc_forbidFunc_office("umeng", "addAlias");
cordova.hc_forbidFunc_office("umeng", "setAlias");
cordova.hc_forbidFunc_office("umeng", "deleteAlias");

// todo Update插件
cordova.hc_forbidFunc_office("update", "download");

// todo VideoPicker插件
cordova.hc_forbidFunc_office("videoPicker", "open");

// todo VideoPreviewer插件
cordova.hc_forbidFunc_office("videoPreviewer", "preview");

// todo webView插件
navigator.webView = {}
navigator.webView.back = function () {
    dd.biz.navigation.goBack({
        onSuccess: function (result) {},
        onFail: function (err) {}
    })
}
navigator.webView.reload = function () {
    let currentLocation = window.location.origin;
    if (currentLocation) {
        dd.biz.navigation.replace({
            url: currentLocation, // 新的页面链接
            onSuccess: function (result) {},
            onFail: function (err) {}
        });
    }
}
navigator.webView.load = function (url, options) {
    if (url) {
        dd.biz.navigation.replace({
            url: url, // 新的页面链接
            onSuccess: function (result) {},
            onFail: function (err) {}
        });
    }
}
navigator.webView.clearHistory = function () {
    if (window.hc_debug) {
        alert("not support action: navigator.webView.clearHistory");
    }
}
navigator.webView.clearCache = function () {
    if (window.hc_debug) {
        alert("not support action: navigator.webView.clearCache");
    }
}

navigator.webView.closeCache = function (disable, cacheTime) {
    if (window.hc_debug) {
        alert("not support action: navigator.webView.closeCache");
    }
}

navigator.webView.close = function () {
    dd.biz.navigation.close({
        onSuccess: function (result) {},
        onFail: function (err) {}
    })
}

// todo Wechat插件
navigator.wechat = {}
cordova.hc_forbidFunc_office("wechat", "getAuth");
navigator.wechat.share = navigator.alipay.share;

// todo 第三方sdk初始化成功后
dd.ready(function () {
    cordova.thirdPartySdkInitSuccess();
});