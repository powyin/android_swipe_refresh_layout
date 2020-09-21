// todo 实现 navigator 各种转化


// todo 3d模型展示
wx.cordova.hc_forbidFunc_office("glPreviewer", "preview");

// todo AdView插件
wx.cordova.hc_forbidFunc_office("adview", "showBannerAd");
wx.cordova.hc_forbidFunc_office("adview", "showInterteristalAd");
wx.cordova.hc_forbidFunc_office("adview", "showNativeAd");
wx.cordova.hc_forbidFunc_office("adview", "showVideoAd");

// todo Alipay插件
wx.navigator.alipay = {};
wx.cordova.hc_forbidFunc_office("alipay", "isAlipayAppInstalled");
wx.navigator.alipay.share = function (obj, successCallback, errorCallback) {
    wx.showShareMenu({
        withShareTicket: true,
        menus: ['shareAppMessage', 'shareTimeline']
    })
}


// todo App插件
wx.navigator.app = {};
wx.cordova.hc_forbidFunc_office("app", "reset");
wx.cordova.hc_forbidFunc_office("app", "resetUrl");
wx.cordova.hc_forbidFunc_office("app", "exit");

wx.navigator.app.openURL = function (_url, successCallback, errorCallback) {
    wx.navigateTo({
        url: _url
    })
}

// todo AppInfo插件
wx.navigator.appInfo = {}
wx.navigator.appInfo.getInfo = function (successCallback) {
    let infoM = wx.getSystemInfoSync();
    let ret = {};
    ret.id = infoM.platform;
    ret.name = "微信";
    ret.isRelease = true;
    ret.onLineId = "";
    if (successCallback) {
        successCallback(ret);
    }
}

// todo ArcSoftFace插件
wx.cordova.hc_forbidFunc_office("arcSoftFace", "imageModeCheck");
wx.cordova.hc_forbidFunc_office("arcSoftFace", "faceFeatureComparison");
wx.cordova.hc_forbidFunc_office("arcSoftFace", "videoModeCheck");

// todo 录音
wx.navigator.audio = {};
wx.navigator.audio.recording = function (ret) {
    wx.cordova.audioListener = ret;
    wx.navigateTo({
        url: '../../hc-cross-js/wx/aud/index',
    })
}

// todo 录音文件选取
wx.cordova.hc_forbidFunc_office("audioPicker", "open");

// todo 录音浏览
wx.navigator.audioPreviewer = {};
wx.navigator.audioPreviewer.preview = function (url,successCallback, errorCallback) {
    wx.playVoice({
        filePath: url,
        duration:600,
        complete (res) {
            if(successCallback){
                successCallback(res);
            }
         },
        fail:function(res){
            if(errorCallback){
                errorCallback(res);
            }
        }
      })
}

// todo 身份证识别
wx.cordova.hc_forbidFunc_office("bdidcard", "recognizeIDCard");
wx.cordova.hc_forbidFunc_office("bdidcard", "recognizeIDCardQuality");

// todo 营业执照识别
wx.cordova.hc_forbidFunc_office("bdlicensebusiness", "licensebusiness");

// todo 车牌识别
wx.cordova.hc_forbidFunc_office("bdlicenseplate", "licenseplate");

// todo BDRecognition插件
wx.cordova.hc_forbidFunc_office("bdrecognition", "generalBasic");
wx.cordova.hc_forbidFunc_office("bdrecognition", "general");
wx.cordova.hc_forbidFunc_office("bdrecognition", "accurateBasic");
wx.cordova.hc_forbidFunc_office("bdrecognition", "accurate");
wx.cordova.hc_forbidFunc_office("bdrecognition", "generalWebImage");


