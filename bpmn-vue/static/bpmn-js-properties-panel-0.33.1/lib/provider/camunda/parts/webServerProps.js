'use strict';

var getBusinessObject = require('bpmn-js/lib/util/ModelUtil').getBusinessObject,
  getExtensionElements = require('../../../helper/ExtensionElementsHelper').getExtensionElements,
  removeEntry = require('../../../helper/ExtensionElementsHelper').removeEntry,
  extensionElements = require('./implementation/ExtensionElements'),
  inputAndSelectBox = require('./implementation/inputAndSelectBox'),
  properties = require('./implementation/Properties'),
  entryFactory = require('../../../factory/EntryFactory'),
  elementHelper = require('../../../helper/ElementHelper'),
  cmdHelper = require('../../../helper/CmdHelper'),
  formHelper = require('../../../helper/FormHelper'),
  utils = require('../../../Utils'),
  is = require('bpmn-js/lib/util/ModelUtil').is,
  find = require('lodash/find'),
  forEach = require('lodash/forEach');
var orgNameTextField = require('./implementation/orgNameTextField');
var participantHelper = require('../../../helper/ParticipantHelper');

function generateValueId() {
  return utils.nextId('Value_');
}

/**
 * Generate a form field specific textField using entryFactory.
 *
 * @param  {string} options.id
 * @param  {string} options.label
 * @param  {string} options.modelProperty
 * @param  {function} options.validate
 *
 * @return {Object} an entryFactory.textField object
 */
function formFieldTextField(options, getSelectedFormField) {

  var id = options.id,
    label = options.label,
    modelProperty = options.modelProperty,
    validate = options.validate;

  return entryFactory.textField({
    id: id,
    label: label,
    modelProperty: modelProperty,
    get: function(element, node) {
      var selectedFormField = getSelectedFormField(element, node) || {},
        values = {};

      values[modelProperty] = selectedFormField[modelProperty];

      return values;
    },

    set: function(element, values, node) {
      var commands = [];

      if (typeof options.set === 'function') {
        var cmd = options.set(element, values, node);

        if (cmd) {
          commands.push(cmd);
        }
      }

      var formField = getSelectedFormField(element, node),
        properties = {};

      properties[modelProperty] = values[modelProperty] || undefined;

      commands.push(cmdHelper.updateBusinessObject(element, formField, properties));

      return commands;
    },
    hidden: function(element, node) {
      return !getSelectedFormField(element, node);
    },
    validate: validate
  });
}

function ensureWebServerSupported(element) {
  return is(element, 'bpmn:UserTask');
}

