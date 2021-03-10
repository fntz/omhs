
# One More Http Server

todo:
* chunked responses
* tests for files
* resolve todo
* swagger
* jmh bench
* syntax: 

```
  path -> path | query | header | cookie | body | file
  query -> header | cookie | body | file
  header -> header | cookie | body | file
  cookie -> header | cookie | body | file
  body -> end
  file -> end

  path / path ? query 
  path / path << header << cookie :/ body 
    
```

todo: probably need to alternative support: `foo | bar | baz`

check codegen: "-Domhs.logLevel=verbose|info|none"


todo: one import 
now: 
import RoutingImplicits._
import ParamDsl._
need: 
import something._ 

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

