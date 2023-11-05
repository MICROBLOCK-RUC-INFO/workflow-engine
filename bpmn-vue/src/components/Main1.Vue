<template>
  <div class="container-fluid" style="height: 100%; padding:0px">
    <div class="row m-0" style="background-color:#891524;width: 100%;height: 40px;color: brown;border-bottom-style:solid;border-bottom-width:1px;border-bottom-color:grey;">
      <div class="col-2" style="color:white;font-size:larger;font-weight:bold;line-height:40px;">BPMN</div>
      <div class="col-10"><div style="float:right;color:white;line-height:40px;">用户:{{ username }}&nbsp;&nbsp;&nbsp;</div></div>
    </div>
    
    <div class="tab-content" id="nav-tabContent" style="width:100%;height:95%;">
      <div class="tab-pane fade show active" id="createBpmn" role="tabpanel" aria-labelledby="createBpmn-tab" style="height: 100%">
        <createBpmn></createBpmn>
      </div>
    </div>
  </div>
</template>
<script>
  import $ from 'jquery'
  import createBpmn from '@/components/createBpmn'
  import {getCookie} from '../util/utils'
  export default {
    name: 'main1',
    data () {
      return {
        username: getCookie('username'),
        myProcessKey: 1,
        myTaskKey: 1,
        executedTaskKey: 1
      }
    },
    components: {
      createBpmn,
    },
    mounted () {
      this.init()
    },
    methods: {
      async init () {
        var that = this
        $('.nav-item').bind('click', function (e) {
          var display=false;
          var obj;
          console.log(e.target.className.indexOf('bp-dropdown-toggle'))
          if(e.target.className.indexOf('bp-dropdown-toggle')!=-1){
            obj= $(e.target).parent().find('.bp-dropdown-menu')[0];
            if (obj.style.display == "block") {
              display=false;
            } else {
              display=true;
            }
          }
          if(e.target.className.indexOf('bp-dropdown-item')==-1){
            $('.bp-dropdown-menu').css('display','none');
            if(!display){
              obj.style.display = 'none';
            }else{
              obj.style.display = 'block';
            }
          }else{
            $('.bp-dropdown-item').css('background-color','gray');
            $('.bp-dropdown-item').css('color','white');
            e.target.style.backgroundColor='white';
            e.target.style.color='gray';
          }
        })
        
        $('#myTask-tab').bind('click', function () {
          that.myTaskKey++
        })
        $('#executedTask-tab').bind('click', function () {
          that.executedTaskKey++
        })
        $('#myProcessCase-tab').bind('click', function () {
          that.myProcessKey++
        })
      }
    }
  }
</script>
<style scoped>
.bp-dropdown{
  border: 1px solid transparent;
  border-top-left-radius: .25rem;
  border-top-right-radius: .25rem;
  width: 100%;
  list-style-type: none;
  color: white;
  text-align: center;
  line-height: 40px;
  z-index: 2;
  overflow: hidden;
}
.bp-dropdown-toggle::after {
  display: inline-block;
  margin-left: 0.255em;
  vertical-align: 0.255em;
  content: "";
  border-top: 0.3em solid;
  border-right: 0.3em solid transparent;
  border-bottom: 0;
  border-left: 0.3em solid transparent;
}
.bp-dropdown:hover{
  border-color:white;
  height: 100%;
}
.bp-dropdown-menu{
  display: none;
}
.bp-dropdown-item{
  margin-left: -40px;
  height: 40px;
  background-color: grey;
  color: white;
  z-index: 1;
  display: block;
}
.bp-dropdown-item:hover{
  background-color: white;
  color: black;
}
</style>
