import $ from 'jquery'
import properties2Json from '../../static/properties2Json/properties2Json'
export function delCookie (name) {
  var exp = new Date()
  exp.setTime(exp.getTime() - 1)
  var cval = this.getCookie(name)
  if (cval != null) {
    document.cookie = name + '=' + cval + ';expires=' + exp.toGMTString()
  }
}
export function getCookie (name) {
  var arr, reg = new RegExp('(^| )' + name + '=([^;]*)(;|$)')
  if (arr = document.cookie.match(reg)) {
    return decodeURI(arr[2])
  } else {
    return null
  }
}
export function setCookie (name, value, expiredays) {
  var exdate = new Date()
  exdate.setDate(exdate.getDate() + expiredays)
  document.cookie = name + '=' + encodeURI(value) + ((expiredays == null) ? '' : ';expires=' + exdate.toGMTString())
}