// todo Bluetooth插件（连接外设）
wx.cordova.hc_forbidFunc_office("bluetooth", "scan");
wx.cordova.hc_forbidFunc_office("bluetooth", "stopScan");
wx.cordova.hc_forbidFunc_office("bluetooth", "connect");
wx.cordova.hc_forbidFunc_office("bluetooth", "disconnect");
wx.cordova.hc_forbidFunc_office("bluetooth", "update");
wx.cordova.hc_forbidFunc_office("bluetooth", "write");
wx.cordova.hc_forbidFunc_office("bluetooth", "read");
wx.cordova.hc_forbidFunc_office("bluetooth", "writeWithoutResponse");
wx.cordova.hc_forbidFunc_office("bluetooth", "startNotification");
wx.cordova.hc_forbidFunc_office("bluetooth", "stopNotification");
wx.cordova.hc_forbidFunc_office("bluetooth", "isConnected");

// todo Camera插件
wx.navigator.camera = {}
wx.navigator.camera.takePhoto = function (succ, error) {
    wx.chooseImage({
        count: 1,
        sizeType: ['original', 'compressed'],
        sourceType: ['camera'],
        success(res) {
            if (succ) {
                var timestamp = Date.parse(new Date());
                let ret = {
                    fullPath: res.tempFiles[0].path,
                    thumbLocalURL: res.tempFiles[0].path,
                    lastModifiedDate: timestamp,
                    size: res.tempFiles[0].size,
                    localURL: res.tempFiles[0].path,
                    type: "image/jpeg",
                };
                succ(ret);
            }
        },
        fail(res) {
            if (error) {
                error(res);
            }
        }
    })
}
wx.navigator.camera.shootVideo = function (succ, error) {
    wx.chooseVideo({
        sourceType: ['camera'],
        maxDuration: 60,
        camera: 'back',
        success(res) {
            if (succ) {
                var timestamp = Date.parse(new Date());
                let ret = {
                    fullPath: res.tempFilePath,
                    thumbLocalURL: "",
                    lastModifiedDate: timestamp,
                    size: res.size,
                    localURL: res.tempFilePath,
                    type: "audio/mp4",
                };
                succ(ret);
            }
        },
        fail(res) {
            if (error) {
                error(res);
            }
        }
      })
}
  


// todo Connection插件
let sysInfo = wx.getSystemInfoSync();
wx.Connection = {}
wx.Connection.UNKNOWN  = 'Unknown connection';
wx.Connection.WIFI    = 'WiFi connection';
wx.Connection.CELL_2G  = 'Cell 2G connection';
wx.Connection.CELL_3G  = 'Cell 3G connection';
wx.Connection.CELL_4G  = 'Cell 4G connection';
wx.Connection.NONE     = 'No network connection';
wx.navigator.connection={};
if(sysInfo.wifiEnabled){
    wx.navigator.connection.type = wx.Connection.WIFI
}else{
    wx.navigator.connection.type = wx.Connection.UNKNOWN
}


// todo 获取崩溃日志
wx.cordova.hc_forbidFunc_office("crash", "getCrashReport");
wx.cordova.hc_forbidFunc_office("crash", "deleteCrashReport");


// todo Database插件
wx.cordova.hc_forbidFunc_office("db", "execSQL");
wx.cordova.hc_forbidFunc_office("db", "rawQuery");
wx.cordova.hc_forbidFunc_office("db", "loadFromJson");


// todo Device插件   需要补充
wx.device = {};
wx.device.model = sysInfo.model;
wx.device.platform = sysInfo.platform;
wx.device.version = sysInfo.system;
wx.device.uuid = "";
if (sysInfo.platform=='ios'){
    wx.device.manufacturer = 'Apple';
}else {
    wx.device.manufacturer = sysInfo.model;
}
wx.device.serial = "";
wx.device.macAddress = "";

// todo Dialing插件     需要补充 健全
wx.navigator.dialing = {};
wx.navigator.dialing.dial = function (success_, failure_, phoneNumber_, isDialNow) {
    wx.makePhoneCall({
        success: function () {
            if (success_) {
                success_();
            }
        },
        fail: function () {
            if (failure_) {
                failure_();
            }
        },
        phoneNumber: phoneNumber_
    })
}

