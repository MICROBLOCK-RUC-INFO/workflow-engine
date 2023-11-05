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

  var orgNameField = entryFactory.orgNameTextField({
    id : 'orgName',
    label : '组织名',
    modelProperty: 'orgName',
    get: function(element, node) {
      var bo = getBusinessObject(element);
      console.log("org Name")
      console.log(bo.get('camunda:orgName'))
      return {
        orgName: bo.get('camunda:orgName')
      };
    },
    set: function(element, values, node) {
      var bo = getBusinessObject(element),
        orgName = values.orgName || undefined;
      return cmdHelper.updateBusinessObject(element, bo, { 'camunda:orgName': orgName });
    }
  })
  group.entries.push(orgNameField);
  orgNameField.askServerList(getBusinessObject(element).get('camunda:orgName'))
  var selectServerField = inputAndSelectBox({
    id: 'selectServer',
    label: '选择服务',
    modelProperty: 'selectServer',
    //selectOptions: serverList,
    selectOptions: orgNameField.queryServer,
    emptyParameter: true,
    get: function(element, node) {
      var bo = getBusinessObject(element);
      console.log("selectServer")
      console.log(bo.get('camunda:selectServer'))
      return {
        selectServer: bo.get('camunda:selectServer')
      };
    },
    set: function(element, values, node) {
      var bo = getBusinessObject(element),
        selectServer = values.selectServer || undefined;
      return cmdHelper.updateBusinessObject(element, bo, { 'camunda:selectServer': selectServer });
    },
    hidden: function(element, node) {
      return false;
    }
  });
  group.entries.push(selectServerField);
  selectServerField.askParams(getBusinessObject(element).get('camunda:selectServer'))
  var selectFuncField = inputAndSelectBox({
    id: 'selectFunc',
    label: '选择函数',
    modelProperty: 'selectFunc',
    selectOptions: selectServerField.serverFunc,
    emptyParameter: true,
    get: function(element, node) {
      var bo = getBusinessObject(element);
      console.log("selectFunc")
      console.log(bo.get('camunda:selectFunc'))
      return {
        selectFunc: bo.get('camunda:selectFunc')
      };
    },
    set: function(element, values, node) {
      var bo = getBusinessObject(element),
        selectFunc = values.selectFunc || undefined;
      return cmdHelper.updateBusinessObject(element, bo, { 'camunda:selectFunc': selectFunc });
    },
    hidden: function(element, node) {
      return false;
    }
  });
  group.entries.push(selectFuncField);

  forEach(selectServerField.serverParams.api, function (v,k) {
    if(v.func == getSelectFunc(element)){
      group.entries.push(entryFactory.validationAwareTextField({
        id: 'httpMethod',
        label: '方法',
        modelProperty: 'httpMethod',
        getProperty: function(element, node) {
          var bo = getBusinessObject(element)
          var showMethod = "";
          forEach(selectServerField.serverParams.api,function (v,k) {
            if(v.func==getSelectFunc(element)){
              showMethod = v.httpMethod;
            }
          })
          var setRes = {}
          setRes['camunda:httpMethod'] = showMethod;
          cmdHelper.updateBusinessObject(element, bo, setRes);
          return showMethod;
        },
        setProperty: function(element, properties, node) {
          var bo = getBusinessObject(element)
          /*,
          value = values[reqItem.variableName] || undefined;
        var setRes = {}
        setRes['camunda:'+reqItem.variableName] = value;
        return cmdHelper.updateBusinessObject(element, bo, setRes);

           */
          return cmdHelper.updateBusinessObject(element, bo, properties);
        },
        hidden: function(element, node) {
          return getSelectFunc(element)=='';
        },
        validate: function(element, values) {
          return true;
        }
      }));
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
        group.entries.push(entryFactory.textField({
          id : reqItem.variableName,
          label : reqItem.variableName,
          modelProperty: reqItem.variableName,
          get: function(element, node) {
            var bo = getBusinessObject(element);
            console.log(reqItem.variableName)
            console.log(bo.get('camunda:'+reqItem.variableName))
            var getRes = {}
            getRes[reqItem.variableName] = bo.get('camunda:'+reqItem.variableName)
            return getRes;
          },
          set: function(element, values, node) {
            var bo = getBusinessObject(element),
              value = values[reqItem.variableName] || undefined;
            var setRes = {}
            setRes['camunda:'+reqItem.variableName] = value;
            return cmdHelper.updateBusinessObject(element, bo, setRes);
          },
          hidden: function(element, node) {
            return getSelectFunc(element, node)=='';
          }
        }))
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
        group.entries.push(entryFactory.textField({
          id : resItem.variableName,
          label : resItem.variableName,
          modelProperty: resItem.variableName,
          get: function(element, node) {
            var bo = getBusinessObject(element);
            console.log(resItem.variableName)
            console.log(bo.get('camunda:'+resItem.variableName))
            var getRes = {}
            getRes[resItem.variableName] = bo.get('camunda:'+resItem.variableName)
            return getRes;
          },
          set: function(element, values, node) {
            var bo = getBusinessObject(element),
              value = values[resItem.variableName] || undefined;
            var setRes = {}
            setRes['camunda:'+resItem.variableName] = value;
            return cmdHelper.updateBusinessObject(element, bo, setRes);
          },
          hidden: function(element, node) {
            return getSelectFunc(element, node)=='';
          }
        }))
      })
    }
  })
};
