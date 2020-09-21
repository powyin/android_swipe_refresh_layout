//index.js

const recorderManager = wx.getRecorderManager()
const innerAudioContext = wx.createInnerAudioContext()
const options = {
  duration: 60000, //指定录音的时长，单位 ms
  sampleRate: 44100, //采样率
  numberOfChannels: 1, //录音通道数
  encodeBitRate: 192000, //编码码率
  format: 'mp3', //音频格式，有效值 aac/mp3
  frameSize: 50, //指定帧大小，单位 KB
}

Page({

  data: {
    j: 1, //帧动画初始图片
    isSpeaking: false, //是否正在说话
    isPlaying: false, //是否正在播放
    voicesPath: "", //音频
    tempFilePath: "",
    audioSize: 0,
  },

  onLoad: function() {
    console.log(recorderManager)
  },


  //开始录音的时候
  startRecord: function() {
    //开始录音
    var _this = this;
    recorderManager.start(options);
    recorderManager.onStart(() => {
      console.log('recorder start')
      _this.setData({
        isSpeaking: true
      })
      speaking.call(_this);
    });
    //错误回调
    recorderManager.onError((res) => {
      console.log(res);
      wx.showToast({
        title: '录音异常',
        icon: "none"
      })

      if(wx.cordova.audioListenerErr){
        wx.cordova.audioListenerErr(ret)
      }

    })
  },


  //停止录音
  stopRecord: function() {
    let that = this;
    this.setData({
      isSpeaking: false,
    })
    clearInterval(this.timer)
    recorderManager.stop();
    recorderManager.onStop((res) => {
      // console.log('停止录音', res)
      // wx.showToast({
      //   title: '恭喜!录音成功',
      //   icon: 'success',
      //   duration: 1000
      // })
      //将音频大小B转为KB
      var size = (res.fileSize / 1024).toFixed(2);
      that.setData({
        tempFilePath: res.tempFilePath,
        audioSize: size
      })

      let ret = {}
      ret.fullPath = res.tempFilePath;
      ret.lastModifiedDate = 0;
      ret.localURL =  res.tempFilePath;
      ret.name = res.tempFilePath;
      ret.size=res.fileSize;
      ret.type="type/mp3"
      if(wx.cordova.audioListener){
        wx.cordova.audioListener(ret)
      }
    })
  },

  /**
   * 播放音频
   */
  playAudio: function() {
    let that = this;
    if (this.data.tempFilePath.length > 0) {
      innerAudioContext.autoplay = true
      innerAudioContext.src = that.data.tempFilePath,
        innerAudioContext.onPlay(() => {
          console.log('开始播放')
          that.setData({
            isPlaying: true
          })
        })

      innerAudioContext.onEnded(() => {
        console.log('自然播放结束');
        that.setData({
          isPlaying: false
        })
      });

      innerAudioContext.onError((res) => {
        console.log(res.errMsg)
        console.log(res.errCode)
        that.setData({
          isPlaying: false
        })
        wx.showToast({
          title: '播放错误',
          icon: "none"
        })
      })
    } else {
      wx.showToast({
        title: '请先录音',
        icon: "none"
      })
    }
  },


  /**
   * 长按监听
   */
  bindlongtap: function() {
    console.log('longTap....')
  },

  /**
   * 按下监听
   */
  bindTouchDown: function() {
    console.log("手指按下了...")
    this.startRecord();
  },

  /**
   * 松开监听
   */
  bindTouchUp: function() {
    console.log("手指松开了...")
    this.stopRecord()
  },



  //点击开始说话
  startSpeak: function() {
    var _this = this;
    if (!this.data.isSpeaking) {
      speaking.call(this);
      this.setData({
        isSpeaking: true
      })
    } else {
      //去除帧动画循环
      clearInterval(this.timer)
      this.setData({
        isSpeaking: false,
        j: 1
      })
    }
  },
})


//麦克风帧动画
function speaking() {
  var _this = this;
  //话筒帧动画
  var i = 1;
  this.timer = setInterval(function() {
    i++;
    i = i % 5;
    _this.setData({
      j: i
    })
  }, 200);
}