// todo 警告对话框
wx.navigator.notification = {};
wx.navigator.notification.alert = function (message, callback, title, buttonLabel) {
    let mod = {};
    mod.title = title;
    mod.content = message;
    if (buttonLabel) {
        mod.confirmText = buttonLabel;
    }
    mod.success = function (res) {
        if (callback) {
            if (res.confirm) {
                callback(0);
            }
        }
    }
    wx.showModal(mod);
}
wx.navigator.notification.confirm = function (message, callback, title, buttonLabels) {
    let mod = {};
    mod.title = title;
    mod.content = message;
    if (buttonLabels && buttonLabels[0]) {
        mod.confirmText = buttonLabels[0];
    }
    if (buttonLabels && buttonLabels[1]) {
        mod.cancelText = buttonLabels[1];
    }
    mod.success = function (res) {
        if (callback) {
            if (res.confirm) {
                callback(0);
            } else {
                callback(1);
            }
        }
    }
    wx.showModal(mod);
}
wx.cordova.hc_forbidFunc_office("notification", "prompt");
wx.navigator.notification.beep = function (time) {
    wx.vibrateShort({})
}

// todo DingTalk插件
wx.navigator.ddshare = {}
wx.cordova.hc_forbidFunc_office("ddshare", "getAuth");
wx.navigator.ddshare.share = wx.navigator.alipay.share;

// todo 文件选取
wx.navigator.documentPicker = {};
wx.navigator.documentPicker.open = function (suc, error) {
    let info = {};
    info.count = 8;
    info.type = "file";
    info.success = function (res) {
        if (suc) {
            var timestamp = Date.parse(new Date());
            let ret = {
                name:res.tempFiles[0].name,
                localURL: res.tempFiles[0].path,
                fullPath: res.tempFiles[0].path,
                thumbLocalURL: res.tempFiles[0].path,
                lastModifiedDate: timestamp,
                size: res.tempFiles[0].size,
                type: res.tempFiles[0].type,
            };
            suc(ret);
        }
    }
    info.fail = function (res) {
        if (error) {
            error(res);
        }
    }
    wx.chooseMessageFile(info);
}





// todo DocumentPreviewer插件
wx.navigator.documentPreviewer = {};
wx.navigator.documentPreviewer.preview = function (url, successCallback, errorCallback) {
    wx.openDocument({
        filePath: url,
        success: function (res) {
            if (successCallback) {
                successCallback(res);
            }
        },
        fail: function (res) {
            if (errorCallback) {
                errorCallback(res);
            }
        }
    })
}

// todo 图片，视频，录音，文件选取
wx.navigator.filePicker = {};
wx.navigator.filePicker.open = function (suc, error, options) {
    let info = {};
    info.count = 8;
    if (options) {
        if (options.image) {
            info.type = "image";
        } else if (options.video) {
            info.type = "video"
        } else {
            info.type = "all";
        }
    } else {
        info.type = "all";
    }
    info.success = function (res) {
        if (suc) {
            var timestamp = Date.parse(new Date());
            let ret = {
                name:res.tempFiles[0].name,
                fullPath: res.tempFiles[0].path,
                thumbLocalURL: res.tempFiles[0].path,
                lastModifiedDate: timestamp,
                size: res.tempFiles[0].size,
                localURL: res.tempFiles[0].path,
                type: res.tempFiles[0].type,
            };
            suc(ret);
        }
    }
    info.fail = function (res) {
        if (error) {
            error(res);
        }
    }
    wx.chooseMessageFile(info);
}




