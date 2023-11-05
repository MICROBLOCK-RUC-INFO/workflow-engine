'use strict'
var lodash = require('lodash')

var splitKeysBy = function (obj, splitBy) {
  var keys, parent, result = {}
  lodash.forEach(obj, function (val, key) {
    console.log("splitKeysBy")
    console.log(val)
    console.log(key)
    keys = key.split(splitBy)
    parent = result
    keys.forEach(function (k, i) {
      if (i === keys.length - 1) {
        parent[k] = val
      } else {
        parent = parent[k] = parent[k] || {}
      }
    })
  })
  return result
}

module.exports = function (text) {
  return splitKeysBy(text, '.')
}
