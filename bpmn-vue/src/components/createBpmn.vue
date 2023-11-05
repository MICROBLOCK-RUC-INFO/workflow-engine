<template>
  <div class="col-12" style="height: 100%">
    <div class="row" style="height:calc(100% - 50px);">
      <div class="col-lg-9 col-sm-8">
        <div id="canvas" style="height: 100%"></div>
      </div>
      <div class="col-lg-3 col-sm-4" style="height: 100%;overflow-y: auto; padding: unset">
        <div id="properties-panel" style="width: 100%; padding: 0 10px 0 0">
        </div>
      </div>
    </div>
    <div class="row pb-5" style="height:50px;">
      <div style="margin:0 auto;">
      <button class="btn btn-primary" @click="createBpmnClick($event)" style="background-color:#891524">创建</button>
      <a class="btn btn-primary" id="saveBpmn" style="background-color:#891524;color:white">保存</a>
      <button class="btn btn-primary" @click="openBpmnClick" style="background-color:#891524">打开</button>
      <input type="file" style="display: none" id="fileInput"/>
      </div>
    </div>
  </div>
</template>

<script>
import $ from 'jquery'
import BpmnModeler from '../../static/bpmn-js-6.4.1/lib/Modeler'
import propertiesPanelModule from '../../static/bpmn-js-properties-panel-0.33.1'
import propertiesProviderModule from '../../static/bpmn-js-properties-panel-0.33.1/lib/provider/camunda'
// import BpmnModeler from 'bpmn-js/lib/Modeler'
// import propertiesPanelModule from 'bpmn-js-properties-panel'
// import propertiesProviderModule from 'bpmn-js-properties-panel/lib/provider/camunda'
import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda'
import x2js from 'x2js'
import 'jquery-editable-select'
import {
  debounce
} from 'min-dash'

