'use strict';

var getBusinessObject = require('bpmn-js/lib/util/ModelUtil').getBusinessObject;

var domQuery = require('min-dom').query,
  domClosest = require('min-dom').closest,
  domify = require('min-dom').domify,
  forEach = require('lodash/forEach');

var elementHelper = require('../../../../helper/ElementHelper'),
  cmdHelper = require('../../../../helper/CmdHelper'),
  utils = require('../../../../Utils'),
  escapeHTML = utils.escapeHTML;

function getSelectBox(node, id) {
  var currentTab = domClosest(node, 'div.bpp-properties-tab');
  var query = 'select[name=selectedExtensionElement]' + (id ? '[id=cam-extensionElements-' + id + ']' : '');
  return domQuery(query, currentTab);
}

function getSelected(node, id) {
  var selectBox = getSelectBox(node, id);
  return {
    value: (selectBox || {}).value,
    idx: (selectBox || {}).selectedIndex
  };
}

function generateElementId(prefix) {
  prefix = prefix + '_';
  return utils.nextId(prefix);
}

var CREATE_EXTENSION_ELEMENT_ACTION = 'create-extension-element',
  REMOVE_EXTENSION_ELEMENT_ACTION = 'remove-extension-element';

module.exports = function(element, bpmnFactory, options, translate) {

  var id = options.id,
    prefix = options.prefix || 'elem',
    label = options.label || id,
    idGeneration = (options.idGeneration === false) ? options.idGeneration : true,
    businessObject = options.businessObject || getBusinessObject(element);

  var modelProperty = options.modelProperty || 'id';

  var getElements = options.getExtensionElements;

  var createElement = options.createExtensionElement,
    canCreate = typeof createElement === 'function';

  var removeElement = options.removeExtensionElement,
    canRemove = typeof removeElement === 'function';

  var onSelectionChange = options.onSelectionChange;

  var hideElements = options.hideExtensionElements,
    canBeHidden = typeof hideElements === 'function';

  var setOptionLabelValue = options.setOptionLabelValue;

  var defaultSize = options.size || 5,
    resizable = options.resizable;

  var reference = options.reference || undefined;

  var selectionChanged = function(element, node, event, scope) {
    if (typeof onSelectionChange === 'function') {
      return onSelectionChange(element, node, event, scope);
    }
  };

  var createOption = function(value) {
    return '<option value="' + escapeHTML(value) + '" data-value data-name="extensionElementValue">' + escapeHTML(value) + '</option>';
  };

  var initSelectionSize = function(selectBox, optionsLength) {
    if (resizable) {
      selectBox.size = optionsLength > defaultSize ? optionsLength : defaultSize;
    }
  };

  var resource =  {
    id: id,
    get: function(element, node) {
      console.log("extension Element")
      console.log(element)
      console.log(node)
      var elements = getElements(element, node);

      var result = [];
      forEach(elements, function(elem) {
        console.log(elem)
        console.log(elem.get(modelProperty))
        result.push({
          extensionElementValue: elem.get(modelProperty)
        });
      });

      var selectBox = getSelectBox(node.parentNode, id);
      initSelectionSize(selectBox, result.length);

      return result;
    },

    set: function(element, values, node) {
      var action = this.__action;
      delete this.__action;

      businessObject = businessObject || getBusinessObject(element);

      var bo =
        (reference && businessObject.get(reference))
          ? businessObject.get(reference)
          : businessObject;

      var extensionElements = bo.get('extensionElements');

      if (action.id === CREATE_EXTENSION_ELEMENT_ACTION) {
        var commands = [];
        if (!extensionElements) {
          extensionElements = elementHelper.createElement('bpmn:ExtensionElements', { values: [] }, bo, bpmnFactory);
          commands.push(cmdHelper.updateBusinessObject(element, bo, { extensionElements: extensionElements }));
        }
        commands.push(createElement(element, extensionElements, action.value, node));
        return commands;

      }
      else if (action.id === REMOVE_EXTENSION_ELEMENT_ACTION) {
        return removeElement(element, extensionElements, action.value, action.idx, node);
      }

    },

    createListEntryTemplate: function(value, index, selectBox) {
      initSelectionSize(selectBox, selectBox.options.length + 1);
      return createOption(value.extensionElementValue);
    },

    deselect: function(element, node) {
      var selectBox = getSelectBox(node, id);
      selectBox.selectedIndex = -1;
    },

    getSelected: function(element, node) {
      return getSelected(node, id);
    },

    setControlValue: function(element, node, option, property, value, idx) {
      node.value = value;

      if (!setOptionLabelValue) {
        node.text = value;
      } else {
        setOptionLabelValue(element, node, option, property, value, idx);
      }
    },

    createElement: function(element, node) {
      // create option template
      var generatedId;
      if (idGeneration) {
        generatedId = generateElementId(prefix);
      }

      var selectBox = getSelectBox(node, id);
      var template = domify(createOption(generatedId));

      // add new empty option as last child element
      selectBox.appendChild(template);

      // select last child element
      selectBox.lastChild.selected = 'selected';
      selectionChanged(element, node);

      // update select box size
      initSelectionSize(selectBox, selectBox.options.length);

      this.__action = {
        id: CREATE_EXTENSION_ELEMENT_ACTION,
        value: generatedId
      };

      return true;
    },

    removeElement: function(element, node) {
      var selection = getSelected(node, id);

      var selectBox = getSelectBox(node, id);
      selectBox.removeChild(selectBox.options[selection.idx]);

      // update select box size
      initSelectionSize(selectBox, selectBox.options.length);

      this.__action = {
        id: REMOVE_EXTENSION_ELEMENT_ACTION,
        value: selection.value,
        idx: selection.idx
      };

      return true;
    },

    hideElements: function(element, entryNode, node, scopeNode) {
      return !hideElements(element, entryNode, node, scopeNode);
    },

    disableRemove: function(element, entryNode, node, scopeNode) {
      return (getSelected(entryNode, id) || {}).idx < 0;
    },

    selectElement: selectionChanged
  };
  resource.html = '<div class="bpp-row bpp-element-list" ' +
  (canBeHidden ? 'data-show="hideElements"' : '') + '>' +
  '<label for="cam-extensionElements-' + escapeHTML(options.id) + '">' + escapeHTML(options.label) + '</label>' +
  '<div class="bpp-field-wrapper">' +
  '<label for="camunda-' + escapeHTML(options.id) + '" ' +
  (canBeHidden ? 'data-show="isHidden" ' : '') +
  '>'+ escapeHTML('方法') +'</label>' +
  '<div class="bpp-field-wrapper" ' +
  (canBeHidden ? 'data-show="isHidden"' : '') +
  '>' +
  '<input id="camunda-httpMethod-' + escapeHTML(options.id) + '" type="text" name="httpMethod-' + escapeHTML(options.modelProperty) + '" ' +
  (canBeHidden ? 'data-show="isHidden"' : '') +
  // ' data-change="askServerList"' +
  ' />'+
  '</div>';
  //forEach(options.params)
  /*
  '<label for="camunda-' + escapeHTML(resource.id) + '" ' +
  (canBeDisabled ? 'data-disable="isDisabled" ' : '') +
  (canBeHidden ? 'data-show="isHidden" ' : '') +
  (dataValueLabel ? 'data-value="' + escapeHTML(dataValueLabel) + '"' : '') + '>'+ escapeHTML(label) +'</label>' +
  '<div class="bpp-field-wrapper" ' +
  (canBeDisabled ? 'data-disable="isDisabled"' : '') +
  (canBeHidden ? 'data-show="isHidden"' : '') +
  '>' +
  '<input id="camunda-' + escapeHTML(resource.id) + '" type="text" name="' + escapeHTML(options.modelProperty) + '" ' +
  (canBeDisabled ? 'data-disable="isDisabled"' : '') +
  (canBeHidden ? 'data-show="isHidden"' : '') +
  // ' data-change="askServerList"' +
  ' />' +

   */
  resource.html = resource.html + '</div>' + '</div>';
};