// todo 文件传输
function FileTransfer() {
    this.download = function (downloadUrl, successCallback, errorCallback, trust, option) {
        let downInfo = {};
        downInfo.url = downloadUrl;
        if (option && option.headers) {
            downInfo.header = option.headers;
        }
        downInfo.success = function (res) {
            if (successCallback) {
                let path = res.filePath;
                if (!path) {
                    path = res.tempFilePath;
                }
                let ret = {};
                ret.isFile = true;
                ret.isDirectory = false;
                ret.name = path;
                ret.fullPath = path;
                ret.filesystem = "<FileSystem: cache>";
                ret.nativeURL = path;
                successCallback(ret);
            }
        }
        downInfo.fail = function (res) {
            if (errorCallback) {
                errorCallback(res);
            }
        }
        wx.downloadFile(downInfo);
    }
    this.upload = function (fileURL, server, successCallback, errorCallback, options, trustAllHosts) {
        let upinfo = {};
        upinfo.filePath = fileURL;
        upinfo.url = server;
        upinfo.name = "file";
        if (options && options.fileKey) {
            upinfo.name = options.fileKey;
        }
        if (options && options.headers) {
            upinfo.header = options.headers;
        }
        if (options && options.params) {
            upinfo.formData = options.params;
        }
        upinfo.success = function (res) {
            if (successCallback) {
                successCallback(res);
            }
        }
        upinfo.fail = function (res) {
            if (errorCallback) {
                errorCallback(res);
            }
        }
        wx.uploadFile(upinfo);
    }
    this.abort = function () {}
}

wx.FileTransfer = FileTransfer;
wx.resolveLocalFileSystemURL = function () {
    alert("resolveLocalFileSystemURL is not allow action in android system");
}

// todo 指纹验证
wx.navigator.fingerprint = {};
wx.cordova.hc_forbidFunc_office("fingerprint", "isAvailable");
wx.navigator.fingerprint.isAvailable = function (callback) {
    if (!callback) {
        return;
    }
    wx.checkIsSupportSoterAuthentication({
        success(res) {
            for (let item in res.supportMode) {
                if ("fingerPrint" == res.supportMode[item]) {
                    callback(wx.Fingerprint.Result.SUPPORT_FINGERPRINT);
                    return;
                }
            }
            callback(wx.Fingerprint.Result.NO_SUPPORT_FINGERPRINT)
        },
        fail(res) {
            callback(wx.Fingerprint.Result.NO_SUPPORT_FINGERPRINT);
        }
    })
}
wx.cordova.hc_forbidFunc_office("fingerprint", "verifyFingerprint");


// todo Geolocation插件
wx.navigator.geolocation = {};
wx.navigator.geolocation.getCurrentPosition = function (successCallback, errorCallback, options) {
    wx.getLocation({
        type: 'wgs84',
        success(result) {
            if (successCallback) {
                let ret = {};
                ret.coords = {};
                ret.coords.latitude = result.latitude;
                ret.coords.longitude = result.longitude;
                successCallback(ret);
            }
        },
        fail(err) {
            if (errorCallback) {
                errorCallback("vist https://developers.weixin.qq.com/miniprogram/dev/reference/configuration/app.html#permission");
            }
        }
    });
    return "random_time";
}
wx.navigator.geolocation.watchPosition = wx.navigator.geolocation.getCurrentPosition;
wx.navigator.geolocation.clearWatch = function () {}

// todo Hik插件
wx.cordova.hc_forbidFunc_office("hik", "login");
wx.cordova.hc_forbidFunc_office("hik", "previewVideo");
wx.cordova.hc_forbidFunc_office("hik", "queryRegionResources");
wx.cordova.hc_forbidFunc_office("hik", "queryControlUnitResources");

// todo IM插件
wx.cordova.hc_forbidFunc_office("nim", "registerNim");
wx.cordova.hc_forbidFunc_office("nim", "loginNim");
wx.cordova.hc_forbidFunc_office("nim", "modifyPassword");
wx.cordova.hc_forbidFunc_office("nim", "modifyUserInfo");
wx.cordova.hc_forbidFunc_office("nim", "refreshPassword");
wx.cordova.hc_forbidFunc_office("nim", "enterTheRecentSessionPage");
wx.cordova.hc_forbidFunc_office("nim", "onTopRecentSession");
wx.cordova.hc_forbidFunc_office("nim", "deleteRecentSession");
wx.cordova.hc_forbidFunc_office("nim", "enterTheChatPage");
wx.cordova.hc_forbidFunc_office("nim", "allRecentSessions");
wx.cordova.hc_forbidFunc_office("nim", "totalUnreadCount");

