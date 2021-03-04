
# One More Http Server

todo:
 async - done
 req as parameter to function  (check with unit functions) - done 
 improve macro compilation - done 
 pass additional headers into CommonResponse - done
 file transfer (send/upload)
 query params materializer ???    
 add more ~> parameters macro - done (check when AnyParam ~> () => unit fn)
 chunk responses - done ??? is it ok do not have Future(Iterator()) 
 setup: 
 chunk size 
 file size 
 date local/timezone
 
 http2
 cookie param
 compressing - done 
 swagger
 resolve todos
 websockets

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
  


[] routing dsl
[] streaming 
[] swagger
[] websocket ? 


```scala

get("test" / LongParam) ~> { x: Long =>
  
}


```

### License: MIT

