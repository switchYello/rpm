### 这是一个java实现的内网穿透工具。
    它能实现通过服务器做跳板来穿透到内网机器上。
    我开发他的目的是疫情在家想要远程公司的机器工作。公司无vpn 无公网ip。
    市面上虽然有相同功能的程序，但是这轮子简单，还是自己造一下比较好。





### 服务端用法配置

    服务端需要绑定一个管理端口用于客户端管理，再绑定一个端口用于映射。
    请在jar文件相同目录下创建一个 config.properties的文件,配置如下。
    
    bindAt为程序绑定的ip和端口，ip是0.0.0.0，端口为绑定的服务端管理端口。
    auto_token为与客户端之间的验证用的，如果客户端提供的auto_token与此不一致，是不允许客户端连接的。



```properties
#common
bindAt=0.0.0.0:9050
auto_token=hcy123456789
```







### 客户端用法

    客户端默认会查找jar文件相同目录下的'conf.json'的配置文件，
    配置文件在resources目录下，按照这个模板配置。



完整配置文件实例:

```json
{
  "server": {
    "serverIp": "127.0.0.1",
    "serverPort": 9050,
    "autoToken": "hcy123456789"
  },
    
  "server_work": [
    {
      "serverPort": "9051",
      "localHost": "0.0.0.0",
      "localPort": "80"
    },
    {
      "serverPort": "9052",
      "localHost": "0.0.0.0",
      "localPort": "81"
    }
  ]
}
```



- serverIp 服务端ip
- serverPort 服务端管理端口
- autoToken 和服务端配置成相同的



server_work 部分即将 `serverPort` 映射到本地的`localHost:localPort`

如示例中 访问`127.0.0.1:9051` 即会把数据转发到`0.0.0.0:80`,

访问`127.0.0.1:9052` 即会把数据转发到`0.0.0.0:81`。



##### 只有配置了映射关系服务器才会监听该端口，并且要保证服务器的指定端口是没有被使用的。






### 配置window远程桌面示例
即在家里远程桌面连接 `123.11.11.2:9051` 就可以连接到公司电脑了。
```
 
 {
  "server": {
    "serverIp": "123.11.11.2",
    "serverPort": 9050,
    "autoToken": "1234567890"
  },
    
  "server_work": [
    {
      "serverPort": "9051",
      "localHost": "0.0.0.0",
      "localPort": "3389"
    }
  ]
}
```