// todo ImagePicker插件
wx.cordova.navigator.imagePicker = {};
wx.cordova.navigator.imagePicker.selectImage = function (succ, error) {
    wx.chooseImage({
        count: 1,
        sizeType: ['original', 'compressed'],
        sourceType: ['album', 'camera'],
        success(res) {
            if (succ) {
                let ret = {
                    fullPath: res.tempFiles[0].path,
                    thumbLocalURL: res.tempFiles[0].path,
                    lastModifiedDate: 0,
                    size: res.tempFiles[0].size,
                    localURL: res.tempFiles[0].path,
                    type: "image/jpeg",
                };
                succ(ret);
            }
        },
        fail(res) {
            if (error) {
                error(res);
            }
        }
    })
}

// todo Log插件
wx.cordova.hc_forbidFunc_office("log", "onEvent");
wx.cordova.hc_forbidFunc_office("log", "onLog");
wx.cordova.hc_forbidFunc_office("log", "getLogReport");
wx.cordova.hc_forbidFunc_office("log", "deleteLogReport");
wx.cordova.hc_forbidFunc_office("log", "addUsers");
wx.cordova.hc_forbidFunc_office("log", "activeUsers");

// todo Mail插件
wx.cordova.hc_forbidFunc_office("mail", "sendMail");
// wx.cordova.mail = {};
// wx.cordova.mail.sendMail = function () {
// };

// todo Map插件
wx.cordova.hc_forbidFunc_office("map", "inInitializeMap");
wx.cordova.hc_forbidFunc_office("map", "open");
wx.cordova.hc_forbidFunc_office("map", "calculateLineDistance");
wx.cordova.hc_forbidFunc_office("map", "convertCoordinate");
wx.cordova.hc_forbidFunc_office("map", "startNavi");
wx.cordova.hc_forbidFunc_office("map", "startPlanningPath");
wx.cordova.hc_forbidFunc_office("map", "downloadByCityName");
wx.cordova.hc_forbidFunc_office("map", "downloadByCityCode");
wx.cordova.hc_forbidFunc_office("map", "downloadByProvinceName");
wx.cordova.hc_forbidFunc_office("map", "openDownloadView");
wx.cordova.hc_forbidFunc_office("map", "getOfflineMapCityList");
wx.cordova.hc_forbidFunc_office("map", "getOfflineMapProvinceList");

// todo MiniProgram插件
wx.cordova.hc_forbidFunc_office("miniProgram", "getMiniProgramList");
wx.cordova.hc_forbidFunc_office("miniProgram", "openMiniProgram");

// todo Modal插件
wx.navigator.modal = {};
wx.navigator.modal.toast = function (message, time) {
    wx.showToast({
        title: message,
        icon: 'none',
        duration: 2000
    })
}

// todo MQTT插件
wx.cordova.hc_forbidFunc_office("mqtt", "connect");
wx.cordova.hc_forbidFunc_office("mqtt", "subscribe");
wx.cordova.hc_forbidFunc_office("mqtt", "publish");
wx.cordova.hc_forbidFunc_office("mqtt", "unsubscribe");
wx.cordova.hc_forbidFunc_office("mqtt", "disconnect");

// todo NFC插件
wx.cordova.hc_forbidFunc_office("nfc", "addNdefListener", window);
wx.cordova.hc_forbidFunc_office("nfc", "removeNdefListener", window);
wx.cordova.hc_forbidFunc_office("nfc", "addTagDiscoveredListener", window);
wx.cordova.hc_forbidFunc_office("nfc", "removeTagDiscoveredListener", window);
wx.cordova.hc_forbidFunc_office("nfc", "showSettings", window);
wx.cordova.hc_forbidFunc_office("nfc", "write", window);

