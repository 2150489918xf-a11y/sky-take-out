Page({
  data: {
    avatarUrl: 'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132',
    nickName: ''
  },

  onChooseAvatar: function(e) {
    if (e.detail.avatarUrl) {
      this.setData({ avatarUrl: e.detail.avatarUrl });
    }
  },

  onNicknameInput: function(e) {
    this.setData({ nickName: e.detail.value });
  },

  onNicknameBlur: function(e) {
    this.setData({ nickName: e.detail.value });
  },

  _getBaseUrl: function() {
    // 与 vendor.js 中的 baseUrl 保持一致
    return 'http://192.168.2.37:8080';
  },

  onConfirm: function() {
    var that = this;
    var app = getApp();
    var store = app.$vm.$store;

    // 保存到store
    store.commit('setBaseUserInfo', {
      avatarUrl: that.data.avatarUrl,
      nickName: that.data.nickName || '微信用户'
    });

    // 调用后端保存用户信息
    var token = store.state.token;
    if (token) {
      wx.request({
        url: that._getBaseUrl() + '/user/user/update',
        method: 'PUT',
        header: {
          'authentication': token,
          'Content-Type': 'application/json'
        },
        data: {
          name: that.data.nickName || '微信用户',
          avatar: that.data.avatarUrl
        },
        success: function(res) {
          console.log('用户信息保存成功', res);
        },
        fail: function(err) {
          console.log('用户信息保存失败', err);
        }
      });
    }

    // 跳转回首页
    wx.reLaunch({ url: '/pages/index/index' });
  },

  onSkip: function() {
    var app = getApp();
    var store = app.$vm.$store;
    store.commit('setBaseUserInfo', {
      avatarUrl: 'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132',
      nickName: '微信用户'
    });
    wx.reLaunch({ url: '/pages/index/index' });
  }
});
