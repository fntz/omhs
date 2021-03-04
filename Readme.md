
# One More Http Server

todo:
 async - done
 req as parameter to function  (check with unit functions) - done 
 improve macro compilation - done 
 pass additional headers into CommonResponse
 file transfer
 query params materializer ???    
 add more ~> parameters macro - done (check when AnyParam ~> () => unit fn)
 streaming https://github.com/lks21c/netty-http-streaming-server/blob/master/src/main/java/com/creamsugardonut/HttpStaticFileServerHandler2.java
 http2
 cookie param
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