// todo Panorama插件
wx.cordova.hc_forbidFunc_office("panorama", "showByConfig");

// todo PhotoPicker插件
wx.cordova.hc_forbidFunc_office("photoPicker", "open");
wx.navigator.photoPicker = {};
wx.navigator.photoPicker.open = function (succ) {
    wx.chooseImage({
        count: 1,
        sizeType: ['original', 'compressed'],
        sourceType: ['album', 'camera'],
        success(res) {
            if (succ) {
                let ret = {
                    fullPath: res.tempFiles[0].path,
                    thumbLocalURL: res.tempFiles[0].path,
                    lastModifiedDate: 0,
                    size: res.tempFiles[0].size,
                    localURL: res.tempFiles[0].path,
                    type: "image/jpeg",
                };
                succ(ret);
            }
        }
    })
}

// todo PhotoPreviewer插件
wx.navigator.photoPreviewer = {};
wx.navigator.photoPreviewer.preview = function (url) {
    wx.previewImage({
        current: url,
        urls: [url]
    })
}

// todo QQ插件
wx.navigator.qq = {}
wx.cordova.hc_forbidFunc_office("qq", "getAuth");
wx.navigator.qq.share = wx.navigator.alipay.share;

// todo 扫描插件
wx.navigator.scanner = {};
wx.navigator.scanner.scan = function (successCallback, errorCallback) {
    wx.scanCode({
        success: function (res) {
            if (successCallback) {
                successCallback(res.result);
            }
        },
        fail: function (err) {
            if (errorCallback) {
                errorCallback(err);
            }
        }
    });
};
wx.cordova.hc_forbidFunc_office("scanner", "createBarCode");
wx.cordova.hc_forbidFunc_office("scanner", "create2DCode");

// todo SFVPN(深信服)插件
wx.cordova.hc_forbidFunc_office("sfvpn", "connectTo");
wx.cordova.hc_forbidFunc_office("sfvpn", "disConnect");
wx.cordova.hc_forbidFunc_office("sfvpn", "isOnline");

// todo Sina插件
wx.navigator.sina = {}
wx.cordova.hc_forbidFunc_office("sina", "getAuth");
wx.navigator.sina.share = wx.navigator.alipay.share;

// todo Sign插件
wx.cordova.hc_forbidFunc_office("signature", "sign");

// todo Sms插件
wx.cordova.hc_forbidFunc_office("sms", "send");

// todo SpeechRecognition插件
wx.cordova.hc_forbidFunc_office("speechrecognition", "speechRecognition");
wx.cordova.hc_forbidFunc_office("speechrecognition", "speechStreamRecognition");
wx.cordova.hc_forbidFunc_office("speechrecognition", "stopRecognition");
wx.cordova.hc_forbidFunc_office("speechrecognition", "cancelRecognition");

// todo SpeechSynthesis插件
wx.cordova.hc_forbidFunc_office("speechsynthesis", "speechSynthesis");
wx.cordova.hc_forbidFunc_office("speechsynthesis", "cancelSynthesis");
wx.cordova.hc_forbidFunc_office("speechsynthesis", "pauseSynthesis");
wx.cordova.hc_forbidFunc_office("speechsynthesis", "resumeSynthesis");

// todo Storage插件
wx.cordova.hc_forbidFunc_office("nim", "registerNim");
wx.navigator.storage = {}
wx.navigator.storage.setItem = function (successCallback, errorCallback, options) {
    if (options) {
        var count_my_rec = 0;
        for (let item in options) {
            count_my_rec = count_my_rec + 1;
        }
        if (count_my_rec === 0) {
            if (errorCallback) {
                errorCallback("no date");
            }
            return;
        }
        for (var item_my_rec in options) {
            wx.setStorage({
                key: item_my_rec,
                data: options[item_my_rec]
            })
        }
        if (successCallback) {
            successCallback();
        }
    }
}

