// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import axios from 'axios'
import vueAxios from 'vue-axios'
import vueCookie from 'vue-cookies'
import App from './App'
import router from './router'

import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-fileinput/css/fileinput.css'
import 'font-awesome/css/font-awesome.min.css'
import 'bootstrap-fileinput/themes/explorer-fas/theme.css'
// import '../static/bootstrap-select-1.13.9/dist/css/bootstrap-select.css'
import 'bootstrap-select/dist/css/bootstrap-select.min.css'
import 'bootstrap-table/dist/bootstrap-table.min.css'
// import 'bootstrap-fileinput/themes/explorer-fa/theme.css'
import 'jquery/dist/jquery.min'
import 'bootstrap/dist/js/bootstrap.min'
// import '../static/bootstrap-select-1.13.9/dist/js/bootstrap-select'
import 'bootstrap-select/js/bootstrap-select'
import 'bootstrap-fileinput/js/plugins/piexif.min'
import 'bootstrap-fileinput/js/plugins/sortable.min'
import 'bootstrap-fileinput/js/fileinput.min'
import 'bootstrap-fileinput/js/locales/zh'
import 'bootstrap-fileinput/themes/fas/theme.min'
// import 'bootstrap-fileinput/themes/fa/theme.min'
import 'bootstrap-fileinput/themes/explorer-fas/theme.min'
// import 'bootstrap-fileinput/themes/explorer-fa/theme.min'


import 'bootstrap-table/dist/bootstrap-table.min'
import 'bootstrap-table/dist/locale/bootstrap-table-zh-CN'

import 'bpmn-js/dist/assets/diagram-js.css' // 左边工具栏以及编辑节点的样式
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'
import 'bpmn-js-properties-panel/dist/assets/bpmn-js-properties-panel.css' // 右边工具栏样式

import 'jquery-editable-select/dist/jquery-editable-select.css'
/*
import 'vx-easyui/dist/themes/default/easyui.css'
import 'vx-easyui/dist/themes/icon.css'
import 'vx-easyui/dist/themes/vue.css'
import EasyUI from 'vx-easyui'
Vue.use(EasyUI)
*/
Vue.use(vueCookie)
Vue.use(vueAxios, axios)
Vue.config.productionTip = false
/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})
