Page({
  data: {
    inputValue: '',
    toggle: true,
    videoSrc: '',
    editingDanmuObj: {
      content: '',
      color: '#fff',
    },
    playingIntTime: 0,
    danmuListObj: {},
  },
  onLoad: function (option) {
    this.setData({
      videoSrc: decodeURI(option.cloudVideoUrl),
    });
    this.videoContext = wx.createVideoContext('myVideo');
  },
  onUnload() {
  },
  onPlay() {
  },

  onPlayTimeUpdate: function (event) {
    var that = this;
    var playingIntTime = Math.floor(event.detail.currentTime).toString();
    if (playingIntTime > that.data.playingIntTime) {
      this.setData({
        'playingIntTime': playingIntTime
      });
      if (playingIntTime in this.data.danmuListObj) {
        console.log(`101-101 playingIntTime: ${playingIntTime} in this.data.danmuListObj`);
        console.log('102-102 that.data.danmuListObj[playingIntTime] : ', that.data.danmuListObj[playingIntTime]);
        that.barrage.addData(that.data.danmuListObj[playingIntTime]);
      }
    }
  },

  onHide() {
  },

  onEnded() {
    this.setData({
      playingIntTime: 0,
    });
  },
  onPause() {
  },
  videoErrorCallback: function (event) {
    console.log('In videoErrorCallback, 视频错误信息: event.detail.errMsg');
    console.log(e.detail.errMsg);
  }
})