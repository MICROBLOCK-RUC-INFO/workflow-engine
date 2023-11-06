<template>
  <div class="card-container">
    <svg version="1.1" xmlns="http://www.w3.org/2000/svg" style="display: none;">
      <defs>
        <g id="icon-qrlogin">
          <polygon points="47.6 47.6 47.6 47.8 47.8 47.8 47.6 47.6"></polygon>
          <polygon points="43.1 30.2 38.8 30.2 38.8 25.9 25.9 25.9 38.9 38.9 47.4 38.9 47.4 25.9 47.4 25.9 43.1 25.9 43.1 30.2"></polygon>
          <polygon points="12.9 12.9 12.9 8.6 8.6 8.6 12.9 12.9"></polygon>
          <path d="M26.1,21.8H47.7V0.2H26.1V21.8ZM30.4,4.6h13v13h-13V4.6Z" transform="translate(-0.2 -0.2)"></path>
          <rect x="34.4" y="8.6" width="4.3" height="4.32"></rect>
          <polygon points="4.3 4.3 17.2 4.3 17.2 17.2 21.5 21.5 21.5 0 0 0 4.3 4.3"></polygon>
          <polygon points="47.4 47.4 47.4 43.2 43.2 43.2 47.4 47.4"></polygon>
        </g>
        <g id="icon-tiup">
          <path d="M22.4 7.1L14.2 4 5.9 7.1 6.2 8l7.9-2.9L22 8ZM5.9 8.3H22.4V9.2H5.9V8.3Zm0 6.8H22.4v1H5.9v-1Zm-1 1.2H23.3v0.9H4.9V16.3h0ZM8.1 9.6H9.7v5.1H8.1V9.6h0Zm2.6 0h1.7v5.1H10.7V9.6h0Zm2.6 0h1.6v5.1H13.4V9.6H13.3Zm2.6 0h1.6v5.1H16V9.6H15.9Zm2.6 0h1.6v5.1H18.7V9.6H18.5ZM10.3 8l3.8-1.4L17.9 8h2.5L14 5.6h0L7.7 8h2.6Zm13.2 9.7H4.7a3.8 3.8 0 0 1-1.3-7.3L3.1 9.6h0A4.4 4.4 0 0 1 9 4.4H9L9.9 4.9a4.3 4.3 0 0 1 0.2-1h0a4.3 4.3 0 0 1 8.3 1.3l1.3-0.7 1.5-0.3a4.2 4.2 0 0 1 4.2 4.2h0a4.9 4.9 0 0 1-0.3 1.3 3.9 3.9 0 0 1-1.9 2.1 2.3 2.3 0 0 0 2.5-0.8 3.7 3.7 0 0 1-2.1 6.6H23.5Zm2.4-7.5a5.1 5.1 0 0 0-6.8-6.4 5.2 5.2 0 0 0-9.9-0.3 5.3 5.3 0 0 0-7 6.5 4.6 4.6 0 0 0 2.3 8.6h19a4.6 4.6 0 0 0 2.4-8.4h0Z"></path>
        </g>
        <g id="icon-account">
          <path d="M10.656 16q-2.219 0-3.781-1.563t-1.563-3.781 1.563-3.781 3.781-1.563 3.781 1.563 1.563 3.781-1.563 3.781-3.781 1.563zM10.656 18.688q1.969 0 4.344 0.563t4.344 1.844 1.969 2.906v2.688h-21.313v-2.688q0-1.625 1.969-2.906t4.344-1.844 4.344-0.563z"></path>
        </g>
        <g id="icon-lock">
          <path d="M18.688 10.688q1.063 0 1.844 0.781t0.781 1.844v13.375q0 1.063-0.781 1.844t-1.844 0.781h-16q-1.125 0-1.906-0.781t-0.781-1.844v-13.375q0-1.063 0.781-1.844t1.906-0.781h1.313v-2.688q0-2.75 1.969-4.719t4.719-1.969 4.688 1.969 1.938 4.719v2.688h1.375zM10.656 22.688q1.094 0 1.875-0.781t0.781-1.906-0.781-1.906-1.875-0.781-1.875 0.781-0.781 1.906 0.781 1.906 1.875 0.781zM14.813 10.688v-2.688q0-1.688-1.219-2.906t-2.938-1.219-2.906 1.219-1.188 2.906v2.688h8.25z"></path>
        </g>
        <g id="icon-shield-security">
          <path class="path1" d="M12 1.313l12 5.375v8q0 5.563-3.438 10.156t-8.563 5.844q-5.125-1.25-8.563-5.844t-3.438-10.156v-8zM12 16v-11.75l-9.313 4.125v7.625h9.313v11.938q3.688-1.188 6.25-4.5t3.063-7.438h-9.313z"></path>
        </g>
        <g id="icon-circle-left">
          <path d="M16 32c8.84 0 16-7.16 16-16s-7.16-16-16-16-16 7.16-16 16 7.16 16 16 16zM16 3c7.18 0 13 5.82 13 13s-5.82 13-13 13-13-5.82-13-13 5.82-13 13-13zM20.91 9.91l-2.83-2.83-8.91 8.91 8.91 8.91 2.83-2.83-6.09-6.09z"></path>
        </g>
      </defs>
    </svg>
    <form>
      <div>
        <h3>用户登录</h3>
      </div>
      <div class="input-group">
        <div class="icon">
          <svg viewBox="0 0 32 32">
            <use xlink:href="#icon-account"></use>
          </svg>
          <span class="arrow_left"></span>
          <span class="arrow"></span>
        </div>
        <input type="text" placeholder="用户名" name="username" required="" id="username">
      </div>
      <div class="input-group">
        <div class="icon">
          <svg viewBox="0 0 32 32">
            <use xlink:href="#icon-lock"></use>
          </svg>
          <span class="arrow_left"></span>
          <span class="arrow"></span>
        </div>
        <input type="password" placeholder="密码" name="password" required="" id="password">
      </div>
      <!--
      <div class="input-group" style="width: 50%;float: left;margin-top: 15px;">
        <input type="text" placeholder="请输入验证码" name="code" required="" style="padding-left: 16px">
      </div>
      <div class="input-group" style="width: 50%;float: right;padding: 5px 15px;margin-top: 13px;">
        <img src="/getImg" id="codeImg" @click="getYzm" style="width: 100%;height: 100%;border: 1px solid #ccc;">
      </div>
      -->
      <div style="clear: both;"></div>
      <div class="form-group">
        <button class="button" id="login-submit" type="submit"><span>登录</span></button>
      </div>
      <div>
        <button class="button" id="login-register" type="submit"><span>注册</span></button>
      </div>
    </form>
  </div>
