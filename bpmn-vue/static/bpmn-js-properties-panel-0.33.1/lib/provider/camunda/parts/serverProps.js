'use strict';

var is = require('bpmn-js/lib/util/ModelUtil').is,
  getBusinessObject = require('bpmn-js/lib/util/ModelUtil').getBusinessObject;

var entryFactory = require('../../../factory/EntryFactory');
var cmdHelper = require('../../../helper/CmdHelper')
var participantHelper = require('../../../helper/ParticipantHelper');

module.exports = function(group, element, translate) {
  var bo = getBusinessObject(element);

  if (!bo) {
    return;
  }

  if (is(element, 'bpmn:UserTask')) {

    var isWebService = entryFactory.checkbox({
      id: 'isWebService',
      label: '请求服务',
      modelProperty: 'isWebService',
      get: function(element) {
        var bo = getBusinessObject(element);
        console.log("get isWebService")
        return {
          isWebService: bo.get('camunda:isWebService')
        }
      },
      set: function(element, values) {
        var bo = getBusinessObject(element);
        var isWebServiceValue = values.isWebService || false;
        var commands = []
        commands.push(cmdHelper.updateBusinessObject(element, bo, {'camunda:isWebService': isWebServiceValue}))
        return commands;
      }
    });

    //group.entries.push(isWebService);

    var description = entryFactory.textField({
      id: 'description',
      label: '服务描述',
      modelProperty: 'description',
      get: function(element) {
        var bo = getBusinessObject(element);
        console.log("description")
        return {
          description: bo.get('camunda:description')
        }
      },
      set: function(element, values) {
        var bo = getBusinessObject(element);
        var descriptionValue = values.description || false;
        var commands = []
        commands.push(cmdHelper.updateBusinessObject(element, bo, {'camunda:description': descriptionValue}))
        return commands;
      } 
    });
/*
    entryFactory.checkbox({
      id: 'description',
      label: '服务描述',
      modelProperty: 'description',
      get: function(element) {
        var bo = getBusinessObject(element);
        console.log("description")
        return {
          description: bo.get('camunda:description')
        }
      },
      set: function(element, values) {
        var bo = getBusinessObject(element);
        var descriptionValue = values.description || false;
        var commands = []
        commands.push(cmdHelper.updateBusinessObject(element, bo, {'camunda:description': descriptionValue}))
        return commands;
      }
    });
*/
    //group.entries.push(description);
  }

};
