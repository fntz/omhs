
# One More Http Server

todo:
1. async - done
2. req as parameter to function  (check with unit functions) - done 
3. improve macro compilation - done 
3.1 query params materializer ???    
4. add more ~> parameters macro - done (check when AnyParam ~> () => unit fn)
5. streaming https://github.com/lks21c/netty-http-streaming-server/blob/master/src/main/java/com/creamsugardonut/HttpStaticFileServerHandler2.java
6. http2
7. cookie param
8. swagger
9. resolve todos
10. websockets 


[] routing dsl
[] streaming 
[] swagger
[] websocket ? 


```scala

get("test" / LongParam) ~> { x: Long =>
  
}


```

### License: MIT

