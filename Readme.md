
# One More Http Server

todo:
* chunked responses
* tests for files
* resolve todo
* swagger

todo: one import 
now: 
import RoutingImplicits._
import ParamDsl._
need: 
import something._ 

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