export default {
  name: 'createBpmn',
  components: {},
  // 生命周期 - 创建完成（可以访问当前this实例）
  created () {
  },
  // 生命周期 - 载入后, Vue 实例挂载到实际的 DOM 操作完成，一般在该过程进行 Ajax 交互
  mounted () {
    this.init()
  },
  data () {
    return {
      // bpmn建模器
      bpmnModeler: null,
      container: null,
      canvas: null,
      loading: true,
      $x2js: null,
      // encodedData: null,
      xmltmp: '<?xml version="1.0" encoding="UTF-8"?>\n' +
        '<bpmn2:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd" id="sample-diagram" targetNamespace="http://bpmn.io/schema/bpmn">\n' +
        '  <bpmn2:process id="Process_1">\n' +
        '    <bpmn2:startEvent id="StartEvent_1"/>\n' +
        '  </bpmn2:process>\n' +
        '  <bpmndi:BPMNDiagram id="BPMNDiagram_1">\n' +
        '    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">\n' +
        '      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">\n' +
        '        <dc:Bounds height="36.0" width="36.0" x="412.0" y="240.0"/>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '    </bpmndi:BPMNPlane>\n' +
        '  </bpmndi:BPMNDiagram>\n' +
        '</bpmn2:definitions>'
    }
  },
  // 方法集合
  methods: {
    saveBpmnClick () {

    },
    createBpmnClick (e) {
      console.log(e)
      e.stopPropagation()
      e.preventDefault()
      this.createNewDiagram()
    },
    openBpmnClick () {
      $('#fileInput').click()
    },
    async init () {
      var that = this
      this.bpmnModeler = new BpmnModeler({
        container: '#canvas',
        propertiesPanel: {
          parent: '#properties-panel'
        },
        additionalModules: [
          propertiesProviderModule,
          propertiesPanelModule
        ],
        moddleExtensions: {
          camunda: camundaModdleDescriptor
        }

      })
      $('svg').css('height','100%');
      this.$x2js = new x2js()
      $('#fileInput').change(function (e) {
        let reads = new FileReader()
        let file = e.target.files[0]
        reads.readAsText(file, 'utf-8')
        reads.onload = function (e) {
          // console.log(e.target.result)
          var xml = e.target.result
          xml = xml.replace(/http:\/\/activiti.org\/bpmn/, 'http://camunda.org/schema/1.0/bpmn')
          var patt = /<!\[CDATA\[.*?\]\]>/g
          var CDATAStr = xml.match(patt)
          console.log('CDATAStr')
          console.log(CDATAStr)
          if (!(!CDATAStr)) {
            for (var i = 0; i < CDATAStr.length; i++) {
              var tmp = CDATAStr[i].replace(/&/g, '&amp;')
              tmp = tmp.replace(/</g, '&lt;')
              tmp = tmp.replace(/>/g, '&gt;')
              xml = xml.replace(CDATAStr[i], tmp)
            }
          }
          var jsonObj = that.$x2js.xml2js(xml)
          that.returnFormProperty(jsonObj)
          // let newXml = xml
          let newXml = that.$x2js.js2xml(jsonObj)
          // console.log(newXml)
          that.bpmnModeler.importXML(newXml, function (err) {
            if (err) {
              console.error(err)
            }
          })
        }
      })
      var downloadLink = $('#saveBpmn')
      // var downloadSvgLink = $('#js-download-svg');
      $('.buttons a').click(function (e) {
        if (!$(this).is('.active')) {
          e.preventDefault()
          e.stopPropagation()
        }
      })

      var exportArtifacts = debounce(function () {
        console.log('xml change')
        /*
        that.saveSVG(function(err, svg) {
          that.setEncoded(downloadSvgLink, 'diagram.svg', err ? null : svg);
        });
        */
        //下载为bpmn将camunda版转为activiti版
        that.saveDiagram(function (err, xml) {
          xml = xml.replace(/http:\/\/camunda.org\/schema\/1.0\/bpmn/, 'http://activiti.org/bpmn')
          xml = xml.replace(/&lt;/g, '<')
          xml = xml.replace(/&gt;/g, '>')
          xml = xml.replace(/&amp;/g, '&')
          var jsonObj = that.$x2js.xml2js(xml)
          that.getFormProperty(jsonObj)
          let newXml = that.$x2js.js2xml(jsonObj)
          console.log(jsonObj)
          that.setEncoded(downloadLink, 'diagram.bpmn', err ? null : newXml)
        })
      }, 500)

      this.bpmnModeler.on('commandStack.changed', exportArtifacts)
    },
    setEncoded (link, name, data) {
      var encodedData = encodeURIComponent(data)
      console.log('save')
      if (data) {
        console.log('true')
        console.log(link)
        link.addClass('active').attr({
          'href': 'data:application/bpmn20-xml;charset=UTF-8,' + encodedData,
          'download': name
        })

        console.log(link)
      } else {
        console.log('false')
        link.removeClass('active')
      }
    },
    createNewDiagram () {
      console.log("createBpmn")
      this.openDiagram(this.xmltmp)
    },
    openDiagram (xml) {
      this.bpmnModeler.importXML(xml, function (err) {
        if (err) {
          console.log('error rendering', err)
        } else {
          console.log('rendered')
          console.log(xml)
        }
      })
    },
    saveSVG (done) {
      this.bpmnModeler.saveSVG(done)
    },
    saveDiagram (done) {
      this.bpmnModeler.saveXML({format: true}, function (err, xml) {
        done(err, xml)
      })
    },
    changeFormItem (formItem) {
      formItem.__prefix = 'activiti'
      if (!(!formItem._label)) {
        formItem._name = formItem._label
        delete formItem._label
      }
    },
    // 下载bpmn去除formData，修改camunda为activiti
    getFormProperty (json) {
      for (var key in json) {
        if (typeof json[key] == 'object') {
          this.getFormProperty(json[key])
        } else if (key == '__prefix' && json.__prefix == 'camunda') {
          json.__prefix = 'activiti'
        } else if (key == '_label') {
          json._name = json._label
          delete json._label
        }
        if (key == 'extensionElements' && json.extensionElements.formData && json.extensionElements.formData.formField) {
          let formProperty = JSON.parse(JSON.stringify(json.extensionElements.formData.formField))
          json.extensionElements.formProperty = formProperty
          delete json.extensionElements.formData
        }
        if (key.includes('camunda')) {
          let str = key.replace('camunda', 'activiti')
          json[str] = json[key]
          delete json[key]
        }
      }
    },
    returnFormProperty (json) {
      for (var key in json) {
        if (typeof json[key] == 'object') {
          this.returnFormProperty(json[key])
        } else if (key == '__prefix' && json.__prefix == 'activiti') {
          json.__prefix = 'camunda'
        }
        if (key === 'extensionElements' && json.extensionElements.formProperty && json.extensionElements.formProperty._type !== 'webServer') {
          let formField = JSON.parse(JSON.stringify(json.extensionElements.formProperty))
          if (this.isArrayFn(formField)) {
            formField.forEach(x => {
              console.log('form')
              console.log(x._name)
              if (!(!x._name)) {
                x._label = x._name
                delete x._name
              }
            })
          } else {
            console.log('form')
            console.log(formField._name)
            if (!(!formField._name)) {
              formField._label = formField._name
              delete formField._name
            }
          }
          json.extensionElements.formData = {
            formField,
            __prefix: 'camunda'
          }
          delete json.extensionElements.formProperty
        }
        if (key.includes('activiti')) {
          let str = key.replace('activiti', 'camunda')
          json[str] = json[key]
          delete json[key]
        }
      }
    },
    isArrayFn (value) {
      if (typeof Array.isArray === 'function') {
        return Array.isArray(value)
      } else {
        return Object.prototype.toString.call(value) === '[object Array]'
      }
    }
  },
  // 计算属性
  computed: {}
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  #main {
  }
</style>