wx.navigator.storage.getItem = function (resultCallback, keyItem) {
    wx.getStorage({
        key: keyItem,
        success: function (res) {
            if (resultCallback) {
                resultCallback(res.data);
            }
        },
        fail: function () {
            if (resultCallback) {
                resultCallback(null);
            }
        }
    });
}
wx.cordova.hc_forbidFunc_office("storage", "getItems");
wx.navigator.storage.removeItem = function (resultCallback, keyItem) {
    wx.removeStorage({
        key: keyItem,
        success(res) {
            if (resultCallback) {
                resultCallback();
            }
        },
        fail(res) {
            if (resultCallback) {
                resultCallback(res);
            }
        }
    })
};
wx.navigator.storage.clear = function (resultCallback) {
    wx.clearStorage();
};

// todo UHF插件
wx.cordova.hc_forbidFunc_office("uhf", "readTID");
wx.cordova.hc_forbidFunc_office("uhf", "setTransmissionPower");
wx.cordova.hc_forbidFunc_office("uhf", "getTransmissionPower");

// todo 友盟插件配置
wx.cordova.hc_forbidFunc_office("umeng", "addPushListener");
wx.cordova.hc_forbidFunc_office("umeng", "getDeviceToken");
wx.cordova.hc_forbidFunc_office("umeng", "addTags");
wx.cordova.hc_forbidFunc_office("umeng", "deleteTags");
wx.cordova.hc_forbidFunc_office("umeng", "getTags");
wx.cordova.hc_forbidFunc_office("umeng", "addAlias");
wx.cordova.hc_forbidFunc_office("umeng", "setAlias");
wx.cordova.hc_forbidFunc_office("umeng", "deleteAlias");

// todo Update插件
wx.cordova.hc_forbidFunc_office("update", "download");

// todo VideoPicker插件
wx.navigator.videoPicker = {};
wx.navigator.videoPicker.open = function (suc, error) {
    console.error("i am here you are")
    wx.chooseVideo({
        sourceType: ['album', 'camera'],
        maxDuration: 60,
        camera: 'back',
        success(res) {
            if (suc) {
                let ret = {};
                ret.fullPath = res.tempFilePath;
                ret.lastModifiedDate = "";
                ret.size = res.size;
                ret.type = "video/mp4";
                ret.name = res.tempFilePath;
                suc(ret);
            }
        },
        fail: function (res) {
            if (error) {
                error(res);
            }
        }
    })
}

// todo VideoPreviewer插件
wx.navigator.videoPreviewer = {};
wx.navigator.videoPreviewer.preview = function (srcUrl) {
    if (srcUrl) {
        srcUrl = encodeURI(srcUrl);
        wx.navigateTo({
            url: '../../hc-cross-js/wx/videoplay/videoplay?cloudVideoUrl=' + srcUrl,
        })
    }
}



// todo webView插件
wx.navigator.webView = {}
wx.navigator.webView.back = function () {
    wx.navigateBack({
        delta: 1
    });
}
wx.cordova.hc_forbidFunc_office("webView", "reload");
wx.navigator.webView.load = function (url, options) {
    if (url) {
        wx.navigateTo({
            url: url,
        })
    }
}
wx.cordova.hc_forbidFunc_office("webView", "clearHistory");
wx.navigator.webView.clearCache = function () {
    wx.clearStorage()
}
wx.cordova.hc_forbidFunc_office("webView", "closeCache");
wx.cordova.hc_forbidFunc_office("webView", "close");

// todo Wechat插件
wx.navigator.wechat = {}
wx.cordova.hc_forbidFunc_office("wechat", "getAuth");
wx.navigator.wechat.share = wx.navigator.alipay.share;

// todo 第三方sdk初始化成功后
// dd.ready(function () {
//     cordova.thirdPartySdkInitSuccess();
// });



wx.binObj(wx.cordova.wxObj, wx.cordova);
wx.binObj(wx.cordova.wxObj, wx.cordova.document);
wx.binObj(wx.cordova.wxObj, wx.cordova.navigator);
wx.cordova.thirdPartySdkInitSuccess()