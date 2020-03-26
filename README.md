### 这是一个java实现的内网穿透工具。
    它能实现通过服务器做跳板来穿透到内网机器上。
    我开发他的目的是疫情在家想要远程公司的机器工作。公司无vpn 无公网ip。
    市面上虽然有相同功能的程序，但是这轮子简单，还是自己造一下比较好。


### 关于此分支
    此分支相比于master分支来说，他将配置文件放置在服务端上，这样我们可以在任何地方访问服务端修改配置
    而客户端由于在内网改起来会麻烦很多。


### 客户端用法

    客户端默认会查找jar文件相同目录下的'config.properties'的配置文件，
    配置文件在resources目录下，按照这个模板配置。

```properties
#服务器ip
serverIp=127.0.0.1
#服务器端口
serverPort=9050
#客户端名
clientName=hcy_home_pc
#密码
token=123456
```





### 服务端用法配置

    服务端需要绑定一个管理端口用于客户端管理，再绑定一个端口用于映射。
    请在jar文件相同目录下创建一个 config.json的文件,配置如下。


```json
{
  "server": {
    "bindHost": 9050
  },
  "clients": [
    {
      "clientName": "hcy_home_pc",
      "token": "123456",
      "serverWorkers": [
        {
          "serverPort": "9051",
          "localHost": "0.0.0.0",
          "localPort": "81"
        },
        {
          "serverPort": "9052",
          "localHost": "0.0.0.0",
          "localPort": "81"
        },
        {
          "serverPort": "9053",
          "localHost": "0.0.0.0",
          "localPort": "82"
        }
      ]
    }
  ]
}
```



- /server/bindHost 服务器绑定的管理端口
- clients  客户端列表
- clientName 客户端名，必须和客户端的配置一致
- token 该客户端的密码
- serverWorkers 映射关系



##### 只有配置了映射关系服务器才会监听该端口，并且要保证服务器的指定端口是没有被使用的。




### 配置window远程桌面示例
即在家里远程桌面连接， 就可以连接到公司电脑了。
```json
 
{
  "server": {
    "bindHost": 9050
  },
  "clients": [
    {
      "clientName": "hcy_home_pc",
      "token": "123456",
      "serverWorkers": [
        {
          "serverPort": "9051",
          "localHost": "0.0.0.0",
          "localPort": 3389
        }
      ]
    }
  ]
}
```
