
var app = getApp(),
rm = wx.getRecorderManager();
//录音停止时调用
rm.onStop(function(e) {
  var a = this;
  wx.showLoading({
    title: "正在识别..."
  });

  //上传逻辑
  var n = {
    url: app.globalData.url + "upload",
    filePath: e.tempFilePath,
    name: "music",
    header: {
      "Content-Type": "application/json"
    },
    success: function (res) {
      
     }
    };
    // wx.uploadFile(n);
}),
Page({

  /**
   * 页面的初始数据
   */
  data: {
    hasRecord: false,
    isDot: "block",
    isTouchStart: false,
    isTouchEnd: false,
    value: '100',
    touchStart:0,
    touchEnd:0,
    vd:''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {
    var a = this;
    wx.authorize({
      scope: "scope.record",
      success: function() {
        console.log("录音授权成功");
      },
      fail: function() {
        console.log("录音授权失败");
      }
    }), a.onShow()

  },
  // 点击录音按钮
  onRecordClick: function () {
    wx.getSetting({
      success: function (t) {
        console.log(t.authSetting), t.authSetting["scope.record"] ? console.log("已授权录音") : (console.log("未授权录音"),
          wx.openSetting({
            success: function (t) {
              console.log(t.authSetting);
            }
          }));
      }
    });
  },
  /**
   * 长按录音开始
   */
  recordStart: function(e) {
    var n = this;
    rm.start({
      format: "mp3",
      sampleRate: 32e3,
      encodeBitRate: 192e3
    }), n.setData({
      touchStart: e.timeStamp,
      isTouchStart: true,
      isTouchEnd: false,
      showPg: true,
    })
    var a = 15, o = 10;
    this.timer = setInterval(function () {
      n.setData({
        value: n.data.value - 100 / 1500
      }), (o += 10) >= 1e3 && o % 1e3 == 0 && (a-- , console.log(a), a <= 0 && (rm.stop(),
        clearInterval(n.timer), n.animation2.scale(1, 1).step(), n.setData({
          animationData: n.animation2.export(),
        showPg: false,
        })));
    }, 10);
  },
  /**
   * 长按录音结束
   */
  recordTerm: function(e) {
    rm.stop(), this.setData({
      isTouchEnd: true,
      isTouchStart: false,
      touchEnd: e.timeStamp,
      showPg: false,
      value: 100
    }), clearInterval(this.timer);
  }
})