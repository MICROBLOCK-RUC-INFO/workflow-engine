import Vue from 'vue'
import Vuex from 'vuex'
Vue.use(Vuex)
export const store = new Vuex.Store({
  // 设置属性
  state: {
    isLogin: false
  },

  // 获取属性的状态
  getters: {
    isLogin: state => state.isLogin
  },
  // 设置属性状态
  mutations: {
    userStatus(state, flag) {
      state.isLogin = flag
    }
  },
  // 应用mutations
  actions: {
    //获取登录状态
    userLogin({commit}, flag) {
      commit("userStatus", flag)
    }
  }
})
