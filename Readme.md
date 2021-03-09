
# One More Http Server

todo:
* setup
* chunked responses
* tests for files
* resolve todo
* swagger
* chunks  
* todo add cookie headers if present in response

setup: 
    chunk size 
    file size 
    date local/timezone


idea: 
```
scala 

// add headers into response (common response)
// (x: String) is not a function but some object ? how to do it ?
get(string) ~> { (x: String) => 
   contentType ("asd")
   header "asd", "qwe"  
}

^ is not possible, but 

case class action {
  ... 
}

get(string) ~> action { (x: String) => 
  .... 
}

```
  
```scala 
// setup
OHMSServer.run(9000, t.toHandler, (c: ChannelPipeline) =>
    c.addLast("logger", new LoggingHandler(LogLevel.DEBUG)), OHMSServer.noSetup)
```

[] routing dsl
[] streaming 
[] swagger


```scala

get("test" / LongParam) ~> { x: Long =>
  
}


```

### License: MIT

