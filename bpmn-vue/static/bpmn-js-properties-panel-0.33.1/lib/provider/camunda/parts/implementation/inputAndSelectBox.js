'use strict';

var assign = require('lodash/assign'),
  find = require('lodash/find'),
  forEach = require('lodash/forEach');
var $ = require('jquery');
var domQuery = require('min-dom').query;
var utils = require('../../../../Utils'),
  escapeHTML = utils.escapeHTML;

var properties2Json = require('../../../../../../properties2Json/properties2Json');

/**
 * The combo box is a special implementation of the select entry and adds the option 'custom' to the
 * select box. If 'custom' is selected, an additional text input field is shown which allows to define
 * a custom value.
 *
 * @param  {Object} options
 * @param  {string} options.id
 * @param  {string} options.label
 * @param  {Array<Object>} options.selectOptions list of name/value pairs
 * @param  {string} options.modelProperty
 * @param  {function} options.get
 * @param  {function} options.set
 * @param  {string} [options.customValue] custom select option value (default: 'custom')
 * @param  {string} [options.customName] custom select option name visible in the select box (default: 'custom')
 *
 * @return {Object}
 */

var isList = function(list) {
  return !(!list || Object.prototype.toString.call(list) !== '[object Array]');
};

var inputAndSelectBox = function(options) {

  var selectOptions = options.selectOptions,
    modelProperty = options.modelProperty,
    customValue = options.customValue || 'custom',
    customName = options.customName || 'custom ' + modelProperty,
    label = options.label || options.id,
    canBeDisabled = !!options.disabled && typeof options.disabled === 'function',
    canBeHidden = !!options.hidden && typeof options.hidden === 'function'

  // check if a value is not a built in value
  var isCustomValue = function(value) {
    if (typeof value[modelProperty] === 'undefined') {
      return false;
    }

    var isCustom = !find(selectOptions, function(option) {
      return value[modelProperty] === option.value;
    });

    return isCustom;
  };

  var comboOptions = assign({}, options);

  // true if the selected value in the select box is customValue
  comboOptions.showCustomInput = function(element, node) {
    var selectBox = domQuery('[data-entry="'+ options.id +'"] select', node.parentNode);

    if (selectBox) {
      return selectBox.value === customValue;
    }

    return false;
  };
  /*
  comboOptions.get = function(element, node) {
    var value = options.get(element, node);

    var modifiedValues = {};

    if (!isCustomValue(value)) {
      modifiedValues[modelProperty] = value[modelProperty] || '';

      return modifiedValues;
    }

    modifiedValues[modelProperty] = customValue;
    modifiedValues['custom-'+modelProperty] = value[modelProperty];

    return modifiedValues;
  };
   */

  /*
  comboOptions.set = function(element, values, node) {
    var modifiedValues = {};

    // if the custom select option has been selected
    // take the value from the text input field
    if (values[modelProperty] === customValue) {
      modifiedValues[modelProperty] = values['custom-' + modelProperty] || '';
    }
    else if (options.emptyParameter && values[modelProperty] === '') {
      modifiedValues[modelProperty] = undefined;
    } else {
      modifiedValues[modelProperty] = values[modelProperty];
    }
    return options.set(element, modifiedValues, node);
  };
   */
  console.log("input and select")
  console.log(options)
  console.log(comboOptions)
  /*
  if(typeof comboOptions.selectOptions != 'undefined'){
    comboOptions.selectOptions.push({ name: customName, value: customValue });
  }

   */
  var inputAndSelectBoxEntry = assign({}, comboOptions);
  var match = function (liText,inputText) {
    if(liText.indexOf(inputText)==0){
      return true;
    }else{
      return false;
    }
  }
  var setLi = function(){
    var inputText = $("#camunda-"+escapeHTML(options.id)).val();
    var liElems = $("#"+escapeHTML(options.id)+"-select-ul").children('li');
    forEach(liElems,function (liEle) {
      if(match(liEle.innerText,inputText)){
        $(liEle).addClass("es-visible");
        $(liEle).css({"display":"block"});
      }else{
        $(liEle).removeClass("es-visible");
        $(liEle).css({"display":"none"});
      }
    })
  }
  inputAndSelectBoxEntry.html =
    '<label for="camunda-' + escapeHTML(options.id) + '"' +
    (canBeDisabled ? 'data-disable="isDisabled" ' : '') +
    (canBeHidden ? 'data-show="isHidden" ' : '') +
    '>' + escapeHTML(label) + '</label>' +
    '<input name="'+ escapeHTML(options.id) +'" type="text" autocomplete="off" class="es-input" id="camunda-'+escapeHTML(options.id)+'" data-action="showUl" data-change="showUl">';
  inputAndSelectBoxEntry.html += '<ul class="es-list" style="width: 151px; display: none" id="'+escapeHTML(options.id)+'-select-ul">';
  if (isList(selectOptions)) {
    forEach(selectOptions, function(option) {
      inputAndSelectBoxEntry.html += '<li  class="" style="display: block;" data-action="setInput">'+
        (option.name ? escapeHTML(option.name) : '') + '</li>';
    });
  }
  inputAndSelectBoxEntry.html += '</ul>';
  inputAndSelectBoxEntry.showUl = function () {
    this.hideUl()
    setLi()
    var selHeight = $("#camunda-"+escapeHTML(options.id)).innerHeight();
    var sely=$("#camunda-"+escapeHTML(options.id)).position().top;
    var selx = $("#camunda-"+escapeHTML(options.id)).position().left;
    //$("#"+escapeHTML(options.id)+"-select-ul").css({"top": sely,"left": selx});
    var sely1=$("#camunda-"+escapeHTML(options.id)).offsetParent().innerHeight()-$("#camunda-"+escapeHTML(options.id)).position().top;
    console.log("bottom")
    console.log($("#camunda-"+escapeHTML(options.id)).offsetParent().height())
    console.log(sely)
    $("#"+escapeHTML(options.id)+"-select-ul").css({"bottom": sely1,"left": selx});
    $("#"+escapeHTML(options.id)+"-select-ul").css({"display":"block"});
  }
  inputAndSelectBoxEntry.hideUl = function () {
    console.log("活动元素")
    $("#"+escapeHTML(options.id)+"-select-ul").css({"display":"none"});
  }
  inputAndSelectBoxEntry.setInput = function(element, node, event, scopeNode){
    console.log("setInput")
    console.log(element)
    console.log(node)
    console.log(event)
    console.log(event.toElement.innerText);
    $("#camunda-"+escapeHTML(options.id)).val(event.toElement.innerText);
    var elem = document.getElementById('camunda-'+escapeHTML(options.id));
    var e = new InputEvent('input');
    e.initEvent('input',true,false);
    elem.dispatchEvent(e);
    this.hideUl()
  }
  inputAndSelectBoxEntry.serverParams = {}
  inputAndSelectBoxEntry.askParams = function (serviceName) {
    var form = {
      'serviceName': serviceName
    }
    var i;
    var that = this;
    var url = 'http://127.0.0.1:8848/nacos/v1/ns/instance/list?';
    forEach(form,function (v,k) {
      url+=k;
      url+='=';
      url+=v;
    })
    console.log("url");
    console.log(url);
    $.ajax({
      type: 'GET',
      url: url,
      data: form,
      processData: false,
      contentType: false,
      async: false,
      dataType: "JSON",
      success: function (data) {
        that.serverParams = properties2Json(data.metadata)
        that.serverFunc = []
        forEach(that.serverParams.api,function (v,k) {
          console.log(k,v)
          that.serverFunc.push({ name: v.func, value: v.func})
        })
        console.log(that.serverFunc);
      },
      error: function (data) {
        console.log("error");
      }
    });
  }
  /*
  if(options.id=='selectServer'){
    inputAndSelectBoxEntry.askParams($("#camunda-"+options.id).val())
  }
   */
  return inputAndSelectBoxEntry;
};

module.exports = inputAndSelectBox;