module.exports = function(group, element, bpmnFactory, translate) {

  if (!ensureWebServerSupported(element)) {
    return;
  }


  /**
   * Return the currently selected form field querying the form field select box
   * from the DOM.
   *
   * @param  {djs.model.Base} element
   * @param  {DOMElement} node - DOM element of any form field text input
   *
   * @return {ModdleElement} the currently selected form field
   */
  function getSelectedFormField(element, node) {
    var selected = formFieldsEntry.getSelected(element, node.parentNode);

    if (selected.idx === -1) {
      return;
    }

    return formHelper.getFormField(element, selected.idx);
  }

  function getSelectFunc (element) {
    return getBusinessObject(element).get('camunda:selectFunc')
  }
  function getWebServerEle(element,commands){
    var bo = getBusinessObject(element);
    var FormPropertyList = getExtensionElements(bo, 'camunda:InputOutput')
    var webServer = undefined;
    forEach(FormPropertyList,function (FormProperty) {
      if(FormProperty.get('type')=='webServer'){
        webServer = FormProperty;
      }
    })
    if(typeof webServer =='undefined'){
      webServer = createWebServer(element,commands);
    }
    return webServer;
  }

  function getWebServerVal(element,name,commands) {
    var webServer = undefined;
    webServer = getWebServerEle(element,commands);
    if(typeof webServer !== 'undefined'){
      return webServer.get(name);
    }else{
      return '';
    }
  }

  function createWebServer(element,commands){
    var bo = getBusinessObject(element);
    var extensionElem = bo.get('extensionElements');
    if(typeof extensionElem == 'undefined'){
      extensionElem = elementHelper.createElement('bpmn:ExtensionElements', {values:[]}, extensionElements, bpmnFactory);
      commands.push(cmdHelper.updateBusinessObject(element, bo, { extensionElements: extensionElem }));
    }
    var webServer = elementHelper.createElement('camunda:InputOutput', { type: 'webServer', inputParameters:[], outputParameters:[]}, extensionElements, bpmnFactory);
    commands.push(cmdHelper.addElementsTolist(element, extensionElem,"values",[webServer]));
    return webServer;
  }

  function setResponseParams (element,responseDict,commands) {
    var webServer = undefined;
    webServer = getWebServerEle(element,commands);
    forEach(webServer.values,function (paramsEle) {
      if(paramsEle.get('type')=='responseParams'){
        commands.push(cmdHelper.addAndRemoveElementsFromList(
          element,
          webServer,
          'outputParameters',
          'camunda:OutputParameter',
          [],
          [paramsEle]
        ));
      }
    })
    forEach(responseDict,function (resItem,i) {
      var responseParamEle = elementHelper.createElement('camunda:OutputParameter',{ type:'responseParams', variableName: resItem.variableName, variableType: resItem.variableType }, webServer, bpmnFactory);
      commands.push(cmdHelper.addElementsTolist(element,webServer,"outputParameters",[responseParamEle]));
    })
  }

  function setResquestParams (element,resquestDict,commands) {
    var webServer = undefined;
    webServer = getWebServerEle(element,commands);
    forEach(webServer.values,function (paramsEle) {
      if(paramsEle.get('type')=='requestParams'){
        commands.push(cmdHelper.addAndRemoveElementsFromList(
          element,
          webServer,
          'inputParameters',
          'camunda:InputParameter',
          [],
          [paramsEle]
        ));
      }
    })
    forEach(resquestDict,function (reqItem,i) {
      var requestParamEle = elementHelper.createElement('camunda:InputParameter',{ type:'requestParams', variableName: reqItem.variableName, variableType: reqItem.variableType, valueSrc:''  }, webServer, bpmnFactory);
      commands.push(cmdHelper.addElementsTolist(element,webServer,"inputParameters",[requestParamEle]));
    })
  }

  function getReqParam (element,paramName) {
    var webServer = undefined;
    webServer = getWebServerEle(element,[]);
    var reqParam = '';
    forEach(webServer.inputParameters,function (paramsEle) {
      if(paramsEle.get('type')=='requestParams'&&paramsEle.get('variableName')==paramName){
        reqParam = paramsEle.get('valueSrc');
      }
    })
    return reqParam;
  }
  function setReqParam (element,paramName,value,commands) {
    var webServer = undefined;
    webServer = getWebServerEle(element,[]);
    var reqParam = '';
    forEach(webServer.inputParameters,function (paramsEle) {
      if(paramsEle.get('type')=='requestParams'&&paramsEle.get('variableName')==paramName){
        commands.push(cmdHelper.updateBusinessObject(element, paramsEle, { 'valueSrc': value }));
      }
    })
  }
  var orgNameField = entryFactory.orgNameTextField({
    id : 'orgName',
    label : '组织名',
    modelProperty: 'orgName',
    get: function(element, node) {
      return {
        orgName: getWebServerVal(element,'orgName',[])
      }
    },
    set: function(element, values, node) {
      var bo = getBusinessObject(element),
        orgName = values.orgName || undefined;
      var commands = []
      var webServer = getWebServerEle(element,commands);
      commands.push(cmdHelper.updateBusinessObject(element, webServer, { 'orgName': orgName }))
      return commands;
    }
  })
  group.entries.push(orgNameField);
  orgNameField.askServerList(getWebServerVal(element,'orgName',[]))
  var selectServerField = inputAndSelectBox({
    id: 'selectServer',
    label: '选择服务',
    modelProperty: 'selectServer',
    //selectOptions: serverList,
    selectOptions: orgNameField.queryServer,
    emptyParameter: true,
    get: function(element, node) {
      return {
        selectServer: getWebServerVal(element,'selectServer',[])
      }
    },
    set: function(element, values, node) {
      var bo = getBusinessObject(element),
        selectServer = values.selectServer || undefined;
      var commands = []
      var webServer = getWebServerEle(element,commands);
      commands.push(cmdHelper.updateBusinessObject(element, webServer, { 'selectServer': selectServer }))
      return commands;
    },
    hidden: function(element, node) {
      return false;
    }
  });
  group.entries.push(selectServerField);
  selectServerField.askParams(getWebServerVal(element,'selectServer',[]))
  var selectFuncField = inputAndSelectBox({
    id: 'selectFunc',
    label: '选择操作',
    modelProperty: 'selectFunc',
    selectOptions: selectServerField.serverFunc,
    emptyParameter: true,
    get: function(element, node) {
      return {
        selectFunc: getWebServerVal(element,'selectFunc',[])
      }
    },
    set: function(element, values, node) {
      var bo = getBusinessObject(element),
        selectFunc = values.selectFunc || undefined;
      var commands = []
      var webServer = getWebServerEle(element,commands);
      var selectApi=undefined;
      forEach(selectServerField.serverParams.api,function (v,k) {
        if(v.func==selectFunc){
          selectApi = v;
        }
      })
      commands.push(cmdHelper.updateBusinessObject(element, webServer, { 'selectFunc': selectFunc, 'httpMethod': selectApi.httpMethod}))
      setResquestParams(element,selectApi.request,commands)
      setResponseParams(element,selectApi.response,commands)
      return commands;
    },
    hidden: function(element, node) {
      return false;
    }
  });
  group.entries.push(selectFuncField);

  forEach(selectServerField.serverParams.api, function (v,k) {
    if(v.func == getWebServerVal(element,'selectFunc',[])){
      var executableEntry = entryFactory.checkbox({
        id: 'webServerAuto',
        label: '非自动',
        modelProperty: 'nonAuto',
        get: function(element) {
          return {
            nonAuto: getWebServerVal(element,'nonAuto',[])
          }
        },
        set: function(element, values) {
          var commands=[];
          var webServer = getWebServerEle(element,commands)
          if(typeof values['nonAuto'] == 'undefined'){
            commands.push(cmdHelper.updateBusinessObject(element, webServer, {'nonAuto':false} ))
          }else{
            commands.push(cmdHelper.updateBusinessObject(element, webServer, {'nonAuto':values['nonAuto']} ))
          }
          return commands
        }
      });
      group.entries.push(executableEntry);
      /*
      group.entries.push(entryFactory.validationAwareTextField({
        id: 'httpMethod',
        label: '方法',
        modelProperty: 'httpMethod',
        getProperty: function(element, node) {
          var showMethod = getWebServerVal(element,'httpMethod',[])
          return showMethod;
        },
        setProperty: function(element, properties, node) {
          var bo = getBusinessObject(element)
          return cmdHelper.updateBusinessObject(element, bo, properties);
        },
        hidden: function(element, node) {
          return getSelectFunc(element)=='';
        },
        validate: function(element, values) {
          return true;
        }
      }));
       */
      if(Object.keys(v.request).length>0){
        group.entries.push(entryFactory.label({
          id: 'requestParams',
          labelText: '请求参数',
          showLabel: function(element, node) {
            return true;
          }
        }));
      }
      forEach(v.request, function (reqItem, i) {
        if(reqItem.variableType=='ServerJSONString'){
          var responseOrgNameField = entryFactory.orgNameTextField({
            id : 'responseOrgName',
            label : '组织名',
            modelProperty: 'responseOrgName',
            get: function(element, node) {
              return {
                responseOrgName: getWebServerVal(element,'responseOrgName',[])
              }
            },
            set: function(element, values, node) {
              var bo = getBusinessObject(element),
                responseOrgName = values.responseOrgName || undefined;
              var commands = []
              var webServer = getWebServerEle(element,commands);
              commands.push(cmdHelper.updateBusinessObject(element, webServer, { 'responseOrgName': responseOrgName }))
              return commands;
            }
          })
          group.entries.push(responseOrgNameField);
          responseOrgNameField.askServerList(getWebServerVal(element,'responseOrgName',[]))
          var responseSelectServerField = inputAndSelectBox({
            id: 'responseSelectServer',
            label: '选择服务',
            modelProperty: 'responseSelectServer',
            //selectOptions: serverList,
            selectOptions: responseOrgNameField.queryServer,
            emptyParameter: true,
            get: function(element, node) {
              return {
                responseSelectServer: getWebServerVal(element,'responseSelectServer',[])
              }
            },
            set: function(element, values, node) {
              var bo = getBusinessObject(element),
                responseSelectServer = values.responseSelectServer || undefined;
              var commands = []
              var webServer = getWebServerEle(element,commands);
              commands.push(cmdHelper.updateBusinessObject(element, webServer, { 'responseSelectServer': responseSelectServer }))
              return commands;
            },
            hidden: function(element, node) {
              return false;
            }
          });
          group.entries.push(responseSelectServerField);
          responseSelectServerField.askParams(getWebServerVal(element,'responseSelectServer',[]))
          var responseSelectFuncField = inputAndSelectBox({
            id: 'responseSelectFunc',
            label: '选择操作',
            modelProperty: 'responseSelectFunc',
            selectOptions: responseSelectServerField.serverFunc,
            emptyParameter: true,
            get: function(element, node) {
              return {
                responseSelectFunc: getWebServerVal(element,'responseSelectFunc',[])
              }
            },
            set: function(element, values, node) {
              var bo = getBusinessObject(element),
                responseSelectFunc = values.responseSelectFunc || undefined;
              var commands = []
              var webServer = getWebServerEle(element,commands);
              var selectApi=undefined;
              forEach(responseSelectServerField.serverParams.api,function (v,k) {
                if(v.func==responseSelectFunc){
                  selectApi = v;
                }
              })
              commands.push(cmdHelper.updateBusinessObject(element, webServer, { 'responseSelectFunc': responseSelectFunc, 'responseHttpMethod': selectApi.httpMethod}))
              return commands;
            },
            hidden: function(element, node) {
              return false;
            }
          });
          group.entries.push(responseSelectFuncField);
        }else{
          group.entries.push(entryFactory.textField({
            id : reqItem.variableName,
            label : reqItem.variableName,
            modelProperty: reqItem.variableName,
            get: function(element, node) {
              var getReq = {}
              var paramName = this.id;
              getReq[paramName] = getReqParam(element,paramName);
              return getReq;
            },
            set: function(element, values, node) {
              var commands = []
              var value = values[reqItem.variableName] || undefined;
              var paramName = this.id;
              setReqParam(element,paramName,value,commands);
              return commands;
            },
            hidden: function(element, node) {
              return getSelectFunc(element, node)=='';
            }
          }))
        }
      })
      if(Object.keys(v.response).length>0){
        group.entries.push(entryFactory.label({
          id: 'responseParams',
          labelText: '返回参数',
          showLabel: function(element, node) {
            return true;
          }
        }));
      }
      forEach(v.response, function (resItem, i) {
        group.entries.push(entryFactory.label({
          id: resItem.variableName,
          labelText: 'Name:'+resItem.variableName+' Type:'+resItem.variableType,
          showLabel: function(element, node) {
            return true;
          }
        }));
      })
    }
  })
};
