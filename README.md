### 这是一个java实现的内网穿透工具。
    它能实现通过服务器做跳板来穿透到内网机器上。
    他能实现客户端服务端数据传输加密
    他能实现多客户端同时使用
    他能实现同时映射多个端口

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
    "bindHost": 9050,
    "token": "123456"
  },
  "clients": [
    {
      "clientName": "hcy_home_pc",
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


- 服务器只允许具有相同Token的客户端连接
- 服务器只能为配置在clients节点下的client服务，如果Token正确client下找不到对应名称的client，也是不能工作的
- 所有配置只在服务器上配置，客户端只能决定连接还是断开



##### 只有配置了映射关系服务器才会监听该端口，并且要保证服务器的指定端口是没有被使用的。



### 配置window远程桌面示例
即在家里远程桌面连接， 就可以连接到公司电脑了。
```json
 
{
  "server": {
    "bindHost": 9050,
    "token": "123456"
  },
  "clients": [
    {
      "clientName": "hcy_home_pc",
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