</template>

<script>
import $ from 'jquery'

export default {
  name: 'Login',
  mounted () {
    this.init()
  },
  methods: {
    async init () {
      var that = this
      $('#login-submit').bind('click', function () {
        //that.$router.push('/main1')
        var username = $('#username').val()
        var pwd = $('#password').val()
        if (username === "admin" && pwd === "admin"){
          that.$router.push('/main1')
        } else {
          alert("username or password incorrect!")
        }

        /*var form = new FormData()
        form.append('username', username)
        form.append('pwd', pwd)
        that.axios({
          url: '/serve/login',
          method: 'post',
          data: form
        }).then(function (response) {
          console.log(response)
          if (response.data === 'administrator') {
            that.$router.push('/main1')
          } else {
            that.$router.push('/main1')
          }
        }).catch(function (error) {
          console.log(error)
        })*/
      })
      $('#login-register').bind('click', function () {
        that.$router.push('/main1')
      })
    }
  }
}
</script>

<style scoped>
  input:-webkit-autofill {
    -webkit-box-shadow: 0 0 0 50px #fff inset !important
  }
  .card-container {
    max-width: 370px;
    margin: 8vw auto;
    background: #fff;
    width: 100%;
    box-shadow: 0 2px 2px 0 rgba(0, 0, 0, .14), 0 3px 1px -2px rgba(0, 0, 0, .2), 0 1px 5px 0 rgba(0, 0, 0, .12);
    padding: 2rem 3rem;
    border-radius: 1px;
    -webkit-transform: scale(1);
    transform: scale(1);
    -webkit-transition: box-shadow .1s ease, -webkit-transform .1s ease;
    transition: box-shadow .1s ease, transform .1s ease
  }
  .card-container:hover {
    box-shadow: 0 8px 10px 1px rgba(0, 0, 0, .14), 0 3px 14px 2px rgba(0, 0, 0, .12), 0 5px 5px -3px rgba(0, 0, 0, .2);
    -webkit-transform: scale(1.005);
    transform: scale(1.005)
  }
  .input-group {
    position: relative;
  }
  .input-group > input {
    background-color: white;
    border-radius: 3px;
    box-sizing: border-box;
    border: 1px solid #ccc;
    margin-bottom: -1px;
    width: 100%;
    height: 45px;
    outline: none;
    padding: 0 16px 0 55px;
    position: relative;
  }
  .input-group > .icon {
    width: 43px;
    height: 43px;
    line-height: 43px;
    text-align: center;
    border-radius: 2px 0 0 2px;
    background: #eee;
    position: absolute;
    z-index: 999;
    color: #9a9a9a;
    left: 1px;
    top: 1px;
    padding: 0;
    display: block;
    float: left;
    border-right: 1px solid #dedede;
  }
  svg {
    height: 100%;
    width: 100%;
    fill: currentColor;
    vertical-align: bottom;
  }
  .input-group > .icon > svg {
    width: 21px;
    height: 21px;
    position: absolute;
    left: 14px;
    top: 10px;
  }
  .icon > .arrow_left {
    border-width: 6px;
    border-style: solid;
    border-color: transparent transparent transparent #dedede;
    position: absolute;
    top: 14px;
    right: -13px;
  }
  .icon > .arrow {
    border-width: 6px;
    border-style: solid;
    border-color: transparent transparent transparent #eee;
    position: absolute;
    top: 14px;
    right: -12px;
  }

  .form-group {
    position: relative;
    margin-top: 1.5em;
    margin-bottom: 1em;
    min-height: 1rem;
  }
  .button {
    position: relative;
    font-size: 0.85rem;
    background: #d75757;
    border: 1px solid #891524;
    padding: .3rem 2rem;
    border-radius: 2px;
    width: 100%;
    cursor: pointer;
    -webkit-transition: background-color .1s ease, color .1s ease, box-shadow .1s ease;
    transition: background-color .1s ease, color .1s ease, box-shadow .1s ease;
    overflow: hidden;
  }
  .button::before {
    content: '';
    position: absolute;
    background: #9c3f3f;
    border: 50vh solid #891524;
    border-radius: 50%;
    display: block;
    top: 50%;
    left: 50%;
    z-index: 0;
    opacity: 1;
    -webkit-transform: translate(-50%, -50%) scale(0);
    transform: translate(-50%, -50%) scale(0)
  }

  .button:hover {
    color: #666;
    box-shadow: 0 6px 10px 0 rgba(0, 0, 0, .14), 0 1px 18px 0 rgba(0, 0, 0, .12), 0 3px 5px -1px rgba(0, 0, 0, .2)
  }

  .button:active::before, .button:focus::before {
    -webkit-transition: opacity .1s ease .13s, -webkit-transform .4s ease;
    transition: transform .4s ease, opacity .1s ease .13s, -webkit-transform .4s ease;
    -webkit-transform: translate(-50%, -50%) scale(1);
    transform: translate(-50%, -50%) scale(1);
    opacity: 0
  }
</style>
