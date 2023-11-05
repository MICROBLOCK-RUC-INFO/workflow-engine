import Vue from 'vue'
import Router from 'vue-router'
import Login from '@/components/Login'
import Main1 from '@/components/Main1'
Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Login',
      component: Login,
      meta: {
        isLogin: false
      }
    },
    {
      path: '/main1',
      name: 'main1',
      component: Main1
    }
  ]
})
