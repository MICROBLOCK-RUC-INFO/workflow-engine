'use strict';

var escapeHTML = require('../../../../Utils').escapeHTML;

var domQuery = require('min-dom').query;

var entryFieldDescription = require('../../../../factory/EntryFieldDescription');

var $ = require('jquery');

var forEach = require('lodash/forEach');

var orgNameTextField = function(options, defaultParameters) {

  // Default action for the button next to the input-field
  var defaultButtonAction = function(element, inputNode) {
    var input = domQuery('input[name="' + options.modelProperty + '"]', inputNode);
    input.value = '';

    return true;
  };

  // default method to determine if the button should be visible
  var defaultButtonShow = function(element, inputNode) {
    var input = domQuery('input[name="' + options.modelProperty + '"]', inputNode);

    return input.value !== '';
  };

  var resource = defaultParameters,
    label = options.label || resource.id,
    dataValueLabel = options.dataValueLabel,
    buttonLabel = (options.buttonLabel || 'X'),
    actionName = (typeof options.buttonAction != 'undefined') ? options.buttonAction.name : 'clear',
    actionMethod = (typeof options.buttonAction != 'undefined') ? options.buttonAction.method : defaultButtonAction,
    showName = (typeof options.buttonShow != 'undefined') ? options.buttonShow.name : 'canClear',
    showMethod = (typeof options.buttonShow != 'undefined') ? options.buttonShow.method : defaultButtonShow,
    canBeDisabled = !!options.disabled && typeof options.disabled === 'function',
    canBeHidden = !!options.hidden && typeof options.hidden === 'function',
    description = options.description;
  console.log(options.hidden)
  console.log(!options.hidden)
  console.log(!!options.hidden)
  resource.html =
    '<label for="camunda-' + escapeHTML(resource.id) + '" ' +
    (canBeDisabled ? 'data-disable="isDisabled" ' : '') +
    (canBeHidden ? 'data-show="isHidden" ' : '') +
    (dataValueLabel ? 'data-value="' + escapeHTML(dataValueLabel) + '"' : '') + '>'+ escapeHTML(label) +'</label>' +
    '<div class="bpp-field-wrapper" ' +
    (canBeDisabled ? 'data-disable="isDisabled"' : '') +
    (canBeHidden ? 'data-show="isHidden"' : '') +
    '>' +
    '<input id="camunda-' + escapeHTML(resource.id) + '" type="text" value="' + options.getProperty() + '" name="' + escapeHTML(options.modelProperty) + '" ' +
    (canBeDisabled ? 'data-disable="isDisabled"' : '') +
    (canBeHidden ? 'data-show="isHidden"' : '') +
    // ' data-change="askServerList"' +
    ' />' +
    '</div>';

  // add description below text input entry field
  if (description) {
    resource.html += entryFieldDescription(description);
  }

  resource[actionName] = actionMethod;
  resource[showName] = showMethod;

  if (canBeDisabled) {
    resource.isDisabled = function() {
      return options.disabled.apply(resource, arguments);
    };
  }

  if (canBeHidden) {
    resource.isHidden = function() {
      return !options.hidden.apply(resource, arguments);
    };
  }

  resource.cssClasses = ['bpp-textfield'];
  // resource.queryServer = [];
  resource.askServerList = function (orgNameStr) {
    var form = {
      'pageNo': '1',
      'pageSize': '100',
      'namespaceId': orgNameStr
    }
    var i;
    var that = this;
    var url = 'http://127.0.0.1:8848/nacos/v1/ns/service/list?';
    forEach(form,function (v,k) {
      url+=k;
      url+='=';
      url+=v;
      url+='&';
    })
    url=url.substring(0,url.length-1);
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
        that.queryServer=[];
        for(i = 0; i< data.doms.length; i++){
          that.queryServer.push({ name: data.doms[i], value: data.doms[i]});
        }
        console.log("askServerList")
        console.log(that)
      },
      error: function (data) {
        console.log("error");
      }
    });
  };
  resource.askServerList($("#camunda-"+options.id).val());
  resource.getInput = function () {
    return $("#camunda-"+options.id).val();
  }
  return resource;
};

module.exports = orgNameTextField;
